package com.glenn.hatter.Shedly.data;

import android.content.Context;

import com.glenn.hatter.Shedly.constants.Constants;

/**
 * Created by hatter on 2015-11-02.
 */
public class DayBook {
    private Context mContext;

    public DayBook(Context context) {
        mContext = context;
    }

    public Event[] mEvents = {
            new Event(
                    "Breakfast",
                    10*Constants.MINUTE
            ),
            new Event(
                    "Lunch",
                    10*Constants.MINUTE
            ),
            new Event(
                    "Supper",
                    10*Constants.MINUTE
            ),
            new Event(
                    "Clean-up",
                    10*Constants.MINUTE
            ),
            new Event(
                    "Morning hygiene",
                    10*Constants.MINUTE
            ),



    };
    /*
    public Event[] startAndStop = {
            new Event(
                    mContext.getString(R.string.morning_greeting_label),
                    7 * Constants.HOUR,
                    1),
            new Event(
                    mContext.getString(R.string.night_greeting_label),
                    2 * Constants.HOUR,
                    22 * Constants.HOUR)
    };
    */


}
