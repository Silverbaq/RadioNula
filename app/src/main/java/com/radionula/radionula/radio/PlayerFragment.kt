package com.radionula.radionula.radio

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.Toast
import com.radionula.radionula.BaseFragment
import com.radionula.radionula.databinding.FragmentPlayerBinding
import com.radionula.radionula.model.NulaTrack
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL

class PlayerFragment : BaseFragment(), FavoritesListener {
    private lateinit var binding: FragmentPlayerBinding

    private val radioViewModel: RadioViewModel by viewModel()
    private val coroutineScope: CoroutineScope by inject(named("main"))
    private val adapter: PlaylistAdapter = PlaylistAdapter(clickListener = this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.playlistRecyclerView.adapter = adapter

        radioViewModel.tunedIn.onResult { setTunedIn() }
        radioViewModel.isPlaying.onResult { isPlaying -> if (isPlaying) { startVinyl() } }
        radioViewModel.currentSong.onResult { setVinylImage(it.cover) }
        radioViewModel.pause.onResult{ stopVinyl() }
        radioViewModel.playlist.onResult(::setPlaylist)
        radioViewModel.currentChannelResources.onResult(::setChannelLogo)
        radioViewModel.getsNoizy.onResult{ radioViewModel.pauseRadio() }
        radioViewModel.favoriteAdded.onResult(::postFavoriteAddedToast)

        binding.fragmentControlsIvSkip.setOnClickListener {
            radioViewModel.nextChannel()
        }
        binding.fragmentControlsIvPause.setOnClickListener {
            radioViewModel.pauseRadio()
        }
        binding.fragmentControlsIvTuneIn.setOnClickListener {
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
        binding.fragmentControlsIvTuneIn.visibility = View.INVISIBLE
        binding.fragmentControlsIvPause.visibility = View.VISIBLE
        binding.fragmentControlsIvSkip.visibility = View.VISIBLE
    }

    private fun setChannelLogo(channelResources: Triple<Int, Int, Int>) {
        binding.fragmentTopIvLogo.setImageResource(channelResources.first)
        binding.fragmentControlsIvSkip.setImageResource(channelResources.second)
        binding.fragmentControlsIvPause.setImageResource(channelResources.third)
    }

    private fun startVinyl() {
        binding.fragmentTopIvRecord.startAnimation(anim)
        binding.fragmentTopIvRecordImage.startAnimation(anim2)

        binding.fragmentTopIvLogo.bringToFront()
    }

    private fun setVinylImage(imageUrl: String) {
        coroutineScope.launch(Dispatchers.Main) {
            try {
                val url = URL(imageUrl.replace(" ", "%20"))

                val image = withContext(Dispatchers.IO) {
                    BitmapFactory.decodeStream(url.openConnection().getInputStream())
                }
                binding.fragmentTopIvRecordImage.setImageBitmap(image)
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        binding.fragmentTopIvLogo.bringToFront()
    }

    private fun setPlaylist(tracks: List<NulaTrack>) {
        adapter.update(tracks)
    }

    private fun stopVinyl() {
        try {
            binding.fragmentTopIvRecord.animation.cancel()
            binding.fragmentTopIvRecordImage.animation.cancel()
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
