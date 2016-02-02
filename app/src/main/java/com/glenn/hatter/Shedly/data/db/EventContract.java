package com.glenn.hatter.Shedly.data.db;

import android.provider.BaseColumns;

/**
 * Created by hatter on 2015-09-01.
 */
public class EventContract {


    public static final class EventEntry implements BaseColumns {
        public static final String TABLE_NAME = "event";
        public static final String _ID = "_id";
        // As of now I don't plan to implement any other tables and don't know what they would be called! No foreign key needed for now.
       // public static final String COLUMN_FOREGN_KEY = "event_id";
        public static final String COLUMN_EVENT_NAME = "name";
        public static final String COLUMN_DURATION = "duration";
        public static final String COLUMN_TIME = "time";
        public static final String COLUMN_FREE_TIME = "freetime";
        // TODO: 2015-09-01 Set mChosenToBeRemoved and mInSelectedArea as false.
        public static final String COLUMN_COLOR = "color";
    }
}
