package com.glenn.hatter.Shedly.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.provider.BaseColumns;
import android.util.Log;

import com.glenn.hatter.Shedly.constants.Constants;
import com.glenn.hatter.Shedly.data.Event;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by Evan Anger on 8/17/14.
 */
public class EventDataSource {

    private static final String TAG = EventDataSource.class.getSimpleName();
    private int J = 1;
    private int N = 0;

    private EventSQLiteHelper mEventSqlLiteHelper;

    public EventDataSource(Context context) {
        mEventSqlLiteHelper = new EventSQLiteHelper(context);
    }

    private SQLiteDatabase open() {
        return mEventSqlLiteHelper.getWritableDatabase();
    }

    private void close(SQLiteDatabase database) {
        database.close();
    }

    public int delete(int eventId) {
        SQLiteDatabase database = open();
        database.beginTransaction();
        // implementation details
        int numRows = database.delete(EventSQLiteHelper.EVENT_TABLE,
                //String.format("%s=%s", BaseColumns._ID, String.valueOf(eventId)),
                BaseColumns._ID + " = ?",
                new String[]{String.valueOf(eventId)});
        database.setTransactionSuccessful();
        database.endTransaction();
        Log.i(TAG, "delete Deleted " + numRows + " number of events");
        return numRows;

    }

    public int deleteNote(String date) {
        SQLiteDatabase database = open();
        database.beginTransaction();
        int numRows = database.delete(EventSQLiteHelper.NOTE_TABLE,
                EventSQLiteHelper.COLUMN_NOTE_DATE +" = ?",
                new String[]{date});
        database.setTransactionSuccessful();
        database.endTransaction();
        return numRows;
    }

    public ArrayList<Event> read() {

        return readEvents();
    }

    public void update(Event event) {
        SQLiteDatabase database = open();
        database.beginTransaction();
        ContentValues updateEventValues = new ContentValues();
        updateEventValues.put(EventSQLiteHelper.COLUMN_EVENT_NAME, event.getName());
        updateEventValues.put(EventSQLiteHelper.COLUMN_EVENT_DURATION, event.getDuration());
        updateEventValues.put(BaseColumns._ID, event.getId());

        updateEventValues.put(EventSQLiteHelper.COLUMN_EVENT_FREE_TIME, event.getFixedTime());
        updateEventValues.put(EventSQLiteHelper.COLUMN_EVENT_REPEAT, event.getRecurring());
        updateEventValues.put(EventSQLiteHelper.COLUMN_EVENT_TIMESTAMP, event.getStartTimeStamp());
        updateEventValues.put(EventSQLiteHelper.COLUMN_EVENT_TRAVELTIME_FROM, event.getTravelDurationFrom());
        updateEventValues.put(EventSQLiteHelper.COLUMN_EVENT_TRAVELTIME_TO, event.getTravelDurationTo());
        updateEventValues.put(EventSQLiteHelper.COLUMN_EVENT_COLOR, event.getColor());

        database.update(EventSQLiteHelper.EVENT_TABLE,
                updateEventValues,
                BaseColumns._ID + " = ?",
                new String[]{String.valueOf(event.getId())});

        database.setTransactionSuccessful();
        database.endTransaction();
        close(database);
    }

