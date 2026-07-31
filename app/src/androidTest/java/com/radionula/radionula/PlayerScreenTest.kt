package com.radionula.radionula

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.radionula.radionula.domain.model.NulaTrack
import com.radionula.radionula.features.player.PlayerUiState
import com.radionula.radionula.features.player.PlayerScreen
import com.radionula.radionula.core.ui.theme.NulaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The playlist used to be a RecyclerView whose adapter injected two blank rows
 * and keyed the headers off positions 0 and 2. That is now plain composition,
 * and these cover the same rules.
 */
@RunWith(AndroidJUnit4::class)
class PlayerScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(
        state: PlayerUiState,
        onTuneIn: () -> Unit = {},
        onAddFavorite: (com.radionula.radionula.domain.model.NulaTrack) -> Unit = {},
    ) = composeRule.setContent {
        NulaTheme {
            PlayerScreen(
                state = state,
                onTuneIn = onTuneIn,
                onSkip = {},
                onPause = {},
                onAddFavorite = onAddFavorite,
            )
        }
    }

    @Test
    fun a_cold_start_offers_tune_in_and_no_transport_controls() {
        setContent(PlayerUiState())

        composeRule.onNodeWithContentDescription("Tune in").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Pause").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Skip to next").assertDoesNotExist()
    }

    @Test
    fun tuning_in_swaps_the_button_for_the_transport_controls() {
        setContent(PlayerUiState(showTuneIn = false))

        composeRule.onNodeWithContentDescription("Tune in").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Pause").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Skip to next").assertIsDisplayed()
    }

    @Test
    fun an_empty_playlist_has_no_headers() {
        setContent(PlayerUiState(showTuneIn = false))

        composeRule.onNodeWithText("NOW PLAYING").assertDoesNotExist()
        composeRule.onNodeWithText("PLAYLIST HISTORY").assertDoesNotExist()
    }

    @Test
    fun a_single_track_is_now_playing_with_no_history_yet() {
        setContent(PlayerUiState(showTuneIn = false, tracks = listOf(track("Session Victim"))))

        composeRule.onNodeWithText("NOW PLAYING").assertIsDisplayed()
        composeRule.onNodeWithText("Session Victim").assertIsDisplayed()
        composeRule.onNodeWithText("PLAYLIST HISTORY").assertDoesNotExist()
    }

    @Test
    fun the_second_track_onwards_lands_under_playlist_history() {
        setContent(
            PlayerUiState(
                showTuneIn = false,
                tracks = listOf(track("Session Victim"), track("Lord Echo")),
            )
        )

        composeRule.onNodeWithText("NOW PLAYING").assertIsDisplayed()
        composeRule.onNodeWithText("PLAYLIST HISTORY").assertIsDisplayed()
        composeRule.onNodeWithText("Lord Echo").assertIsDisplayed()
    }

    @Test
    fun a_row_reveals_the_favourite_button_only_once_tapped() {
        val added = mutableListOf<com.radionula.radionula.domain.model.NulaTrack>()
        setContent(
            state = PlayerUiState(showTuneIn = false, tracks = listOf(track("Session Victim"))),
            onAddFavorite = { added += it },
        )

        composeRule.onNodeWithContentDescription("Add to favorite").assertDoesNotExist()

        composeRule.onNodeWithText("Session Victim").performClick()
        composeRule.onNodeWithContentDescription("Add to favorite").performClick()

        assertEquals(listOf("Session Victim"), added.map { it.artist })
        // Tapping it also dismisses the scrim, as the old adapter did.
        composeRule.onNodeWithContentDescription("Add to favorite").assertDoesNotExist()
    }

    @Test
    fun tune_in_is_clickable() {
        var tuned = 0
        setContent(PlayerUiState(), onTuneIn = { tuned++ })

        composeRule.onNodeWithContentDescription("Tune in").performClick()

        assertEquals(1, tuned)
    }

    private fun track(artist: String) =
        _root_ide_package_.com.radionula.radionula.domain.model.NulaTrack(
            artist,
            "$artist title",
            "",
            id = artist.hashCode()
        )
}
