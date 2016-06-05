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
 * Created by hatter on 2016-02-24.
 */
public class QueryHandlerTest {
    private DayBook daybook;
    ArrayList<Event> mScheckpoints;
    boolean[] checkedBoxes = {true, true, true, true, true, false};
    private QueryHandler mQueryHandler;
    private boolean[] checkedBoxes2 = {true, true, false, true, true, false};

    @Before
    public void setUp() throws Exception {
        daybook = new DayBook();
        mScheckpoints = new ArrayList<>();
        Event[] mPoints = daybook.mEvents;
        Event[] mStartStop = daybook.startAndStop;


        Collections.addAll(mScheckpoints, mPoints);

        mScheckpoints.add(0, mStartStop[0]);
        mScheckpoints.add(mScheckpoints.size(), mStartStop[1]);
        mScheckpoints.add(mScheckpoints.size(), new Event("free", Constants.HOUR, true));


        int i = 0;
        for (Event mScheckpoint : mScheckpoints) {
            mScheckpoint.setTime(Constants.HOUR * i);
            i++;
        }

        for (int i1 = 0; i1 < mScheckpoints.size(); i1++) {
            mScheckpoints.add(i1, mScheckpoints.get(i1));
            i1++;
        }

        mQueryHandler = new QueryHandler(mScheckpoints);
    }


    @Test
    public void checkedCorrectBoxes() throws Exception {
        int time = (Constants.HOUR * 4) + (Constants.MINUTE * 5);

        // Start callculate when the morning time stops. Allas the starttime is the duration of the morningtime.

        boolean[] checkedBoxesFromMethod = mQueryHandler.getCheckedBoxes(time);

        for (int i = 0; i < checkedBoxes.length; i++) {
            assertEquals(i + " box checked?", checkedBoxes[i], checkedBoxesFromMethod[i]);
        }

    }


    @Test
    public void minutesAhead() throws Exception {
        int time = (Constants.HOUR * 5) + (Constants.MINUTE * 5);
        int scheduleStatus = mQueryHandler.setScheduleStatus(checkedBoxes, time);
        int scheduleStatus2 = mQueryHandler.setScheduleStatus(checkedBoxes2, time);
        assertEquals(Constants.MINUTE * (-5), scheduleStatus);
        assertEquals(Constants.MINUTE * (-5) + Constants.HOUR * -1, scheduleStatus2);

    }


    @Test
    public void combinedMinutesOfFreeTime() throws Exception {
        int combiedFreetime = mQueryHandler.getCombinedFreeTimeLeft(mScheckpoints);

        assertEquals(Constants.HOUR, combiedFreetime);
    }

}