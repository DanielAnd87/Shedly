package com.glenn.hatter.Shedly.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.glenn.hatter.Shedly.R;
import com.glenn.hatter.Shedly.adapters.StartEventsAdapter;
import com.glenn.hatter.Shedly.constants.Constants;
import com.glenn.hatter.Shedly.data.Event;
import com.glenn.hatter.Shedly.data.db.EventDataSource;
import com.getbase.floatingactionbutton.FloatingActionButton;


public class EventChoosingActivity extends Activity {

    private EventDataSource mDataSource;
    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_choosing);

        Event[] mEvents = {
                new Event(
                        getString(R.string.start_event_breakfast),
                        10*Constants.MINUTE
                ),
                new Event(
                        getString(R.string.start_event_lunch),
                        10*Constants.MINUTE
                ),
                new Event(
                        getString(R.string.start_event_supper),
                        10*Constants.MINUTE
                ),
                new Event(
                        getString(R.string.start_event_cleanup),
                        10*Constants.MINUTE
                ),
                new Event(
                        getString(R.string.start_event_morning_hygiene),
                        10*Constants.MINUTE
                ),
                new Event(
                        getString(R.string.start_event_workout),
                        1*Constants.HOUR
                ),
                new Event(
                        getString(R.string.start_event_cooking),
                        30*Constants.MINUTE
                ),
                new Event(
                        getString(R.string.start_event_study),
                        30*Constants.MINUTE
                ),



        };

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.event_recylist);
        mDataSource = new EventDataSource(this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        final StartEventsAdapter adapter = new StartEventsAdapter(mEvents);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);



        mContext = this;
        final FloatingActionButton continueFab = (FloatingActionButton) findViewById(R.id.contiue_fab);
        continueFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Event[] events = adapter.getChosenEvents();
                //Event[] startAndStop = mDayBook.startAndStop;
                Event[] startAndStop = {
                        new Event(
                                getString(R.string.morning_greeting_label),
                                7 * Constants.HOUR,
                                1),
                        new Event(
                                getString(R.string.night_greeting_label),
                                2 * Constants.HOUR,
                                22 * Constants.HOUR)
                };
                startAndStop[0].setId(Constants.MORNING_ID);
                startAndStop[1].setId(Constants.NIGHT_ID);
                for (Event event : startAndStop) {
                    mDataSource.create(event);
                }
                for (Event event : events) {
                    if (!event.isChosenToBeRemoved()) {
                        mDataSource.create(event);
                    }
                }
                start(mContext);
            }
        });

    }

    public static void start(Context context) {
        Intent starter = new Intent(context, MainActivity.class);
        context.startActivity(starter);
    }
}
