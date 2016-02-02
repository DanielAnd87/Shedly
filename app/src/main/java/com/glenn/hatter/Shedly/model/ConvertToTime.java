package com.glenn.hatter.Shedly.model;

import com.glenn.hatter.Shedly.constants.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by hatter on 2015-10-06.
 */
public class ConvertToTime {
    public static String convertToTime(int duration) {
        int minutes = duration/ Constants.MINUTE;
        int hours = minutes / 60;

        int minutesOverHour;
        if (duration > 0) {
            minutesOverHour = minutes - hours*60;
        }
        else {
            minutesOverHour = 0;
        }


        Date date = null;
        try {
            date = new SimpleDateFormat("H:m").parse(String.format("%s:%s", hours + "", minutesOverHour + ""));
        } catch (ParseException e) {
            e.printStackTrace();
        }


        String time = new SimpleDateFormat("HH:mm").format(date);
        return time;
    }

    public static int getHour(int duration) {
        int minutes = duration/ Constants.MINUTE;
        int hours = minutes / 60;

        int minutesOverHour;
        if (duration > 0) {
            minutesOverHour = minutes - hours*60;
        }
        else {
            minutesOverHour = 0;
        }
        return hours;
    }
    public static int getMinute(int duration) {
        int minutes = duration/ Constants.MINUTE;
        int hours = minutes / 60;

        int minutesOverHour;
        if (duration > 0) {
            minutesOverHour = minutes - hours*60;
        }
        else {
            minutesOverHour = 0;
        }
        return minutesOverHour;
    }
}
