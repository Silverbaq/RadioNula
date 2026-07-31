package com.radionula.radionula

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.radionula.radionula.data.db.NulaDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Favourites are user data. This is the gate on the SQLiteOpenHelper ->
 * androidx.sqlite swap: it recreates the exact database an installed 2.3.0 has,
 * opens it with the new code, and checks the rows came through untouched.
 */
@RunWith(AndroidJUnit4::class)
class LegacyFavoritesMigrationTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var dbFile: File

    @Before
    fun createLegacyDatabase() {
        dbFile = context.getDatabasePath("MigrationTestNulaDB")
        dbFile.parentFile?.mkdirs()
        dbFile.delete()
        File("${dbFile.path}-wal").delete()
        File("${dbFile.path}-shm").delete()

        // Byte-for-byte what MyDatabaseHelper wrote, including user_version = 1
        // and the absence of an explicit NOT NULL on _id.
        SQLiteDatabase.openOrCreateDatabase(dbFile, null).use { legacy ->
            legacy.execSQL(
                """
                CREATE TABLE IF NOT EXISTS NulaTracks (
                    _id INTEGER PRIMARY KEY AUTOINCREMENT,
                    artist TEXT NOT NULL,
                    title TEXT NOT NULL,
                    image TEXT NOT NULL
                )
                """.trimIndent()
            )
            legacy.execSQL(
                "INSERT INTO NulaTracks (artist, title, image) VALUES " +
                    "('Izit', 'Make Way For The Solos', 'cover-a')," +
                    "('Adi Oasis', 'Serena', 'cover-b')"
            )
            legacy.version = 1
        }
    }

    @Test
    fun favorites_saved_before_the_driver_swap_survive_it() = runBlocking {
        val database = NulaDatabase(BundledSQLiteDriver(), dbFile.absolutePath)

        val tracks = database.selectAllTracks().sortedBy { it.id }

        assertEquals(2, tracks.size)
        assertEquals("Izit", tracks[0].artist)
        assertEquals("Make Way For The Solos", tracks[0].title)
        assertEquals("cover-a", tracks[0].image)
        assertEquals("Adi Oasis", tracks[1].artist)
        // Ids are the favourites' identity for deletion, so they must not shift.
        assertEquals(1, tracks[0].id)
        assertEquals(2, tracks[1].id)
    }

    @Test
    fun a_track_removed_from_a_legacy_database_is_the_right_one() = runBlocking {
        val database = NulaDatabase(BundledSQLiteDriver(), dbFile.absolutePath)
        val before = database.selectAllTracks().sortedBy { it.id }

        val removed = database.removeTrack(before[0])

        assertEquals(1, removed)
        val after = database.selectAllTracks()
        assertEquals(1, after.size)
        assertEquals("Adi Oasis", after[0].artist)
    }
}
