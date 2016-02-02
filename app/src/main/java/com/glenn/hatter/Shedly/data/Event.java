package com.glenn.hatter.Shedly.data;

import android.graphics.Color;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by hatter on 2015-06-01.
 */
public class Event {
    private int mId = -1;

    private String mName;
    private int mDuration;
    private int mTime;
    private boolean mChosenToBeRemoved;
    private int mColor;
    private int mFixedTime;
    private int mTravelDurationTo;
    private int mTravelDurationFrom;
    private boolean mFreeTime;
    private boolean mInSelectedArea;
    private String mRecurring;
    private long mStartTimeStamp;

    public int getTravelDurationTo() {

        return mTravelDurationTo;
    }

    public void setTravelDurationTo(int travelDurationTo) {
        mTravelDurationTo = travelDurationTo;
    }

    public int getTravelDurationFrom() {
        return mTravelDurationFrom;
    }

    public void setTravelDurationFrom(int travelDurationFrom) {
        mTravelDurationFrom = travelDurationFrom;
    }




    public boolean isFixedTime() {
        return mFixedTime > 0;
    }


    // Ordinary
    public Event(String name, int duration) {
        mName = name;
        mDuration = duration;
        mChosenToBeRemoved = false;
        mInSelectedArea = false;
        mColor = Color.parseColor("#0088AA");
        mFreeTime = false;
    }
    // Ordinary
    public Event(int id, String name, int duration) {
        mName = name;
        mId = id;
        mDuration = duration;
        mChosenToBeRemoved = false;
        mInSelectedArea = false;
        mColor = Color.parseColor("#0088AA");
        mFreeTime = false;
    }

    // FixedTime
    public Event(String name, int duration, int fixedTime) {
        mName = name;
        mDuration = duration;
        mFixedTime = fixedTime;
        mChosenToBeRemoved = false;
        mInSelectedArea = false;
        mColor = Color.parseColor("#FF7F2A");
        mFreeTime = false;
    }
    // Fixed time that repeate
    public Event(String name, int duration, int fixedTime, long timeStamp, String recurring) {
        mName = name;
        mDuration = duration;
        mFixedTime = fixedTime;
        mChosenToBeRemoved = false;
        mInSelectedArea = false;
        if (fixedTime > 0) {
            mColor = Color.parseColor("#FF7F2A");
        } else {
            mColor = Color.parseColor("#0088AA");
        }
        mFreeTime = false;
        mStartTimeStamp = timeStamp;
        mRecurring = recurring;


    }
    // freetime
    public Event(String name, int duration, boolean freeTime) {
        mName = name;
        mDuration = duration;
        mChosenToBeRemoved = false;
        mInSelectedArea = false;
        mColor = Color.parseColor("#00BCD4");
        mFreeTime = freeTime;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public boolean hasBeenSaved() { return (getId() != -1); }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        mColor = color;
    }


    public boolean getIsInSelectedArea() {
        return mInSelectedArea;
    }

    public void setInSelectedArea(boolean inSelectedArea) {
        mInSelectedArea = inSelectedArea;
    }

    public boolean isFreeTime() {
        return mFreeTime;
    }
    public int getFixedTime() {
        return mFixedTime;
    }

    public void setFixedTime(int fixedTime) {
        mFixedTime = fixedTime;
    }





    public int getTime() {
        return mTime;
    }

    public void setTime(int time) {
        mTime = time;
    }

    public boolean isChosenToBeRemoved() {
        return mChosenToBeRemoved;
    }

    public void setChosenToBeRemoved(boolean chosenToBeRemoved) {
        mChosenToBeRemoved = chosenToBeRemoved;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getDuration() {
        return mDuration;
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }

    public String getRecurring() {
        return mRecurring;
    }

    public void setRecurring(String recurring) {
        mRecurring = recurring;
    }

    public long getStartTimeStamp() {
        return mStartTimeStamp;
    }
    public void setStartTimeStamp(int timeStamp) {
        mStartTimeStamp = timeStamp;
    }

    public void setStartTimeStamp() {
        // TODO: 2015-09-23 When the user can add events for future days then I will get the date from that instead.
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        long timeStamp = date.getTime()/1000;
        mStartTimeStamp = timeStamp;
    }
    public void setStartTimeStamp(Calendar calendar) {
        // TODO: 2015-09-23 When the user can add events for future days then I will get the date from that instead.
        Date date = calendar.getTime();
        long timeStamp = date.getTime()/1000;
        mStartTimeStamp = timeStamp;
    }
}
