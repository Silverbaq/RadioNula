package com.radionula.radionula

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.radionula.radionula.services.mediaplayer.TuningNoise
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The tuning noise moved from res/raw to a Compose resource, so it now reaches
 * MediaPlayer as an asset file descriptor. That only works while AAPT leaves
 * .mp3 uncompressed and the asset path matches packageOfResClass - neither of
 * which fails at build time. This is the test that would.
 */
@RunWith(AndroidJUnit4::class)
class TuningNoiseTest {

    @Test
    fun the_static_plays_from_the_compose_resource_and_stops_again() {
        val noise = TuningNoise(InstrumentationRegistry.getInstrumentation().targetContext)
        try {
            noise.start()
            assertTrue("the tuning noise did not start", noise.isPlaying)

            noise.stop()
            assertFalse("the tuning noise did not stop", noise.isPlaying)
        } finally {
            noise.release()
        }
    }
}
