package com.radionula.radionula.radio


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
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

import com.radionula.radionula.data.network.PlaylistApiService
import com.radionula.radionula.interfaces.IControls
import com.radionula.radionula.model.NulaTrack
import com.radionula.radionula.MyApp
import com.radionula.radionula.PlaylistAdapter
import com.radionula.radionula.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import java.io.IOException
import java.net.MalformedURLException
import java.net.URL


/**
 * A simple [Fragment] subclass.
 */
class PlayerFragment : Fragment() {

    //
    // Top of player
    internal lateinit var anim: RotateAnimation
    internal lateinit var anim2: RotateAnimation

    internal var playing = false

    internal lateinit var ivRecord: ImageView
    internal lateinit var ivLogo: ImageView
    //CircularImageView ivRecordImage;
    internal lateinit var ivRecordImage: ImageView

    //
    // Playlist of player
    internal lateinit var llPlaylist: LinearLayout
    internal lateinit var adapter: PlaylistAdapter
    internal lateinit var ivFaded: ImageView

    //
    // Control of player
    internal lateinit var _controls: IControls

    internal lateinit var ivSkip: ImageView
    internal lateinit var ivPause: ImageView
    internal lateinit var ivTuneIn: ImageView
    private var logoUrl = "drawable://" + R.drawable.nula_logo_ch1
    private var skipurl = R.drawable.play_button_1

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        _controls = (context as IControls?)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_player, container, false)


        ivRecord = view.findViewById<View>(R.id.fragment_top_ivRecord) as ImageView
        ivRecordImage = view.findViewById<View>(R.id.fragment_top_ivRecordImage) as ImageView
        ivLogo = view.findViewById<View>(R.id.fragment_top_ivLogo) as ImageView
        llPlaylist = view.findViewById<View>(R.id.fragment_playlist_llPlaylist) as LinearLayout
        ivFaded = view.findViewById<View>(R.id.fragment_playlist_ivShadow) as ImageView

        // This disable hardware acceleration - fixes a bug for android 5.0
        //ivRecordImage.setLayerType(View.LAYER_TYPE_SOFTWARE, null);


        // Sets load adapter
        //LoadingAdapter loadingAdapter = new LoadingAdapter(getActivity());
        //View item = loadingAdapter.getView(0, null, null);
        //llPlaylist.addView(item);

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

        ivSkip = view.findViewById<View>(R.id.fragment_controls_ivSkip) as ImageView
        ivPause = view.findViewById<View>(R.id.fragment_controls_ivPause) as ImageView
        ivTuneIn = view.findViewById<View>(R.id.fragment_controls_ivTuneIn) as ImageView

        ivSkip.setOnClickListener { _controls.TuneIn() }

        ivPause.setOnClickListener { _controls.Pause() }

        ivTuneIn.setOnClickListener { tuneIn() }


        return view
    }

    private fun tuneIn() {
        ivTuneIn.visibility = View.INVISIBLE
        ivPause.visibility = View.VISIBLE
        ivSkip.visibility = View.VISIBLE

        _controls.TuneIn()
    }

    fun UpdateChannelLogo(imageUrl: String, skipUrl: Int) {
        logoUrl = imageUrl
        skipurl = skipUrl
        MyApp.getImageLoader().displayImage(imageUrl, ivLogo)
        //MyApp.getImageLoader().displayImage(skipUrl, ivSkip);
        ivSkip.setImageResource(skipUrl)

    }

    fun StopVinyl() {
        try {
            playing = false
            ivRecord.animation.cancel()
            ivRecordImage.animation.cancel()
        } catch (e: Exception) {

        }

    }

    fun StartVinyl() {
        //  if (!playing) {
        playing = true

        ivRecord.startAnimation(anim)
        ivRecordImage.startAnimation(anim2)

        ivLogo.bringToFront()
        // }
    }

    fun SetVinylImage(imageUrl: String) {
        //Picasso.with(getContext()).load(imageUrl).into(ivRecordImage);
        SetVinylImageTask().execute(imageUrl)
        //MyApp.aquery.id(ivRecordImage).image(imageUrl, true, true, 0, 0, null, AQuery.FADE_IN);;

        ivLogo.bringToFront()
    }

    fun SetPlaylist(tracks: List<NulaTrack>) {
        adapter = PlaylistAdapter(activity, tracks, PlaylistAdapter.AdapterType.ADD)

        llPlaylist.removeAllViews()

        for (i in 0 until adapter.count) {
            //
            // Set header
            if (i == 0) {
                val view = activity?.layoutInflater?.inflate(R.layout.list_header, null)

                val tvHeader = view?.findViewById<View>(R.id.list_header_textview) as TextView
                tvHeader.text = "NOW PLAYING"

                val artistFont = Typeface.createFromAsset(activity?.assets, "fonts/Roboto-Regular.ttf")
                tvHeader.typeface = artistFont

                llPlaylist.addView(view)
            } else if (i == 1) {
                val view = activity?.layoutInflater?.inflate(R.layout.list_header, null)

                val tvHeader = view?.findViewById<View>(R.id.list_header_textview) as TextView
                tvHeader.text = "PLAYLIST HISTORY"

                val artistFont = Typeface.createFromAsset(activity?.assets, "fonts/Roboto-Regular.ttf")
                tvHeader.typeface = artistFont


                llPlaylist.addView(view)
            }

            val layout = LinearLayout(context)
            val item = adapter.getView(i, null, null)

            llPlaylist.addView(item)

        }
        ivFaded.bringToFront()

    }

    fun UpdatePlaylist(tracks: List<NulaTrack>) {
        SetPlaylist(tracks)
    }

    override fun onResume() {
        super.onResume()

        try {
            if (MyApp.tunedIn) {
                if (playing) {
                    ivRecord.startAnimation(anim)
                    ivRecordImage.startAnimation(anim2)

                    ivLogo.bringToFront()
                }

                ivSkip.visibility = View.VISIBLE
                ivPause.visibility = View.VISIBLE
                ivTuneIn.visibility = View.INVISIBLE

                UpdateChannelLogo(logoUrl, skipurl)
                _controls.UpdatePlaylist()


            }
        } catch (ex: Exception) {
            Log.e(TAG, ex.message)
        }

    }

    internal inner class SetVinylImageTask : AsyncTask<String, Void, Void>() {
        var image: Bitmap? = null

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun onPostExecute(aVoid: Void) {
            super.onPostExecute(aVoid)

            ivRecordImage.setImageBitmap(image)
        }

        override fun doInBackground(vararg params: String): Void? {
            var url: URL? = null
            try {
                url = URL(params[0].replace(" ", "%20"))

                image = BitmapFactory.decodeStream(url.openConnection().getInputStream())

            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }


            return null
        }


    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val playlistApiService = PlaylistApiService()
        GlobalScope.launch(Dispatchers.Main){
            val playlist = playlistApiService.getPlaylist().await()
            Log.d(TAG, playlist.toString())

        }
    }

    companion object {

        private val TAG = "PlayerFagment"
    }
}// Required empty public constructor
