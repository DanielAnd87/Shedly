package com.glenn.hatter.Shedly.ui;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.glenn.hatter.Shedly.R;
import com.glenn.hatter.Shedly.adapters.EventAdapter;
import com.glenn.hatter.Shedly.constants.Constants;
import com.glenn.hatter.Shedly.data.Event;
import com.glenn.hatter.Shedly.data.db.EventDataSource;
import com.glenn.hatter.Shedly.ui.fragments.DatePickerFragment;
import com.glenn.hatter.Shedly.ui.fragments.EventQueneFragment;
import com.glenn.hatter.Shedly.ui.fragments.NewEventFragment;
import com.glenn.hatter.Shedly.interfaces.Communicator;
import com.glenn.hatter.Shedly.model.ConvertToTime;
import com.glenn.hatter.Shedly.model.SortEvent;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;



public class MainActivity extends Activity implements Communicator {
    private NewEventFragment mNewEventFragment;
    private EventQueneFragment mEventQueueFragment;
    private FragmentManager mFragmentManager;
    public static final String FRAGMENT_TAG = "new Event";
    private EventDataSource mDatasource;
    private boolean mMorning;
    private Calendar mCalendar = Calendar.getInstance();
    private Calendar mTimeCal = Calendar.getInstance();
    private TextView mDateText;
    // Initiates the daybook so I can make a index from it (mCheesIndex) and send that to the adapter. Later I will reorder the index and reference it to the Daybook class.
    private ArrayList<Event> mEvent = new ArrayList<>();
    private EventAdapter adapter;

    private final TimePickerDialog.OnTimeSetListener timeDialog= new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay,
                              int minute) {
            mTimeCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
            mTimeCal.set(Calendar.MINUTE, minute);
            /*
            if (mMorning) {
                mTimeCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mTimeCal.set(Calendar.MINUTE, minute);
            } else {
                int nightTime = adapter.getNightTime();
                mTimeCal.set(Calendar.HOUR_OF_DAY, ConvertToTime.getHour(nightTime));
                mTimeCal.set(Calendar.MINUTE, ConvertToTime.getMinute(nightTime));
            }
             */
            updateMorningAndNightTime();
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mDateText = (TextView) findViewById(R.id.date_label);
        mDateText.setText(setDate(mCalendar));
        mDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        mDatasource = new EventDataSource(this);
        readAndSortEvents();

        adapter = new EventAdapter(this, mEvent, mDatasource);
        recyclerView.setAdapter(adapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);


        final android.support.design.widget.FloatingActionButton mNewEventFab = (android.support.design.widget.FloatingActionButton) findViewById(R.id.new_event_fab);
        mNewEventFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date date = mCalendar.getTime();
                long timeStamp = (date.getTime()/1000);
                Bundle bundle = communicateEventpreferences(new Event("New event", 10), timeStamp);

                mFragmentManager = getFragmentManager();
                mNewEventFragment = new NewEventFragment();
                mNewEventFragment.setArguments(bundle);
                FragmentTransaction transaction = mFragmentManager.beginTransaction();
                transaction.add(R.id.main_layout, mNewEventFragment, FRAGMENT_TAG);
                transaction.addToBackStack("new event");
                transaction.commit();
            }
        });
