package com.radionula.radionula.data.network

import com.radionula.radionula.domain.model.NulaTrack
import nl.adaptivity.xmlutil.EventType
import nl.adaptivity.xmlutil.XmlReader
import nl.adaptivity.xmlutil.XmlStreaming

/**
 * Parses the RSS 0.92 feeds at https://radionula.com/recently_played_chN.xml.
 *
 * The first <item> is the currently playing track, the rest is history, newest
 * first. <title> holds "Artist - Title" and the cover art sits in <image><url>.
 */
object RecentlyPlayedParser {

    fun parse(xml: String): List<NulaTrack> {
        // The *generic* reader, not the platform one: the feed is remote, and
        // the generic reader does not resolve external entities. This is what
        // isExpandEntityReferences = false did on the DOM parser.
        val reader = XmlStreaming.newGenericReader(xml)
        val tracks = mutableListOf<NulaTrack>()

        while (reader.hasNext()) {
            if (reader.next() == EventType.START_ELEMENT && reader.localName == "item") {
                reader.readItem()?.let(tracks::add)
            }
        }
        return tracks
    }

    /**
     * Reads to the end of the <item> the reader is positioned on.
     *
     * Scoping matters: the feed has a <title> on <channel> too, and only the
     * ones inside an item are tracks.
     */
    private fun XmlReader.readItem(): NulaTrack? {
        var title: String? = null
        var cover: String? = null
        var inImage = false

        while (hasNext()) {
            when (next()) {
                EventType.START_ELEMENT -> when (localName) {
                    "image" -> inImage = true
                    "title" -> title = elementText()
                    // First image's url only, matching the DOM version's item(0).
                    "url" -> if (inImage && cover == null) cover = elementText()
                }

                EventType.END_ELEMENT -> when (localName) {
                    "image" -> inImage = false
                    "item" -> return toTrack(title, cover)
                }

                else -> Unit
            }
        }
        return toTrack(title, cover)
    }

    /** Concatenates the text of the element the reader is positioned on. */
    private fun XmlReader.elementText(): String {
        val text = StringBuilder()
        while (hasNext()) {
            when (next()) {
                EventType.TEXT, EventType.CDSECT, EventType.ENTITY_REF -> text.append(this.text)
                EventType.END_ELEMENT -> return text.toString().trim()
                else -> Unit
            }
        }
        return text.toString().trim()
    }

    private fun toTrack(rawTitle: String?, cover: String?): NulaTrack? {
        val title = rawTitle?.stripCdata()?.takeIf { it.isNotEmpty() } ?: return null
        val parts = title.split(" - ", limit = 2)
        return NulaTrack(
                artist = parts[0].trim(),
                title = parts.getOrElse(1) { "" }.trim(),
                image = cover.orEmpty(),
        )
    }

    /** Some feed generators escape the CDATA markers instead of emitting real CDATA. */
    private fun String.stripCdata(): String =
            replace("<![CDATA[", "").replace("]]>", "").trim()
}
