package com.radionula.radionula.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.radionula.radionula.model.NulaTrack;
import com.radionula.radionula.util.MyDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class NulaDatabase {
    private MyDatabaseHelper databaseHelper;
    private SQLiteDatabase database;

    public final static String EMP_TABLE = "NulaTracks"; // name of table

    public final static String EMP_ID = "_id"; // id value for track
    public final static String EMP_ARTIST = "artist";  // artist name of track
    public final static String EMP_TITLE = "title";  // title of track
    public final static String EMP_IMAGE = "image";  // image of track

    /**
     * @param context
     */
    public NulaDatabase(Context context) {
        databaseHelper = new MyDatabaseHelper(context);
        database = databaseHelper.getWritableDatabase();
    }


    public long insertTrack(NulaTrack track) {
        ContentValues values = new ContentValues();
        values.put(EMP_ARTIST, track.getArtist());
        values.put(EMP_TITLE, track.getTitle());
        values.put(EMP_IMAGE, track.getImage());
        return database.insert(EMP_TABLE, null, values);
    }

    public List<NulaTrack> selectAllTracks() {
        String[] cols = new String[]{EMP_ID, EMP_ARTIST, EMP_TITLE, EMP_IMAGE};
        Cursor mCursor = database.query(true, EMP_TABLE, cols, null
                , null, null, null, null, null);

        List<NulaTrack> result = new ArrayList<>();

        if (mCursor != null) {
            if (mCursor.moveToFirst()) {
                do {
                    int id = mCursor.getInt(0);
                    String artist = mCursor.getString(1);
                    String title = mCursor.getString(2);
                    String image = mCursor.getString(3);

                    NulaTrack track = new NulaTrack(artist, title, image, id);
                    result.add(track);
                } while (mCursor.moveToNext());
            }
        }
        return result;
    }

    public long remoteTrack(NulaTrack track){
        return database.delete(EMP_TABLE,"_id=?",new String[]{""+track.getId()});
    }

}
