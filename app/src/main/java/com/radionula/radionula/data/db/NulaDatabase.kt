package com.radionula.radionula.data.db

import android.content.ContentValues
import android.content.Context
import com.radionula.radionula.model.NulaTrack
import com.radionula.radionula.util.MyDatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The favourites store, a plain SQLiteOpenHelper.
 *
 * Every call hops to Dispatchers.IO. Opening the database creates the file on
 * first touch, and that used to happen on the main thread - the helper was
 * constructed eagerly in the constructor, and the list query ran straight from
 * an adapter's initialiser.
 */
class NulaDatabase(context: Context) {

    private val helper = MyDatabaseHelper(context)

    suspend fun insertTrack(track: NulaTrack): Long = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(COLUMN_ARTIST, track.artist)
            put(COLUMN_TITLE, track.title)
            put(COLUMN_IMAGE, track.image)
        }
        helper.writableDatabase.insert(TABLE, null, values)
    }

    suspend fun selectAllTracks(): List<NulaTrack> = withContext(Dispatchers.IO) {
        helper.readableDatabase.query(
            TABLE,
            arrayOf(COLUMN_ID, COLUMN_ARTIST, COLUMN_TITLE, COLUMN_IMAGE),
            null, null, null, null, null
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        NulaTrack(
                            artist = cursor.getString(1),
                            title = cursor.getString(2),
                            image = cursor.getString(3),
                            id = cursor.getInt(0)
                        )
                    )
                }
            }
        }
    }

    suspend fun removeTrack(track: NulaTrack): Int = withContext(Dispatchers.IO) {
        helper.writableDatabase.delete(TABLE, "$COLUMN_ID = ?", arrayOf(track.id.toString()))
    }

    private companion object {
        const val TABLE = "NulaTracks"
        const val COLUMN_ID = "_id"
        const val COLUMN_ARTIST = "artist"
        const val COLUMN_TITLE = "title"
        const val COLUMN_IMAGE = "image"
    }
}