    public ArrayList<Event> readEvents() {
        SQLiteDatabase database = open();

        Cursor cursor = database.query(
                EventSQLiteHelper.EVENT_TABLE,
                //  I could just pass null here since I want everything from the table.
                new String[]{EventSQLiteHelper.COLUMN_EVENT_NAME, BaseColumns._ID, EventSQLiteHelper.COLUMN_EVENT_FREE_TIME, EventSQLiteHelper.COLUMN_EVENT_COLOR, EventSQLiteHelper.COLUMN_EVENT_DURATION, EventSQLiteHelper.COLUMN_EVENT_REPEAT, EventSQLiteHelper.COLUMN_EVENT_TIMESTAMP, EventSQLiteHelper.COLUMN_EVENT_FIXEDTIME, EventSQLiteHelper.COLUMN_EVENT_TRAVELTIME_FROM, EventSQLiteHelper.COLUMN_EVENT_TRAVELTIME_TO},
                null, // selection
                null, // selection args
                null, // group by
                null, // having
                null); // order

        ArrayList<Event> events = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int eventDuration = getIntFromColumnName(cursor, EventSQLiteHelper.COLUMN_EVENT_DURATION);
                int eventFixedTime = getIntFromColumnName(cursor, EventSQLiteHelper.COLUMN_EVENT_FIXEDTIME);
                int eventTravelDurTo = getIntFromColumnName(cursor, EventSQLiteHelper.COLUMN_EVENT_TRAVELTIME_TO);
                int eventTravelDurFrom = getIntFromColumnName(cursor, EventSQLiteHelper.COLUMN_EVENT_TRAVELTIME_FROM);
                int eventColor = getIntFromColumnName(cursor, EventSQLiteHelper.COLUMN_EVENT_COLOR);

                // TODO: 2015-09-23 add color
                int event_id = getIntFromColumnName(cursor, BaseColumns._ID);
                long event_timeStamp = getIntFromColumnName(cursor, EventSQLiteHelper.COLUMN_EVENT_TIMESTAMP);
                String event_repeat = getStringFromColumnName(cursor, EventSQLiteHelper.COLUMN_EVENT_REPEAT);

                String eventName = getStringFromColumnName(cursor, EventSQLiteHelper.COLUMN_EVENT_NAME);
                Event event = new Event(eventName,
                        eventDuration);
                if (event_timeStamp > 0) {
                    event.setStartTimeStamp((int) event_timeStamp);
                    event.setRecurring(event_repeat);
                }
                // Checking if the event has a fixedTime and then it will be added.
                if (eventFixedTime > 0) {
                    event.setFixedTime(eventFixedTime);
                }
                if (eventTravelDurTo > 0) {
                    event.setTravelDurationTo(eventTravelDurTo);
                }
                if (eventTravelDurFrom > 0) {
                    event.setTravelDurationTo(eventTravelDurFrom);
                }
                event.setColor(eventColor);
                event.setId(event_id);
                events.add(event);
            } while (cursor.moveToNext());
        }
        cursor.close();
        close(database);
        return events;
    }

    public ArrayList<Event> readNote(Calendar calendar, ArrayList<Event> events) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = calendar.getTime();
        String noteDate = dateFormat.format(date);
        SQLiteDatabase database = open();
        Cursor cursor = database.query(
                EventSQLiteHelper.NOTE_TABLE,
                new String[]{EventSQLiteHelper.COLUMN_NOTE_DATE, EventSQLiteHelper.COLUMN_NOTE_TIME, EventSQLiteHelper.COLUMN_NOTE_KEY},
                //  String.format("%s=%s", EventSQLiteHelper.COLUMN_NOTE_DATE, noteDate),
                EventSQLiteHelper.COLUMN_NOTE_DATE + " = ?",
                new String[]{noteDate},
                null,
                null,
                null
        );


        ArrayList<Event> noteEvents = new ArrayList<>();
        ArrayList<Event> noteReplace = new ArrayList<>();

        if (cursor.moveToNext()) {
            do {
                int noteTime = getIntFromColumnName(cursor, EventSQLiteHelper.COLUMN_NOTE_TIME);
                int noteId = getIntFromColumnName(cursor, EventSQLiteHelper.COLUMN_NOTE_KEY);
                Log.i(TAG, "readNote My friend Cursor got id-number " + noteId + " with the time of " + noteTime);

                if (noteId == -1) {
                    noteEvents.add(new Event("FreeTime", 1, true));
                    noteEvents.get(noteEvents.size() - 1).setTime(noteTime);
                    Log.i(TAG, "readNote Adding a freeTime.");
                } else {
                    boolean found = false;
                    for (int i = 0; i < events.size() && !found; i++) {
                        Log.i(TAG, "readNote Checking if event id " + noteId + " equals " + events.get(i).getId());
                        if (noteId == events.get(i).getId() && noteTime != 0 || noteId == 1) {
                            // setTime whit the value from the cursor.
                            events.get(i).setTime(noteTime);
                            // I find my event from events and add it to noteEvents.
                            noteEvents.add(events.get(i));
                            // I also delete the event from events.
                            events.remove(i);
                            found = true;
                            Log.i(TAG, "readNote Adding an Event.");
                            i--;
                        } else if (noteId == events.get(i).getId() && noteTime == 0 && noteId != 1) {
                            // if the event has no scheduled time and isn't the morning then it belongs in the replace-list.
                            noteReplace.add(events.get(i));
                        }
                    }
                }
                // Going to sett the duration for all freetime events.
            } while (cursor.moveToNext());
        }
        events.clear();
        events.addAll(noteReplace);
        for (Event event : events) {
            Log.i(TAG, "readNote Event list" + event.getName());
        }
        Collections.sort(noteEvents, new Comparator<Event>() {
            @Override
            public int compare(Event lhs, Event rhs) {
                return lhs.getTime() - rhs.getTime();
            }
        });

        // Setting the duration for all events.
        int duration = 0;
        for (int i = 0; i < noteEvents.size(); i++) {
            Event noteEvent = noteEvents.get(i);
                if (i < noteEvents.size() - 2) {
                    duration = noteEvents.get(i + 2).getTime() - noteEvent.getTime();
                } else {
                    duration = Constants.HOUR*24 - noteEvent.getTime();
                }
                noteEvent.setDuration(duration);
        }

        if (noteEvents.size() == 0) {
            // If I did'nt find any then I return the empty list.
            Log.i(TAG, "readNote Didn't find no Event!");
            return noteEvents;
        }
        return noteEvents;
        }

    private int getIntFromColumnName(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.getInt(columnIndex);
    }

    private String getStringFromColumnName(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.getString(columnIndex);
    }



    public int create(Event event) {
        SQLiteDatabase database = open();
        database.beginTransaction();

        ContentValues eventValues = new ContentValues();
        eventValues.put(EventSQLiteHelper.COLUMN_EVENT_NAME, event.getName());
        eventValues.put(EventSQLiteHelper.COLUMN_EVENT_COLOR, event.getColor());
        eventValues.put(EventSQLiteHelper.COLUMN_EVENT_DURATION, event.getDuration());
        if (event.isFreeTime()) {
            eventValues.put(EventSQLiteHelper.COLUMN_EVENT_FREE_TIME, J);
        } else {
            eventValues.put(EventSQLiteHelper.COLUMN_EVENT_FREE_TIME, N);
        }
        if (event.getStartTimeStamp() > 0) {
            eventValues.put(EventSQLiteHelper.COLUMN_EVENT_TIMESTAMP, event.getStartTimeStamp());
            eventValues.put(EventSQLiteHelper.COLUMN_EVENT_REPEAT, event.getRecurring());
        }
        if (event.isFixedTime()) {
            eventValues.put(EventSQLiteHelper.COLUMN_EVENT_FIXEDTIME, event.getFixedTime());
        }
        if (event.getTravelDurationTo() > 0) {
            eventValues.put(EventSQLiteHelper.COLUMN_EVENT_TRAVELTIME_TO, event.getTravelDurationTo());
        }
        if (event.getTravelDurationFrom() > 0) {
            eventValues.put(EventSQLiteHelper.COLUMN_EVENT_TRAVELTIME_FROM, event.getTravelDurationFrom());
        }


        int rowId = (int) database.insert((EventSQLiteHelper.EVENT_TABLE), null, eventValues);
        database.setTransactionSuccessful();
        database.endTransaction();
        close(database);
        return rowId;
    }

    public void saveDate(Calendar calendar, ArrayList<Event> list, ArrayList<Event> replaceList) {
        Log.i(TAG, "saveDate My mEvents contains: ");
        for (Event event : list) {
            Log.i(TAG, "saveDate " + event.getName() + " with id: " + event.getId());
        }
        Log.i(TAG, "saveDate My Replace-list contains: ");
        for (Event event : replaceList) {
            Log.i(TAG, "saveDate " + event.getName() + " with id: " + event.getId());
        }

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = calendar.getTime();
        String noteDate = dateFormat.format(date);
        Log.i(TAG, "saveDate Formating the date of " + noteDate);

        // Run an deleteDate method here.
        int numRows = deleteNote(noteDate);
        Log.i(TAG, "saveDate Delete " + numRows + " number of notes, before I save new.");
        // Save everything with a loop here!
        SQLiteDatabase database = open();
        database.beginTransaction();
        int numNotes = 0;
        ContentValues noteValues = new ContentValues();
        for (int i = 0; i < list.size(); i++) {

            noteValues.put(EventSQLiteHelper.COLUMN_NOTE_DATE, noteDate);
            noteValues.put(EventSQLiteHelper.COLUMN_NOTE_TIME, list.get(i).getTime());
            noteValues.put(EventSQLiteHelper.COLUMN_NOTE_KEY, list.get(i).getId());
           // noteValues.put(EventSQLiteHelper.COLUMN_EVENT_COLOR, list.get(i).getColor());
            numNotes = (int) database.insert(EventSQLiteHelper.NOTE_TABLE, null, noteValues);

            Log.i(TAG, "saveDate Put the date of " + noteDate + " the time of " + list.get(i).getTime() + " and id of " + list.get(i).getId());
        }
        for (Event event : replaceList) {
            noteValues.put(EventSQLiteHelper.COLUMN_NOTE_DATE, noteDate);
            noteValues.put(EventSQLiteHelper.COLUMN_NOTE_KEY, event.getId());
            noteValues.put(EventSQLiteHelper.COLUMN_NOTE_TIME, 0);


            Log.i(TAG, "saveDate Put the date of " + noteDate + " the time of " + event.getTime() + " and id of " + event.getId());

            numNotes = (int) database.insert(EventSQLiteHelper.NOTE_TABLE, null, noteValues);

        }
        Log.i(TAG, "saveDate Insert " + numNotes + " number of notes in the Note table");
        database.setTransactionSuccessful();
        database.endTransaction();
    }
}
