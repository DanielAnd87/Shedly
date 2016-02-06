package com.glenn.hatter.Shedly.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by hatter on 2015-09-01.
 */


public class EventSQLiteHelper extends SQLiteOpenHelper {

    // If I change the database schema I must increment the database version.
    //46
    private static final int DATABASE_VERSION = 48;

    public static final String DATABASE_NAME = "event.db";


    public static final String EVENT_TABLE = "EVENTS";
    public static final String _ID = "EVENT_ID";
    // As of now I don't plan to implement any other tables and don't know what they would be called! No foreign key needed for now.
    // public static final String COLUMN_FOREGN_KEY = "event_id";
    public static final String COLUMN_EVENT_NAME = "NAME";
    public static final String COLUMN_EVENT_DURATION = "DURATION";
    public static final String COLUMN_EVENT_FIXEDTIME = "FIXED_TIME";
    // Is the fixedTime it pointing at. Quite confusing.
    public static final String COLUMN_EVENT_FREE_TIME = "FREETIME";
    public static final String COLUMN_EVENT_TIMEZONE_MIN = "TIMEZONE_MIN";
    public static final String COLUMN_EVENT_TIMEZONE_MAX = "TIMEZONE_MAX";
    public static final String COLUMN_EVENT_COLOR = "COLOR";
    public static final String COLUMN_EVENT_TRAVELTIME_TO = "TRAVELTIME_TO";
    public static final String COLUMN_EVENT_TRAVELTIME_FROM = "TRAVELTIME_FROM";
    public static final String COLUMN_EVENT_TIMESTAMP = "TIMESTAMP";
    public static final String COLUMN_EVENT_REPEAT = "REPEAT";

    public static final String ALTER_ADD_CREATE_TRAVELTIME_TO = "ALTER TABLE " + EVENT_TABLE + " ADD COLUMN " + COLUMN_EVENT_TRAVELTIME_TO + " INTEGER";
    public static final String ALTER_ADD_CREATE_TRAVELTIME_FROM = "ALTER TABLE " + EVENT_TABLE + " ADD COLUMN " + COLUMN_EVENT_TRAVELTIME_FROM + " INTEGER";


    public static final String NOTE_TABLE = "NOTE";
    public static final String COLUMN_NOTE_DATE = "DATE";
    public static final String COLUMN_NOTE_TIME = "TIME";
    public static final String COLUMN_NOTE_KEY = "MEME_ID";

    public static final String CREATE_NOTE = "CREATE TABLE " +
            NOTE_TABLE + " (" + BaseColumns._ID +
            " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_NOTE_DATE + " TEXT," +
            COLUMN_NOTE_TIME + " INTEGER," +
            COLUMN_NOTE_KEY + " INTEGER"
            + ")";


    private static final String CREATE_EVENT = "CREATE TABLE " +
            EVENT_TABLE + " (" + BaseColumns._ID +
            " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_EVENT_NAME + " TEXT, " +
            COLUMN_EVENT_COLOR + " INTEGER, " +
            COLUMN_EVENT_DURATION + " INTEGER, " +
            COLUMN_EVENT_TIMEZONE_MIN + " INTEGER, " +
            COLUMN_EVENT_TIMEZONE_MAX + " INTEGER, " +
            COLUMN_EVENT_FREE_TIME + " INTEGER, " +
            COLUMN_EVENT_TRAVELTIME_FROM + " INTEGER, " +
            COLUMN_EVENT_TRAVELTIME_TO + " INTEGER, " +
            COLUMN_EVENT_TIMESTAMP + " INTEGER," +
            COLUMN_EVENT_FIXEDTIME + " INTEGER," +
            COLUMN_EVENT_REPEAT + " TEXT" +
            ")";






    public EventSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_EVENT);
        db.execSQL(CREATE_NOTE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
/*
        db.execSQL("DROP TABLE IF EXISTS " + EVENT_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + NOTE_TABLE);
        onCreate(db);
*/
        if (oldVersion == 46) {
            // TODO: 2015-10-31 Do nothing or all my own events will fuck up!!!
        }
/*
        if (oldVersion < 46) {
            db.execSQL(ALTER_ADD_CREATE_TRAVELTIME_FROM);
            db.execSQL(ALTER_ADD_CREATE_TRAVELTIME_TO);
        }
*/
    }
}