/*
        final FloatingActionButton mEventQueueFab = (FloatingActionButton) findViewById(R.id.event_queue_fab);
        mEventQueueFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFragmentManager = getFragmentManager();
                mEventQueueFragment = new EventQueneFragment();
                mEventQueueFragment.setArguments(adapter.getEventQuene(-1, true));
                FragmentTransaction transaction = mFragmentManager.beginTransaction();
                transaction.add(R.id.main_layout, mEventQueueFragment, FRAGMENT_TAG);
                transaction.addToBackStack("event Quene");
                transaction.commit();

            }
        });
*/
        final android.support.design.widget.FloatingActionButton mCalendarFab = (android.support.design.widget.FloatingActionButton) findViewById(R.id.calendar_fab);
        mCalendarFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });

        final android.support.design.widget.FloatingActionButton mEventBin = (android.support.design.widget.FloatingActionButton) findViewById(R.id.eventbin_fab);
        mEventBin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFragmentManager = getFragmentManager();
                mEventQueueFragment = new EventQueneFragment();
                mEventQueueFragment.setArguments(adapter.getEventQuene(-1, false));
                FragmentTransaction transaction = mFragmentManager.beginTransaction();
                transaction.add(R.id.main_layout, mEventQueueFragment, FRAGMENT_TAG);
                transaction.addToBackStack("event Quene");
                transaction.commit();

            }
        });

        // This is where Drag&Drop and swipe to remove occurs
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {


            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                boolean moved = false;
                if (viewHolder.getAdapterPosition() != 1 || viewHolder.getAdapterPosition() != adapter.getItemCount() - 1) {

                    if (viewHolder.getItemViewType() == 0) {
                        return false;
                    }
                    if (target.getItemViewType() == 1) {
                        final int fromPos = viewHolder.getAdapterPosition();
                        final int toPos = target.getAdapterPosition();

                        moved = adapter.move(fromPos, toPos);
                    }

                    // move item in `fromPos` to `toPos` in adapter.
                    // adapter.notifyItemMoved(fromPos, toPos);
                    return moved;// true if moved, false otherwise
                } else {
                    return false;
                }

            }



            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                //Remove swiped item from list and notify the RecyclerView
                if (viewHolder.getItemViewType() == 1) {
                    if (viewHolder.getLayoutPosition() != 1 && viewHolder.getLayoutPosition() != adapter.getItemCount()-1) {
                        if (swipeDir == ItemTouchHelper.LEFT) {
                            // Remove and add to freeTime.
                            adapter.replaceEvent(viewHolder.getLayoutPosition(), false);
                            Toast.makeText(MainActivity.this, R.string.removing_event_message, Toast.LENGTH_SHORT).show();

                        } else if (swipeDir == ItemTouchHelper.RIGHT) {
                            // As remove and add to Queue
                            adapter.replaceEvent(viewHolder.getLayoutPosition(), true);


                        }
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    adapter.notifyDataSetChanged();
                }
            }


        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);


        itemTouchHelper.attachToRecyclerView(recyclerView);




    }

    private void showDatePicker() {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }



    private Bundle communicateEventpreferences(Event event, long timeStamp) {
        // Will send the name and duration to EventFragment
        int dur = event.getDuration();
        String name = event.getName();

        Bundle bundle = new Bundle();

        bundle.putString(Constants.NEW_EVENT_NAME, name);
        bundle.putString(Constants.REPEATING, event.getRecurring());
        bundle.putInt(Constants.NEW_MINUTE, ConvertToTime.getMinute(dur));
        bundle.putInt(Constants.NEW_HOUR, ConvertToTime.getHour(dur));
        bundle.putInt(Constants.FIXEDTIME_VALUE, event.getFixedTime());
        bundle.putInt(Constants.TRAVEL_DURATION_FROM, event.getTravelDurationFrom());
        bundle.putInt(Constants.TRAVEL_DURATION_TO, event.getTravelDurationTo());
        bundle.putInt(Constants.TIMESTAMP, (int) timeStamp);


        bundle.putBoolean(Constants.NEW_EVENT_BOOLEAN, true);
        bundle.putInt(Constants.NEW_EVENT_ID, -1);


        return bundle;
    }

    private String setDate(Calendar calendar) {
        //DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat dateFormat = DateFormat.getDateInstance();
        Date date = calendar.getTime();
        return dateFormat.format(date);


    }


    public static void start(Context context) {
        Intent starter = new Intent(context, EventChoosingActivity.class);
        context.startActivity(starter);
    }

    private void readAndSortEvents() {
        mEvent = mDatasource.read();
        // Checking if morning and eventing is there and add it if not.
        if (mEvent.size() == 0) {
            start(this);
        }
        // Sorts reoccurring events.
        SortEvent sortEvent = new SortEvent(mCalendar, mEvent);
        mEvent = sortEvent.getEvents();
    }

    private void updateMorningAndNightTime() {
        int startMinute = mTimeCal.get(Calendar.MINUTE);
        int startHour = mTimeCal.get(Calendar.HOUR_OF_DAY);
        // This is the duration from midnight until the time the schedule starts.
        int totalPixelMinutes = startHour * Constants.HOUR + startMinute * Constants.MINUTE;

        if (mMorning) {
            adapter.updateMorning(totalPixelMinutes);
        } else {
            adapter.updateNight(totalPixelMinutes);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        adapter.saveDate();
        Toast.makeText(this, "Day saved.", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void sendQueneIndex(int evenId, int mEventpos) {
        // Checking if event is a FixedTime.
        Event event = adapter.getEventFromId(evenId);
        if (event.isFixedTime()) {
            adapter.addEvent(event, false);
        } else {
            adapter.removeFreetime(evenId, mEventpos);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void startNewEventFragment(Bundle bundle) {
        mFragmentManager = getFragmentManager();
        mNewEventFragment = new NewEventFragment();
        mNewEventFragment.setArguments(bundle);
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.add(R.id.main_layout, mNewEventFragment, FRAGMENT_TAG);
        transaction.addToBackStack("new event");
        transaction.commit();
    }

    @Override
    public void startEventQueneFragment(Bundle bundle) {
        mFragmentManager = getFragmentManager();
        mEventQueueFragment = new EventQueneFragment();
        mEventQueueFragment.setArguments(bundle);
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.add(R.id.main_layout, mEventQueueFragment, FRAGMENT_TAG);
        transaction.addToBackStack("event Quene");
        transaction.commit();

    }
// Todo: Send correct date and call SortEvent and create a new recyclerView adapter.
    @Override
    public void communicatingDate(Calendar calendar) {
        mCalendar = calendar;
        // Filling up eventsFromDb with saved events from my database.
        ArrayList<Event> eventFromDb = mDatasource.read();
        // TODO: 2016-02-10 Wasting performance when I dos'nt check for a saved day.
        // Initilazing the SortEvent class with my new ArrayList.
        SortEvent sortEvents = new SortEvent(calendar, eventFromDb);
        // Using the result in my adapter.
        adapter.setCalendar(calendar);
        adapter.startDay(sortEvents.getEvents(), calendar);
        adapter.notifyDataSetChanged();
        mDateText.setText(setDate(calendar));
    }

    @Override
    public void changeMorningTime(boolean morning) {
        mMorning = morning;
        mTimeCal = Calendar.getInstance();
        if (mMorning) {
            new TimePickerDialog(MainActivity.this,
                    timeDialog,
                    mTimeCal.get(Calendar.HOUR_OF_DAY),
                    mTimeCal.get(Calendar.MINUTE),
                    true).show();
        } else {

            int nightTime = adapter.getNightTime();

            new TimePickerDialog(MainActivity.this,
                    timeDialog,
                    ConvertToTime.getHour(nightTime),
                    ConvertToTime.getMinute(nightTime),
                    true).show();
        }


    }



    @Override
    public void deleteEvent(int eventId) {
        adapter.removeEvent(eventId);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void restoreRemovedEvents(int pos, boolean ordinary) {
        adapter.resetEvents();
    }


    @Override
    public void respond(Event event, boolean brandNew, int dataPos) {
        EventDataSource datasource = new EventDataSource(this);
        if (brandNew) {
            int id = datasource.create(event);
            event.setId(id);
            // FIXME: 2015-11-27 DRY
            adapter.addEvent(event, true);
        } else {
            datasource.update(event);
            if (event.isFixedTime()) {
                adapter.replacingFixedTime(event);
                adapter.addEvent(event, false);
            } else {
                adapter.addEvent(event, false);
            }
        }


        adapter.notifyDataSetChanged();
    }


    @Override
    public void removeFraktion(String fractionTag) {
        if (fractionTag.equals("new Event")) {
            NewEventFragment eventFragment = (NewEventFragment) mFragmentManager.findFragmentByTag(FRAGMENT_TAG);
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            if (eventFragment != null) {
                transaction.remove(eventFragment);
                transaction.commit();
            } else {
                Toast.makeText(this, "The Fragment was not added before", Toast.LENGTH_SHORT).show();
            }
        }
        if (fractionTag.equals(Constants.QUEUE_FRACTION)) {
            EventQueneFragment eventFragment = (EventQueneFragment) mFragmentManager.findFragmentByTag(FRAGMENT_TAG);
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            if (eventFragment != null) {
                transaction.remove(eventFragment);
                transaction.commit();
            } else {
                Toast.makeText(this, "The Fragment was not added before", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
