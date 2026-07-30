package com.radionula.radionula.util

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(database: SQLiteDatabase) {
        database.execSQL(CREATE_TRACKS)
    }

    /**
     * Idempotent on purpose.
     *
     * This used to drop a table called MyEmployees - which has never existed in
     * this database - and then re-run CREATE TABLE against the live NulaTracks
     * table, so the first DATABASE_VERSION bump would have failed with
     * "table NulaTracks already exists".
     *
     * Favourites are user data and are never dropped. A genuine schema change
     * belongs here as a versioned ALTER keyed off oldVersion.
     */
    override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        database.execSQL(CREATE_TRACKS)
    }

    private companion object {
        const val DATABASE_NAME = "NulaDB"
        const val DATABASE_VERSION = 1

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
