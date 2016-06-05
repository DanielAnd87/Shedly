package com.glenn.hatter.Shedly.model;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.glenn.hatter.Shedly.R;
import com.glenn.hatter.Shedly.constants.Constants;
import com.glenn.hatter.Shedly.data.Event;
import com.glenn.hatter.Shedly.data.db.EventDataSource;

import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by hatter on 2016-02-09.
 */
public class SortEventFromDb {

    private final EventDataSource mDatasource;
    private final Context mContext;

    private Calendar mCalendar;

    private ArrayList<Event> mEvent = new ArrayList<>();
    private ArrayList<Event> mRemovedEvents = new ArrayList<>();
    private ArrayList<Event> mReplaceList = new ArrayList<>();
    private ArrayList<Event> mBinedFixedEvents = new ArrayList<>();

    public SortEventFromDb(Context context, EventDataSource datasource) {
        mDatasource = datasource;
        mContext = context;
    }


    public void setCalendar(Calendar calendar) {
        // The date has to be set or the sort method won't run.
        mCalendar = calendar;
    }

    public ArrayList<Event> getEvent() {
        return mEvent;
    }

    public ArrayList<Event> getRemovedEvents() {
        return mRemovedEvents;
    }

    public ArrayList<Event> getReplaceList() {
        return mReplaceList;
    }

    public ArrayList<Event> getBinedFixedEvents() {
        return mBinedFixedEvents;
    }

    public ArrayList<Event> sort(ArrayList<Event> list) {
        mRemovedEvents.addAll(list);
        // Adding doubles in replacelist so it is one event for the time and one to show.
        for (Event event : list) {
            mReplaceList.add(mReplaceList.size(), event);
            mReplaceList.add(mReplaceList.size(), event);
        }
        mEvent = mDatasource.readNote(mCalendar, mReplaceList);
        for (int i = 0; i < mReplaceList.size(); i++) {
            mReplaceList.remove(i);
        }

        if (mEvent.size() > 0) {
            // Removing duplicates that are in both list.
            for (int i = 0; i < mRemovedEvents.size(); i++) {
                Event current = mRemovedEvents.get(i);
                if (mEvent.contains(current)) {
                    mRemovedEvents.remove(current);
                    i--;
                }
                if (mReplaceList.contains(current)) {
                    mRemovedEvents.remove(current);
                    i--;
                }
            }

            // Sorting fixed times into the Bin-list.
            for (int i = 0; i < mReplaceList.size(); i++) {
                Event current = mReplaceList.get(i);
                if (current.isFixedTime()) {
                    mBinedFixedEvents.add(current);
                    mReplaceList.remove(i);
                    i--;
                }
            }
            Toast.makeText(mContext, R.string.saved_day_message, Toast.LENGTH_SHORT).show();
            return mEvent;
        } else {
            return mEvent;
        }
    }

    public void saveDate() {
        mDatasource.saveDate(mCalendar, mEvent, mReplaceList);
    }

    public void changeStartTime(List<Event> list) {
        Integer currentHour = mCalendar.get(Calendar.HOUR_OF_DAY);
        Integer currentMinute = mCalendar.get(Calendar.MINUTE);
        String name = mContext.getString(R.string.morning_greeting_label);
        int morningDuration = currentHour * Constants.HOUR + currentMinute * Constants.MINUTE;

        if (morningDuration > 12 * Constants.HOUR && morningDuration < 20 * Constants.HOUR) {
            name = mContext.getString(R.string.eventing_greeting_label);
        } else if (morningDuration > 20 * Constants.HOUR && morningDuration < 24 * Constants.HOUR) {
            name = mContext.getString(R.string.evening_greeting_label);
        }

        int y = 0;
        for (int i = 0; i < list.size() && y < 2; i++) {
            if (list.get(i).getId() == Constants.MORNING_ID) {
                list.get(i).setDuration(morningDuration);
                list.get(i).setName(name);


                y++;
            }
        }

        if (morningDuration > Constants.HOUR * 22) {
            int t = 0;
            for (int i = 0; i < list.size() && t < 2; i++) {
                if (list.get(i).getId() == 2) {
                    list.get(i).setFixedTime(morningDuration + 30 * Constants.MINUTE);
                    list.get(i).setName(name);


                    t++;
                }
            }

        }
    }
}
