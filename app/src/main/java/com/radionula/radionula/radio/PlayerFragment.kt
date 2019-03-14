package com.radionula.radionula.radio


import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.radionula.radionula.PlaylistAdapter
import com.radionula.radionula.R
import com.radionula.radionula.model.NulaTrack
import com.radionula.radionula.util.PhoneStateLiveData
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
    // Image spin animation
    private val anim: RotateAnimation = RotateAnimation(0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f).apply {
        interpolator = LinearInterpolator()
        repeatCount = Animation.INFINITE
        repeatMode = Animation.REVERSE
        duration = 50
    }
    // Rotate animation
    private val anim2: RotateAnimation = RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f).apply {
        interpolator = LinearInterpolator()
        repeatCount = Animation.INFINITE
        duration = 4000
    }

    //
    // Playlist of player
    private lateinit var adapter: PlaylistAdapter

    val radioViewModel: RadioModelView by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        radioViewModel.observeTuneIn().observe(this, Observer {
            fragment_controls_ivTuneIn.visibility = View.INVISIBLE
            fragment_controls_ivPause.visibility = View.VISIBLE
            fragment_controls_ivSkip.visibility = View.VISIBLE

            if (it != null)
                tuneIn()
        })
        radioViewModel.observePlaying().observe(this, Observer { isPlaying ->
            if (isPlaying) {
                StartVinyl()
            }
        })
        radioViewModel.observeCurrentSong().observe(this, Observer { SetVinylImage(it.cover) })
        radioViewModel.observePause().observe(this, Observer {
            if (it != null) {
                StopVinyl()
            }
        })
        radioViewModel.observePlaylist().observe(this, Observer { newPlaylist ->
            val playlist = newPlaylist.map { NulaTrack(it.artist, it.title, it.cover) }
            SetPlaylist(playlist)
        })
        radioViewModel.observeCurrentChannel().observe(this, Observer { channel ->
            setChannelLogo(channel)
        })

        fragment_controls_ivSkip.setOnClickListener {
            GlobalScope.async { radioViewModel.nextChannel() }
        }
        fragment_controls_ivPause.setOnClickListener {
            radioViewModel.pauseRadio()
        }
        fragment_controls_ivTuneIn.setOnClickListener {
            radioViewModel.tuneIn()
            GlobalScope.launch {
                radioViewModel.autoFetchPlaylist()
            }
        }

        // TODO: Make sure if this is needed
        // Call State
        //PhoneStateLiveData(
        //        requireContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        //).observe(this, Observer { idle -> if (!idle) radioViewModel.pauseRadio() })
    }

    private fun tuneIn() {
        GlobalScope.async {
            radioViewModel.fetchPlaylist()
        }
    }

    fun setChannelLogo(channel: ChannelPresenter.Channel) {
        when (channel) {
            ChannelPresenter.Channel.Classic -> {
                fragment_top_ivLogo.setImageResource(R.drawable.nula_channel1)
                fragment_controls_ivSkip.setImageResource(R.drawable.skip_channel1)
                fragment_controls_ivPause.setImageResource(R.drawable.pause_channel1)
            }
            ChannelPresenter.Channel.Ch2 -> {
                fragment_top_ivLogo.setImageResource(R.drawable.nula_channel2)
                fragment_controls_ivSkip.setImageResource(R.drawable.skip_channel2)
                fragment_controls_ivPause.setImageResource(R.drawable.pause_channel2)
            }
            ChannelPresenter.Channel.Smoky -> {
                fragment_top_ivLogo.setImageResource(R.drawable.nula_channel3)
                fragment_controls_ivSkip.setImageResource(R.drawable.skip_channel3)
                fragment_controls_ivPause.setImageResource(R.drawable.pause_channel3)
            }
        }
    }

    fun StopVinyl() {
        try {
            fragment_top_ivRecord.animation.cancel()
            fragment_top_ivRecordImage.animation.cancel()
        } catch (e: Exception) {

        }

    }

    fun StartVinyl() {
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
            val item = adapter.getView(i, null, null)

            fragment_playlist_llPlaylist.addView(item)

        }
        fragment_playlist_ivShadow.bringToFront()
    }

    companion object {
        private val TAG = "PlayerFagment"
    }
}
