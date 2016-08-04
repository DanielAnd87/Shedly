package com.glenn.hatter.Shedly.model;

import com.glenn.hatter.Shedly.constants.Constants;
import com.glenn.hatter.Shedly.data.DayBook;
import com.glenn.hatter.Shedly.data.Event;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Created by hatter on 2016-08-03.
 */
public class ScheduleDayTest {


    private DayBook daybook;
    ArrayList<Event> mScheckpoints;
    boolean[] checkedBoxes = {true, true, true, true, true, false};
    private boolean[] checkedBoxes2 = {true, true, false, true, true, false};

    private ScheduleDay mScheduleDay;

    @Before
    public void setUp() throws Exception {
        daybook = new DayBook();
        mScheckpoints = new ArrayList<>();
        Event[] mPoints = daybook.mEvents;
        Event[] mStartStop = daybook.startAndStop;


        Collections.addAll(mScheckpoints, mPoints);

        mScheckpoints.add(0, mStartStop[0]);
        mScheckpoints.add(mScheckpoints.size(), mStartStop[1]);

        mScheduleDay = new ScheduleDay(mScheckpoints);
        mScheckpoints = mScheduleDay.getEvent();
    }



    @Test
    public void isMorningFirst() throws Exception {
        assertEquals("first", Constants.MORNING_ID, mScheckpoints.get(0).getId());
        assertEquals("second", Constants.MORNING_ID, mScheckpoints.get(1).getId());
    }

    @Test
    public void fixedTimesOnRightTime() throws Exception {
        for (Event scheckpoint : mScheckpoints) {
            if (scheckpoint.isFixedTime() && scheckpoint.getId() != Constants.MORNING_ID) {
                assertEquals(scheckpoint.getFixedTime(), scheckpoint.getTime());
            }
        }
    }

    @Test
    public void containsImportantEvents() throws Exception {
        assertEquals("Morning", true, mScheckpoints.contains(daybook.startAndStop[0]));
        assertEquals("Night", true, mScheckpoints.contains(daybook.startAndStop[1]));
    }

    @Test
    public void isNightLast() throws Exception {
        int lastIndex = mScheckpoints.size() - 1;
        assertEquals("first", Constants.NIGHT_ID, mScheckpoints.get(lastIndex).getId());
        assertEquals("second", Constants.NIGHT_ID, mScheckpoints.get(lastIndex - 1).getId());
    }

    @Test
    public void isAllDoublesInRightPlace() throws Exception {
        for (int i = 0; i < mScheckpoints.size(); i += 2) {
            assertEquals(mScheckpoints.get(i).getName(), mScheckpoints.get(i+1).getName());
        }
    }

    @Test
    public void isLaterEventsAtLaterTime() throws Exception {
        int currentTime = -1;
        for (int i = 0; i < mScheckpoints.size(); i+=2) {
            int time = mScheckpoints.get(i).getTime();
            assertEquals(true, currentTime < time);
            currentTime = time;
        }
    }

    @Test
    public void isNextEventExactlyAfterTheOneBefore() throws Exception {
        int currentTime = 0;
        int currentLasting = 0;
        for (int i = 0; i < mScheckpoints.size(); i+=2) {
            int time = mScheckpoints.get(i).getTime();
            int duration = mScheckpoints.get(i).getDuration();
            assertEquals(time, ((currentTime + currentLasting)));
            currentTime = time;
            currentLasting = duration;
        }
    }

    @Test
    public void correctMinutesPerDay() throws Exception {
        int duration=0;
        for (int i = 0; i < mScheckpoints.size(); i += 2) {
            duration += mScheckpoints.get(i).getDuration();
        }
        assertEquals(Constants.HOUR*24, duration);
    }
}