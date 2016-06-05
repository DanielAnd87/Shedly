package com.glenn.hatter.Shedly.model;

import android.app.Instrumentation;
import android.content.Context;
import android.test.InstrumentationTestCase;
import android.test.InstrumentationTestRunner;

import com.glenn.hatter.Shedly.constants.Constants;
import com.glenn.hatter.Shedly.data.DayBook;
import com.glenn.hatter.Shedly.data.Event;

import org.junit.Before;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by hatter on 2016-02-14.
 */
public class checkpointCalculatorTest extends InstrumentationTestCase {

    private DayBook daybook;
    ArrayList<Event> mScheckpoints;
    private Instrumentation mInstrumentation;
    private Context mContext;

    @Before
    public void setUp() throws Exception {
        mInstrumentation = this.getInstrumentation();
        mContext = mInstrumentation.getContext();


        daybook = new DayBook();
        Event[] mScheckpoints = daybook.mEvents;
        Event[] mStartStop = daybook.mEvents;


        int i = 0;
        for (Event mScheckpoint : mScheckpoints) {
            mScheckpoint.setTime(Constants.HOUR * i);
            i++;
        }
    }


}