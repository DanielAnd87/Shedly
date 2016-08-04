package com.glenn.hatter.Shedly.data;

import android.content.Context;

import com.glenn.hatter.Shedly.constants.Constants;

/**
 * Created by hatter on 2015-11-02.
 */
public class DayBook {

    public DayBook() {
        startAndStop[0].setId(Constants.MORNING_ID);
        startAndStop[1].setId(Constants.NIGHT_ID);
    }

    public Event[] mEvents = {
            new Event(
                    "Breakfast",
                    10 * Constants.MINUTE
            ),
            new Event(
                    "Lunch",
                    10 * Constants.MINUTE
            ),
            new Event(
                    "Supper",
                    10 * Constants.MINUTE
            ),
            new Event(
                    "FixedTime",
                    2 * Constants.HOUR
                    , Constants.HOUR, 0, ""
            ),
            new Event(
                    "FixedTime",
                    10 * Constants.MINUTE
                    , Constants.HOUR*2, 0, ""
            ),
            new Event(
                    "FixedTime",
                    20 * Constants.MINUTE
                    , Constants.HOUR*23, 0, ""
            ),
            new Event(
                    "FixedTime",
                    10 * Constants.MINUTE
                    , Constants.HOUR*23+10 * Constants.MINUTE, 0, ""
            ),
            new Event(
                    "Clean-up",
                    10 * Constants.MINUTE
            ),
            new Event(
                    "Morning hygiene",
                    10 * Constants.MINUTE
            ),


    };

    public Event[] startAndStop = {
            new Event(
                    "",
                    7 * Constants.HOUR,
                    1),
            new Event(
                    "",
                    2 * Constants.HOUR,
                    22 * Constants.HOUR)
    };



}
