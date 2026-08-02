package com.radionula.radionula

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.radionula.radionula.data.db.NulaDatabase
import com.radionula.radionula.domain.model.NulaTrack
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

    @Test
    fun a_track_inserted_into_a_legacy_database_gets_a_new_id_and_leaves_existing_rows_alone() =
        runBlocking {
            val database = NulaDatabase(BundledSQLiteDriver(), dbFile.absolutePath)

            val newId = database.insertTrack(
                NulaTrack(artist = "Khruangbin", title = "August 10", image = "cover-c")
            )

            // The legacy database already has rows 1 and 2 - a new row colliding
            // with either of those would mean last_insert_rowid() is being read
            // from the wrong connection.
            assertEquals(3L, newId)

            val tracks = database.selectAllTracks().sortedBy { it.id }
            assertEquals(3, tracks.size)

            val inserted = tracks.single { it.id == newId.toInt() }
            assertEquals("Khruangbin", inserted.artist)
            assertEquals("August 10", inserted.title)
            assertEquals("cover-c", inserted.image)

            // The pre-existing favourites must be untouched by the insert.
            assertEquals(1, tracks[0].id)
            assertEquals("Izit", tracks[0].artist)
            assertEquals("Make Way For The Solos", tracks[0].title)
            assertEquals("cover-a", tracks[0].image)
            assertEquals(2, tracks[1].id)
            assertEquals("Adi Oasis", tracks[1].artist)
            assertEquals("Serena", tracks[1].title)
            assertEquals("cover-b", tracks[1].image)
        }

    @Test
    fun a_fresh_install_creates_the_database_and_stamps_user_version_1() = runBlocking {
        // Both tests above pre-populate dbFile via raw SQLiteDatabase before
        // NulaDatabase ever touches it. Every real install instead takes the
        // create-from-nothing path, which neither of them exercises.
        dbFile.delete()
        File("${dbFile.path}-wal").delete()
        File("${dbFile.path}-shm").delete()

        val database = NulaDatabase(BundledSQLiteDriver(), dbFile.absolutePath)

        val newId = database.insertTrack(
            NulaTrack(artist = "Izit", title = "Make Way For The Solos", image = "cover-a")
        )
        val tracks = database.selectAllTracks()

        assertEquals(1, tracks.size)
        assertEquals(newId, tracks[0].id.toLong())
        assertEquals("Izit", tracks[0].artist)
        assertEquals("Make Way For The Solos", tracks[0].title)
        assertEquals("cover-a", tracks[0].image)

        SQLiteDatabase.openOrCreateDatabase(dbFile, null).use { assertEquals(1, it.version) }
    }
}
