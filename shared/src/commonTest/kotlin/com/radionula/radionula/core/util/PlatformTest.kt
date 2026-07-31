package com.radionula.radionula.core.util

import kotlin.test.Test
import kotlin.test.assertTrue

class PlatformTest {

    @Test
    fun epoch_millis_is_a_real_wall_clock_time() {
        // Sanity check that the actual is wired up at all: any plausible
        // wall-clock value is after 2020-01-01.
        assertTrue(epochMillis() > 1_577_836_800_000L)
    }
}
