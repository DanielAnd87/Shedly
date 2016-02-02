package com.glenn.hatter.Shedly.model;

import android.util.Log;

import com.glenn.hatter.Shedly.constants.Constants;
import com.glenn.hatter.Shedly.data.Event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * Created by hatter on 2015-07-05.
 */
public class SortEvent {
    private static final String TAG = SortEvent.class.getSimpleName();
    private ArrayList<Event> mEvents;
    private ArrayList<Event> mRepeatingEvents = new ArrayList<>();
    private Calendar mChosenDate;

    private int mYear;
    private int mMonth;
    private int mDay;
    private int mWeekDay;


    public  SortEvent(ArrayList<Event> eventList) {
        // Whant a simpledateFormater that finds out what mDay, week, and maybe mMonth it is.

        mEvents = eventList;
        for (int i = 0; i < mEvents.size(); i++) {
            if (mEvents.get(i).getStartTimeStamp() > 0) {
                Log.i(TAG, "Found out that "+mEvents.get(i).getName()+" is a repeating Event and will sort it.");
                        mRepeatingEvents.add(mEvents.get(i));
                mEvents.remove(i);
                i--;
            }
        }
        mChosenDate = Calendar.getInstance();

        mYear = mChosenDate.get(Calendar.YEAR);
        mMonth = mChosenDate.get(Calendar.MONTH);
        mDay = mChosenDate.get(Calendar.DAY_OF_MONTH);
        mWeekDay = mChosenDate.get(Calendar.DAY_OF_WEEK);
        sortRepeatingEvents();
    }
    public SortEvent(Calendar calendar, ArrayList<Event> eventList) {
        // Whant a simpledateFormater that finds out what mDay, week, and maybe mMonth it is.

        mEvents = eventList;
        for (int i = 0; i < mEvents.size(); i++) {
            if (mEvents.get(i).getStartTimeStamp() > 0) {
                Log.i(TAG, "Found out that "+mEvents.get(i).getName()+" is a repeating Event and will sort it.");
                        mRepeatingEvents.add(mEvents.get(i));
                mEvents.remove(i);
                i--;
            }
        }
        mChosenDate = calendar;
        mYear = mChosenDate.get(Calendar.YEAR);
        mMonth = mChosenDate.get(Calendar.MONTH);
        mDay = mChosenDate.get(Calendar.DAY_OF_MONTH);
        mWeekDay = mChosenDate.get(Calendar.DAY_OF_WEEK);

        sortRepeatingEvents();
    }

    private Calendar getCalendar(Event event) {
        Log.i(TAG, "The timeStamp of " + event.getName() + " is " + event.getStartTimeStamp());
        Calendar calendar = Calendar.getInstance();
        long timeStamp = event.getStartTimeStamp();
        Date date = new Date(timeStamp*1000);
        calendar.setTime(date);
        return calendar;
    }

    public ArrayList<Event> getEvents() {
        return mEvents;
    }

    private void sortRepeatingEvents() {

        for (Event event : mRepeatingEvents) {
            Calendar startDate = getCalendar(event);
            // Making one of four checks to see if it is in the correct time and will add it if it is.
            Log.i(TAG, "Checking if " + event.getName() + " is occurring today.");
            if (Objects.equals(event.getRecurring(), Constants.WEEKLY)) {
                if (findInterval(startDate, 1, Calendar.WEEK_OF_YEAR)) {
                    mEvents.add(event);
                }

            }
            else if (Objects.equals(event.getRecurring(), Constants.BI_WEEKLY)) {
                if (findInterval(startDate, 2, Calendar.WEEK_OF_YEAR)) {
                    mEvents.add(event);
                }

            }
            else if (Objects.equals(event.getRecurring(), Constants.MONTLY)) {
                if (findInterval(startDate, 1, Calendar.MONTH)) {
                    mEvents.add(event);
                }

            }
            else if (Objects.equals(event.getRecurring(), Constants.ONCE)) {
                int dayT = startDate.get(Calendar.DAY_OF_MONTH);
                int yearT = startDate.get(Calendar.YEAR);
                int monthT = startDate.get(Calendar.MONTH);

                if (mDay == dayT && yearT == mYear && monthT == mMonth) {
                    mEvents.add(event);
                }


            }
            else if (Objects.equals(event.getRecurring(), Constants.FOURTH_WEEK)) {
                if (findInterval(startDate, 4, Calendar.WEEK_OF_YEAR)) {
                    mEvents.add(event);
                }

            }
            else if (Objects.equals(event.getRecurring(), Constants.WEEKDAYS)) {
                // Checking if todays date is a weeday
                if (mWeekDay > Calendar.SUNDAY && mWeekDay < Calendar.SATURDAY) {
                    mEvents.add(event);
                }
            }
            else if (Objects.equals(event.getRecurring(), Constants.WEEKENDS)) {
                // Checking if todays date is a weekend
                if (mWeekDay == Calendar.SATURDAY || mWeekDay == Calendar.SUNDAY) {
                    mEvents.add(event);
                }

            }

        }
    }

    public boolean findInterval(Calendar startDate, int interval, int dateTime) {
        // Will add days to the Calendar until it passes current date. If it is a mach by then it return true;
        if (mChosenDate.compareTo(startDate) == 0) {
            return true;
        }
        int compareTo = startDate.compareTo(mChosenDate);
        Log.i(TAG, "");
        Log.i(TAG, "The comparing result " + compareTo);
        Log.i(TAG, "");


        int yearT = startDate.get(Calendar.YEAR);
        int monthT = startDate.get(Calendar.MONTH);
        int dayT = startDate.get(Calendar.DAY_OF_MONTH);

        if (yearT == mYear && mDay == dayT && mMonth == monthT) {
            Log.i(TAG, "Found a match right here!");
            return true;
        }

        while (startDate.compareTo(mChosenDate) < 0) {
            compareTo = startDate.compareTo(mChosenDate);
            Log.i(TAG, "");
            Log.i(TAG, "The comparing result " + compareTo);
            Log.i(TAG, "");

            startDate.add(dateTime, interval);
            yearT = startDate.get(Calendar.YEAR);
            monthT = startDate.get(Calendar.MONTH);
            dayT = startDate.get(Calendar.DAY_OF_MONTH);

            if (yearT == mYear && mDay == dayT && mMonth == monthT) {
                Log.i(TAG, "Found a match right here!");
                return true;
            }
        }
        Log.i(TAG, "Not a match!");
        return false;
    }
}
