package com.radionula.radionula.data.db

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.SQLiteException
import androidx.sqlite.execSQL
import com.radionula.radionula.core.util.ioDispatcher
import com.radionula.radionula.core.util.logError
import com.radionula.radionula.domain.model.NulaTrack
import kotlinx.coroutines.withContext

/**
 * The favourites store.
 *
 * Same three suspend functions the SQLiteOpenHelper version exposed, so the
 * ViewModels are unchanged - and deliberately the same table, with the same
 * DDL, so a database written by the old build is picked up as-is. There is no
 * migration and no schema version to bump.
 *
 * ponytail: a connection is opened per operation rather than held open, with
 * no mutex and no busy timeout. NulaDatabase is a Koin single shared between
 * RadioViewModel (insertTrack) and FavoritesViewModel (selectAllTracks,
 * removeTrack), so two genuinely simultaneous calls could throw SQLITE_BUSY
 * under the default rollback journal. That's a liveness risk from concurrent
 * access, not a list-size one - unlikely from manual taps, but if two screens
 * ever touch the store at once, hold a single connection behind a Mutex
 * rather than reaching for a connection pool.
 */
class NulaDatabase(
    private val driver: SQLiteDriver,
    private val databasePath: String,
) {

    // The old ContentValues insert (SQLiteDatabase.insert()) caught SQLException
    // internally and returned -1 rather than throwing. SQLiteStatement.step()
    // throws instead, and addFavoriteClicked launches this with no try of its
    // own, so an uncaught SQLITE_BUSY here would kill the app instead of just
    // failing to save a favourite. Restore the old failure mode.
    suspend fun insertTrack(track: NulaTrack): Long = try {
        withConnection { connection ->
            connection
                .prepare("INSERT INTO $TABLE ($COLUMN_ARTIST, $COLUMN_TITLE, $COLUMN_IMAGE) VALUES (?, ?, ?)")
                .use { statement ->
                    statement.bindText(1, track.artist)
                    statement.bindText(2, track.title)
                    statement.bindText(3, track.image)
                    statement.step()
                }
            // The old ContentValues insert returned the new row id, and
            // FavoritesViewModel's contract still says Long.
            connection.prepare("SELECT last_insert_rowid()").use { statement ->
                if (statement.step()) statement.getLong(0) else -1L
            }
        }
    } catch (e: SQLiteException) {
        logError("NulaDatabase", "Could not insert $track", e)
        -1L
    }

    suspend fun selectAllTracks(): List<NulaTrack> = withConnection { connection ->
        connection
            .prepare("SELECT $COLUMN_ID, $COLUMN_ARTIST, $COLUMN_TITLE, $COLUMN_IMAGE FROM $TABLE")
            .use { statement ->
                buildList {
                    while (statement.step()) {
                        add(
                            NulaTrack(
                                artist = statement.getText(1),
                                title = statement.getText(2),
                                image = statement.getText(3),
                                id = statement.getInt(0),
                            )
                        )
                    }
                }
            }
    }

    suspend fun removeTrack(track: NulaTrack): Int = withConnection { connection ->
        connection.prepare("DELETE FROM $TABLE WHERE $COLUMN_ID = ?").use { statement ->
            statement.bindInt(1, track.id)
            statement.step()
        }
        connection.prepare("SELECT changes()").use { statement ->
            if (statement.step()) statement.getInt(0) else 0
        }
    }

    /**
     * CREATE TABLE IF NOT EXISTS on every open, which is exactly what
     * MyDatabaseHelper's onCreate and onUpgrade both did. Idempotent, so a
     * database from the old build needs nothing done to it, and a fresh install
     * gets the same schema it always had.
     */
    private suspend fun <T> withConnection(block: (SQLiteConnection) -> T): T =
        withContext(ioDispatcher) {
            val connection = driver.open(databasePath)
            try {
                connection.execSQL(CREATE_TRACKS)
                connection.stampFreshUserVersion()
                block(connection)
            } finally {
                connection.close()
            }
        }

    /**
     * SQLiteOpenHelper stamped every database it created with
     * user_version = 1. The driver path never touches user_version, so a
     * fresh install would otherwise leave it at SQLite's own default of 0 -
     * only setting it when it is still 0 leaves an existing database untouched.
     */
    private fun SQLiteConnection.stampFreshUserVersion() {
        val isFresh = prepare("PRAGMA user_version").use { statement ->
            statement.step() && statement.getInt(0) == 0
        }
        if (isFresh) execSQL("PRAGMA user_version = 1")
    }

    private companion object {
        const val TABLE = "NulaTracks"
        const val COLUMN_ID = "_id"
        const val COLUMN_ARTIST = "artist"
        const val COLUMN_TITLE = "title"
        const val COLUMN_IMAGE = "image"

        // Byte-for-byte the DDL MyDatabaseHelper used. Favourites are user data;
        // changing this column list strands them.
        const val CREATE_TRACKS = """
            CREATE TABLE IF NOT EXISTS NulaTracks (
                _id INTEGER PRIMARY KEY AUTOINCREMENT,
                artist TEXT NOT NULL,
                title TEXT NOT NULL,
                image TEXT NOT NULL
            )
        """
    }
}
