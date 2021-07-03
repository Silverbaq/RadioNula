package com.radionula.radionula.radio

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.Toast
import com.radionula.radionula.BaseFragment
import com.radionula.radionula.R
import com.radionula.radionula.model.NulaTrack
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL

class PlayerFragment : BaseFragment(), FavoritesListener {
    private val radioViewModel: RadioViewModel by viewModel()
    private val adapter: PlaylistAdapter = PlaylistAdapter(clickListener = this)

    override fun getLayoutId() = R.layout.fragment_player

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playlistRecyclerView.adapter = adapter

        radioViewModel.tunedIn.onResult { setTunedIn() }
        radioViewModel.isPlaying.onResult { isPlaying -> if (isPlaying) { startVinyl() } }
        radioViewModel.currentSong.onResult { setVinylImage(it.cover) }
        radioViewModel.pause.onResult{ stopVinyl() }
        radioViewModel.playlist.onResult(::setPlaylist)
        radioViewModel.currentChannelResources.onResult(::setChannelLogo)
        radioViewModel.getsNoizy.onResult{ radioViewModel.pauseRadio() }
        radioViewModel.favoriteAdded.onResult(::postFavoriteAddedToast)

        fragment_controls_ivSkip.setOnClickListener {
            radioViewModel.nextChannel()
        }
        fragment_controls_ivPause.setOnClickListener {
            radioViewModel.pauseRadio()
        }
        fragment_controls_ivTuneIn.setOnClickListener {
            radioViewModel.tuneIn()
            radioViewModel.autoFetchPlaylist()
        }


        // TODO: Make sure if this is needed
        // Call State
        //PhoneStateLiveData(
        //        requireContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        //).observe(this, Observer { idle -> if (!idle) radioViewModel.pauseRadio() })
    }

    private fun setTunedIn() {
        fragment_controls_ivTuneIn.visibility = View.INVISIBLE
        fragment_controls_ivPause.visibility = View.VISIBLE
        fragment_controls_ivSkip.visibility = View.VISIBLE
    }

    private fun setChannelLogo(channelResources: Triple<Int, Int, Int>) {
        fragment_top_ivLogo.setImageResource(channelResources.first)
        fragment_controls_ivSkip.setImageResource(channelResources.second)
        fragment_controls_ivPause.setImageResource(channelResources.third)
    }

    private fun startVinyl() {
        fragment_top_ivRecord.startAnimation(anim)
        fragment_top_ivRecordImage.startAnimation(anim2)

        fragment_top_ivLogo.bringToFront()
    }

    private fun setVinylImage(imageUrl: String) {
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

    private fun setPlaylist(tracks: List<NulaTrack>) {
        adapter.update(tracks)
    }

    private fun stopVinyl() {
        try {
            fragment_top_ivRecord.animation.cancel()
            fragment_top_ivRecordImage.animation.cancel()
        } catch (e: Exception) {

        }
    }

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

    companion object {
        private val TAG = "PlayerFagment"
    }

    override fun onAddFavoriteClicked(track: NulaTrack) {
        radioViewModel.addFavoriteClicked(track)
    }

    private fun postFavoriteAddedToast(title: String) {
        Toast.makeText(activity, "Added $title to favorites", Toast.LENGTH_LONG).show()
    }
}
