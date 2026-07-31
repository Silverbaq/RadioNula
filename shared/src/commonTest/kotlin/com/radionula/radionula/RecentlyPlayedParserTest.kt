package com.radionula.radionula

import com.radionula.radionula.data.network.RecentlyPlayedParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecentlyPlayedParserTest {

    /** Trimmed copy of https://radionula.com/recently_played_ch4.xml */
    private val feed = """
        <?xml version='1.0' encoding='utf-8'?>
        <rss version="0.92">
          <channel>
            <title>Radio RSS</title>
            <lastBuildDate>7/30/2026 1:15:35 PM</lastBuildDate>
            <item>
              <title>Izit - Make Way For The Solos</title>
              <description>0:00</description>
              <image>
                <url>https://tracks.radionula.com/covers/izit-make_way_for_the_solos.jpg</url>
              </image>
            </item>
            <item>
              <title>Adi Oasis - Serena</title>
              <description>0:00</description>
              <image>
                <url>https://tracks.radionula.com/covers/adi_oasis_-_serena.jpg</url>
              </image>
            </item>
          </channel>
        </rss>
    """.trimIndent().trim()

    @Test
    fun current_track_is_first_artist_and_title_split_on_the_separator() {
        val tracks = RecentlyPlayedParser.parse(feed)

        assertEquals(2, tracks.size)
        assertEquals("Izit", tracks[0].artist)
        assertEquals("Make Way For The Solos", tracks[0].title)
        assertEquals(
                "https://tracks.radionula.com/covers/izit-make_way_for_the_solos.jpg",
                tracks[0].image
        )
        assertEquals("Adi Oasis", tracks[1].artist)
    }

    @Test
    fun only_the_first_separator_splits_so_titles_keep_their_own_dashes() {
        val tracks = RecentlyPlayedParser.parse(
                itemFeed("<title>Gil Scott-Heron - Me And The Devil - Remix</title>")
        )

        assertEquals("Gil Scott-Heron", tracks[0].artist)
        assertEquals("Me And The Devil - Remix", tracks[0].title)
    }

    @Test
    fun missing_cover_and_missing_separator_do_not_drop_the_track() {
        val tracks = RecentlyPlayedParser.parse(itemFeed("<title>Untitled Jam</title>"))

        assertEquals("Untitled Jam", tracks[0].artist)
        assertEquals("", tracks[0].title)
        assertEquals("", tracks[0].image)
    }

    @Test
    fun cdata_markers_are_stripped() {
        val tracks = RecentlyPlayedParser.parse(
                itemFeed("<title>&lt;![CDATA[Bobby Oroza - Strange Girl]]&gt;</title>")
        )

        assertEquals("Bobby Oroza", tracks[0].artist)
        assertEquals("Strange Girl", tracks[0].title)
    }

    @Test
    fun items_without_a_title_are_skipped() {
        val tracks = RecentlyPlayedParser.parse(itemFeed("<description>0:00</description>"))

        assertTrue(tracks.isEmpty())
    }

    private fun itemFeed(itemBody: String) =
            """<rss version="0.92"><channel><item>$itemBody</item></channel></rss>"""
}
