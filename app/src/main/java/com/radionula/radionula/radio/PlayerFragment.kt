package com.radionula.radionula.radio


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Contacts
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.radionula.radionula.MyApp
import com.radionula.radionula.PlaylistAdapter
import com.radionula.radionula.R
import com.radionula.radionula.interfaces.IControls
import com.radionula.radionula.model.NulaTrack
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL


/**
 * A simple [Fragment] subclass.
 */
class PlayerFragment : Fragment() {

    //
    // Top of player
    private lateinit var anim: RotateAnimation
    private lateinit var anim2: RotateAnimation

    private var playing = false

    //
    // Playlist of player
    private lateinit var adapter: PlaylistAdapter

    //
    // Control of player
    private lateinit var _controls: IControls

    private var logoUrl = "drawable://" + R.drawable.nula_logo_ch1
    private var skipurl = R.drawable.play_button_1

    val radioViewModel: RadioModelView by viewModel()

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        _controls = (context as IControls?)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_player, container, false)

       // Image spin animation
        anim = RotateAnimation(0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        anim.interpolator = LinearInterpolator()
        anim.repeatCount = Animation.INFINITE
        anim.repeatMode = Animation.REVERSE
        anim.duration = 50

        // Rotate animation
        anim2 = RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        anim2.interpolator = LinearInterpolator()
        anim2.repeatCount = Animation.INFINITE
        anim2.duration = 4000

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_controls_ivSkip.setOnClickListener { _controls.TuneIn() }
        fragment_controls_ivPause.setOnClickListener { _controls.Pause() }
        fragment_controls_ivTuneIn.setOnClickListener { tuneIn() }
    }

    private fun tuneIn() {
        fragment_controls_ivTuneIn.visibility = View.INVISIBLE
        fragment_controls_ivPause.visibility = View.VISIBLE
        fragment_controls_ivSkip.visibility = View.VISIBLE

        _controls.TuneIn()
    }

    fun UpdateChannelLogo(imageUrl: String, skipUrl: Int) {
        logoUrl = imageUrl
        skipurl = skipUrl
        MyApp.getImageLoader()?.displayImage(imageUrl, fragment_top_ivLogo)
        fragment_controls_ivSkip.setImageResource(skipUrl)
    }

    fun StopVinyl() {
        try {
            playing = false
            fragment_top_ivRecord.animation.cancel()
            fragment_top_ivRecordImage.animation.cancel()
        } catch (e: Exception) {

        }

    }

    fun StartVinyl() {
        playing = true

        fragment_top_ivRecord.startAnimation(anim)
        fragment_top_ivRecordImage.startAnimation(anim2)

        fragment_top_ivLogo.bringToFront()
    }

    fun SetVinylImage(imageUrl: String) {

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val url = URL(imageUrl.replace(" ", "%20"))

                val image = withContext(Dispatchers.IO) {
                    BitmapFactory.decodeStream(url.openConnection().getInputStream())
                }
                fragment_top_ivRecordImage.setImageBitmap(image)
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        fragment_top_ivLogo.bringToFront()
    }

    fun SetPlaylist(tracks: List<NulaTrack>) {
        adapter = PlaylistAdapter(activity, tracks, PlaylistAdapter.AdapterType.ADD)

        fragment_playlist_llPlaylist.removeAllViews()

        for (i in 0 until adapter.count) {
            //
            // Set header
            if (i == 0) {
                val view = activity?.layoutInflater?.inflate(R.layout.list_header, null)

                val tvHeader = view?.findViewById<View>(R.id.list_header_textview) as TextView
                tvHeader.text = "NOW PLAYING"

                val artistFont = Typeface.createFromAsset(activity?.assets, "fonts/Roboto-Regular.ttf")
                tvHeader.typeface = artistFont

                fragment_playlist_llPlaylist.addView(view)
            } else if (i == 1) {
                val view = activity?.layoutInflater?.inflate(R.layout.list_header, null)

                val tvHeader = view?.findViewById<View>(R.id.list_header_textview) as TextView
                tvHeader.text = "PLAYLIST HISTORY"

                val artistFont = Typeface.createFromAsset(activity?.assets, "fonts/Roboto-Regular.ttf")
                tvHeader.typeface = artistFont


                fragment_playlist_llPlaylist.addView(view)
            }

            val layout = LinearLayout(context)
            val item = adapter.getView(i, null, null)

            fragment_playlist_llPlaylist.addView(item)

        }
        fragment_playlist_ivShadow.bringToFront()

    }

    fun UpdatePlaylist(tracks: List<NulaTrack>) {
        SetPlaylist(tracks)
    }

    override fun onResume() {
        super.onResume()

        try {
            if (MyApp.tunedIn) {
                if (playing) {
                    fragment_top_ivRecord.startAnimation(anim)
                    fragment_top_ivRecordImage.startAnimation(anim2)

                    fragment_top_ivLogo.bringToFront()
                }

                fragment_controls_ivSkip.visibility = View.VISIBLE
                fragment_controls_ivPause.visibility = View.VISIBLE
                fragment_controls_ivTuneIn.visibility = View.INVISIBLE

                UpdateChannelLogo(logoUrl, skipurl)
                _controls.UpdatePlaylist()


            }
        } catch (ex: Exception) {
            Log.e(TAG, ex.message)
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        radioViewModel.observeCurrentSong().observe(this, Observer {
            Log.d(TAG, it.toString())
            SetVinylImage(it.cover)
        })

        GlobalScope.async {
            radioViewModel.fetchPlaylist()
        }
    }

    companion object {

        private val TAG = "PlayerFagment"
    }
}// Required empty public constructor
