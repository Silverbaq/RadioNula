package com.radionula.radionula.data.network

import com.radionula.radionula.domain.model.NulaTrack
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Parses the RSS 0.92 feeds at https://radionula.com/recently_played_chN.xml.
 *
 * The first <item> is the currently playing track, the rest is history, newest
 * first. <title> holds "Artist - Title" and the cover art sits in <image><url>.
 */
object RecentlyPlayedParser {

    fun parse(xml: String): List<NulaTrack> {
        val factory = DocumentBuilderFactory.newInstance().apply {
            // Feed is remote: never let it pull in external content.
            isExpandEntityReferences = false
        }
        val items = factory.newDocumentBuilder()
                .parse(InputSource(StringReader(xml)))
                .getElementsByTagName("item")

        return (0 until items.length).mapNotNull { index ->
            val item = items.item(index) as? Element ?: return@mapNotNull null
            val title = item.firstText("title")?.stripCdata() ?: return@mapNotNull null
            val parts = title.split(" - ", limit = 2)
            NulaTrack(
                artist = parts[0].trim(),
                title = parts.getOrElse(1) { "" }.trim(),
                image = item.cover().orEmpty()
            )
        }
    }

    private fun Element.cover(): String? =
            (getElementsByTagName("image").item(0) as? Element)?.firstText("url")

    private fun Element.firstText(tag: String): String? =
            getElementsByTagName(tag).item(0)?.textContent?.trim()?.takeIf { it.isNotEmpty() }

    /** Some feed generators escape the CDATA markers instead of emitting real CDATA. */
    private fun String.stripCdata(): String =
            replace("<![CDATA[", "").replace("]]>", "").trim()
}
