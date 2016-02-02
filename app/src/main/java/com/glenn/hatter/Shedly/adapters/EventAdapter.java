package com.glenn.hatter.Shedly.adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.glenn.hatter.Shedly.constants.Constants;
import com.glenn.hatter.Shedly.data.db.EventDataSource;
import com.glenn.hatter.Shedly.interfaces.Communicator;
import com.glenn.hatter.Shedly.ui.MainActivity;
import com.glenn.hatter.Shedly.R;
import com.glenn.hatter.Shedly.data.Event;
import com.glenn.hatter.Shedly.model.ConvertToTime;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by hatter on 2015-04-29.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {


    private int mReplaceIndex = -1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private ArrayList<Event> mEvent = new ArrayList<>();
    private ArrayList<Event> mReplaceList = new ArrayList<>();
    private ArrayList<Event> mRemovedEvents = new ArrayList<>();


    // for now I replace mReplaceList where it is needed.
    private ArrayList<Event> mOrdinaryEventQueue = new ArrayList<>();
    private ArrayList<Event> mBinedFixedEvents = new ArrayList<>();
    // I think this is where the unused events goes.

    private ArrayList<Integer> mEventsToBeReplacedRef = new ArrayList<>();

    private Communicator mCommunicator;

    public void setCommunicator(Communicator communicator) {
        mCommunicator = communicator;
    }


    private boolean mChosen = false;
    private boolean mAreaChoice = false;
    private final int MINUTE = 10;
    private final int HOUR = MINUTE * 60;
    private int mNeededDur = 0;
    private EventDataSource mDatasource;
    private Calendar mCalendar;

    private Context mContext;

    public EventAdapter(Context context, List<Event> list, EventDataSource datasource) {
        mContext = context;
        mCalendar = Calendar.getInstance();
        mDatasource = datasource;
        mRemovedEvents.addAll(list);
        for (Event event : list) {
            mReplaceList.add(mReplaceList.size(), event);
            mReplaceList.add(mReplaceList.size(), event);
        }
        mEvent = mDatasource.readNote(mCalendar, mReplaceList);
        for (int i = 0; i < mReplaceList.size(); i++) {
            mReplaceList.remove(i);
        }


        Log.i(TAG, "EventAdapter Event in mEvent: ");
        for (Event event : mEvent) {
            Log.i(TAG, "EventAdapter " + event.getName());
        }
        Log.i(TAG, "EventAdapter Event in replaceList: ");
        for (Event event : mReplaceList) {
            Log.i(TAG, "EventAdapter " + event.getName());
        }


        if (mEvent.size() > 0) {
            for (int i = 0; i < mRemovedEvents.size(); i++) {
                Event current = mRemovedEvents.get(i);
                if (mEvent.contains(current)) {
                    mRemovedEvents.remove(current);
                    i--;
                }
                if (mReplaceList.contains(current)) {
                    mRemovedEvents.remove(current);
                    i--;
                }
            }

            for (int i = 0; i < mReplaceList.size(); i++) {
                Event current = mReplaceList.get(i);
                if (current.isFixedTime()) {
                    mBinedFixedEvents.add(current);
                    mReplaceList.remove(i);
                    i--;
                }
            }
            Toast.makeText(mContext, R.string.saved_day_message, Toast.LENGTH_SHORT).show();
        } else {
            changeStartTime(list);
            update(list);
        }
    }

    public void createDay(List<Event> list) {
        mRemovedEvents.addAll(list);
        for (Event event : list) {
            mReplaceList.add(mReplaceList.size(), event);
            mReplaceList.add(mReplaceList.size(), event);
        }
        mEvent = mDatasource.readNote(mCalendar, mReplaceList);
        for (int i = 0; i < mReplaceList.size(); i++) {
            mReplaceList.remove(i);
        }


        Log.i(TAG, "EventAdapter Event in mEvent: ");
        for (Event event : mEvent) {
            Log.i(TAG, "EventAdapter " + event.getName());
        }
        Log.i(TAG, "EventAdapter Event in replaceList: ");
        for (Event event : mReplaceList) {
            Log.i(TAG, "EventAdapter " + event.getName());
        }


        if (mEvent.size() > 0) {
            for (int i = 0; i < mRemovedEvents.size(); i++) {
                Event current = mRemovedEvents.get(i);
                if (mEvent.contains(current)) {
                    mRemovedEvents.remove(current);
                    i--;
                }
                if (mReplaceList.contains(current)) {
                    mRemovedEvents.remove(current);
                    i--;
                }
            }

            for (int i = 0; i < mReplaceList.size(); i++) {
                Event current = mReplaceList.get(i);
                if (current.isFixedTime()) {
                    mBinedFixedEvents.add(current);
                    mReplaceList.remove(i);
                    i--;
                }
            }
            Toast.makeText(mContext, R.string.saved_day_message, Toast.LENGTH_SHORT).show();
        } else {
            update(list);
        }
    }

    private void changeStartTime(List<Event> list) {
        Integer currentHour = mCalendar.get(Calendar.HOUR_OF_DAY);
        Integer currentMinute = mCalendar.get(Calendar.MINUTE);
        String name = mContext.getString(R.string.morning_greeting_label);
        int morningDuration = currentHour * Constants.HOUR + currentMinute * Constants.MINUTE;
        if (morningDuration > 12 * Constants.HOUR && morningDuration < 20 * Constants.HOUR) {
            name = mContext.getString(R.string.eventing_greeting_label);
        } else if (morningDuration > 20 * Constants.HOUR && morningDuration < 24 * Constants.HOUR) {
            name = mContext.getString(R.string.evening_greeting_label);
        }

        int y = 0;
        for (int i = 0; i < list.size() && y < 2; i++) {
            if (list.get(i).getId() == 1) {
                list.get(i).setDuration(morningDuration);
                list.get(i).setName(name);


                y++;
            }
        }

        if (morningDuration > Constants.HOUR * 22) {
            int t = 0;
            for (int i = 0; i < list.size() && t < 2; i++) {
                if (list.get(i).getId() == 2) {
                    list.get(i).setFixedTime(morningDuration + 30 * Constants.MINUTE);
                    list.get(i).setName(name);


                    t++;
                }
            }

        }
    }

    public void update(List<Event> list) {
        mReplaceList.clear();
        mRemovedEvents.clear();
        mOrdinaryEventQueue.clear();
        mEvent.clear();
        for (Event event : list) {
            if (event.getFixedTime() > 0) {
                mReplaceList.add(event);
            } else {
                mOrdinaryEventQueue.add(event);
            }

        }
        Collections.sort(mReplaceList, new Comparator<Event>() {
            @Override
            public int compare(Event lhs, Event rhs) {
                return lhs.getFixedTime() - rhs.getFixedTime();
            }
        });
        Log.i(TAG, "The events in mReplaceList:");
        sortForReplaceList();
        Collections.sort(mOrdinaryEventQueue, new Comparator<Event>() {
            @Override
            public int compare(Event lhs, Event rhs) {
                return lhs.getDuration() - rhs.getDuration();
            }
        });

        // the last event in the replaceList has to be the goodnigth event.
        Event lastEventInReplaceList = mReplaceList.get(mReplaceList.size() - 1);
        while (lastEventInReplaceList.getId() != 2) {
            /*
            for (Event event : mReplaceList) {
                if (event.getId() == 2) {
                    Event lastEvent = mReplaceList.get(mReplaceList.size() - 1);
                    int lastEventStopTime = lastEvent.getTime()+lastEvent.getDuration();
                    event.setTime(lastEventStopTime + (5 * MINUTE));
                    int eventTime = event.getTime();
                    // Set the duration such that it end at midnight.
                    event.setDuration(24 * HOUR - eventTime);
                }
            }
            sortForReplaceList();
            */
            mBinedFixedEvents.add(lastEventInReplaceList);
            mReplaceList.remove(mReplaceList.size() - 1);


            lastEventInReplaceList = mReplaceList.get(mReplaceList.size() - 1);
        }
        // Checking if two times occur the same time.
        for (int i = 0; i < mReplaceList.size() - 1; i++) {
            Event current = mReplaceList.get(i);
            Event after = mReplaceList.get(i + 1);

            int currentEndTime = current.getFixedTime() + current.getDuration();



            if (currentEndTime > after.getFixedTime()) {
                if (i != mReplaceList.size() - 2) {
                    mBinedFixedEvents.add(current);
                    mReplaceList.remove(current);
                    i--;
                } else {
                    int fixedTime = currentEndTime + (5 * MINUTE);
                    after.setFixedTime(fixedTime);
                    // Set the duration such that it end at midnight.
                    after.setDuration(24 * HOUR - fixedTime);
                }
            }
        }
        Log.i(TAG, "The events in mOrdinaryEventQueue:");
        for (int i = 0; i < mOrdinaryEventQueue.size(); i++) {
            Log.i(TAG, mOrdinaryEventQueue.get(i).getName() + " with the duation of " + mOrdinaryEventQueue.get(i).getDuration() + ".");
        }
        int time = 0;
        // The code will run trough all the fixed time events
        while (mReplaceList.size() > 0) {
            int eventPlace = addingFixedTime(time);
            Log.i(TAG, "1. Added the fixedTime event  " + mEvent.get(mEvent.size() - 1).getName() + "  to the list with the fixedtime of " + mEvent.get(mEvent.size() - 1).getFixedTime());
            if (mEvent.size() > 2) {
                // And this code will run as long as the current event is'nt at it's fixedTime.
                while (mEvent.get(mEvent.size() - 1).getTime() != mEvent.get(mEvent.size() - 1).getFixedTime()) {
                    int current = 0;
                    // The value of timeLeft is what needs to fill up with events until next fixedTime is added.
                    int timeLeft = mEvent.get(mEvent.size() - 1).getFixedTime() - time;
                    while (timeLeft < 0) {
                        Log.i(TAG, "The time left was " + timeLeft + " and I therefore binned " + mEvent.get(mEvent.size() - 1).getName() + " and putting in the next event in line");
                        mBinedFixedEvents.add(mEvent.get(mEvent.size() - 1));
                        mEvent.remove(mEvent.size() - 1);
                        mEvent.remove(mEvent.size() - 1);
                        eventPlace = addingFixedTime(time);
                        timeLeft = mEvent.get(mEvent.size() - 1).getFixedTime() - time;
                    }
                    Log.i(TAG, "2. The FixedTime has still not pushed enough." + " The time is " + time + ", the FixedTime currently at " + mEvent.get(mEvent.size() - 1).getFixedTime() + ", and the timeLeft is at " + timeLeft + ".");

                    // While there is still ordinary events to pick from they are prioritised.
                    if (mOrdinaryEventQueue.size() > 0) {
                        Log.i(TAG, "3. There's still ordinary events left to chose from.");
                        Log.i(TAG, "3. Time at " + time);

                        // Only adding from the ordinary events if they can fit between the fixedTime and the latest time.
                        while (current < mOrdinaryEventQueue.size()
                                && mEvent.get(mEvent.size() - 1).getTime() != mEvent.get(mEvent.size() - 1).getFixedTime()) {
                            Log.i(TAG, "4. Trying number " + current + " in the list of orinary Events.");


                            Event ordinaryEvent = mOrdinaryEventQueue.get(current);
                            // If there is ordinary events that can be fitted between the current event and the last, then one of them will be picked
                            if (ordinaryEvent.getDuration() <= mEvent.get(mEvent.size() - 1).getFixedTime() - time) {
                                // Setting the new and focus event time and adding its duration to the time variable

                                ordinaryEvent.setTime(time);
                                time += ordinaryEvent.getDuration();

                                mEvent.get(mEvent.size() - 1).setTime(time);
                                // Transfer the events from queue to mEvent
                                mEvent.add(eventPlace, ordinaryEvent);
                                mEvent.add(eventPlace, ordinaryEvent);
                                mOrdinaryEventQueue.remove(current);

                                timeLeft = mEvent.get(mEvent.size() - 1).getTime() - time;

                                eventPlace = mEvent.size() - 2;
                                Log.i(TAG, "5. Found a nice match and adding the event at " + mEvent.get(eventPlace).getTime() + " The time left to push is " + timeLeft + " minutes!");
                            } else {
                                Log.i(TAG, "6. Wasn't a match and moving to the next Event in line.");
                                current++;
                            }
                        }
                        if (timeLeft > 0) {
                            // FIXME: 2015-10-16 Everytime this is called the fillup method below is allso called!

                            time = addFillup(time, eventPlace, timeLeft);
                            timeLeft = 0;
                        }
                    }
                    if (timeLeft > 0) {
                        time = addFillup(time, eventPlace, timeLeft);
                    }
                }
            }
            // Adding the new duration to the time.
            time += mEvent.get(mEvent.size() - 1).getDuration();
            Log.i(TAG, "9. Setting a new time at " + time + ".");
        }
        mReplaceList.addAll(mOrdinaryEventQueue);
    }

    private void sortForReplaceList() {
        for (int i = 0; i < mReplaceList.size(); i++) {
            Log.i(TAG, mReplaceList.get(i).getName() + " with the fixedTime at" + mReplaceList.get(i).getFixedTime() + ".");
        }
    }

    private int addingFixedTime(int time) {
        mEvent.add(mReplaceList.get(0));
        mEvent.add(mReplaceList.get(0));

        mReplaceList.remove(0);
        mEvent.get(mEvent.size() - 1).setTime(time);
        mEvent.get(mEvent.size() - 2).setTime(time);


        return mEvent.size() - 2;
    }

    private int addFillup(int time, int eventPlace, int timeLeft) {
        addFreeTime(timeLeft, eventPlace, time);
        time += timeLeft;
        mEvent.get(mEvent.size() - 1).setTime(time);
        mEvent.get(mEvent.size() - 2).setTime(time);
        Log.i(TAG, "addFillup: Adding a freeTime at " + mEvent.get(mEvent.size() - 3).getTime() + " with the duration of " + mEvent.get(mEvent.size() - 3).getDuration() + ".");
        return time;
    }

    private void addFreeTime(int duration, int dataPos, int time) {
        //Creating a freetime.
        Event freetime = new Event(mContext.getString(R.string.freetime_label), duration, true);
        freetime.setTime(time);
        // Adding freetime
        mEvent.add(dataPos, freetime);
        mEvent.add(dataPos, freetime);
        // notify the adapter
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        int viewType;

        if (position % 2 == 0) {
            viewType = 0;
        } else {
            viewType = 1;
        }
        return viewType;
    }


    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_list_time_item, parent, false);

        View view1 = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_list_item, parent, false);

        EventViewHolder viewHolder = new EventViewHolder(view);
        EventViewHolder viewHolder1 = new EventViewHolder(view1);

        switch (viewType) {
            case 0:
                return viewHolder;
            case 1:
                return viewHolder1;
        }

        return viewHolder;


    }


    @Override
    public void onBindViewHolder(EventViewHolder holder, int position) {
        holder.bindHour(mEvent.get(position).getName(),
                mEvent.get(position).getDuration(), position, mEvent.get(position).getTime());

    }


    @Override
    public int getItemCount() {
        return mEvent.size();
    }


    public void swipeToDismiss(int itemId, boolean refill) {
        Event deletedEvent = deleteAndReplaceEvent(itemId);

        if (!refill) {
            mEventsToBeReplacedRef.remove(0);
        }

        // todo: Should put the removed event in a queue list

        if (refill) {
            fillWithNewEvents(deletedEvent.getDuration(), itemId);
        }

        //todo: Looks awful! try to animate the apperance of new events. Maybe they should grow slitely and srink back again.
        notifyDataSetChanged();


    }

    private Event deleteAndReplaceEvent(int position) {
        Event deletedEvent = mEvent.get(position);
        mEvent.remove(position);
        mEvent.remove(position - 1);

        // If I currently is chosing event to replace then I'm correcting those references here everytime an event is deleted.
        if (mChosen) {

            if (mEventsToBeReplacedRef.contains(position)) {
                for (int y = 0; y < mEventsToBeReplacedRef.size(); y++) {
                    // Because I removed an index in mEvent it means that all indexes above is changed, and I need to change all those references in the data-list
                    if (mEventsToBeReplacedRef.get(y) > position) {
                        mEventsToBeReplacedRef.set(y, mEventsToBeReplacedRef.get(y) - 2);
                    }
                }

            }
        }

        notifyItemRemoved(position);
        notifyItemRemoved(position - 1);
        notifyItemRangeChanged(position, 2);
        return deletedEvent;
    }

    private void fillWithNewEvents(int duration, int index) {
        // The unused space should be filled with new events
        int remainingSpaceToFillup = duration;
        if (duration == 0) {
            notifyItemRemoved(index);
            notifyItemRangeChanged(index, 1);
            //setTimes(index, true);
            updateAllTimes();
        }
        while (remainingSpaceToFillup > 0) {

            Log.i(TAG, "fillWithNewEvents: Remaining space to fill up is: " + remainingSpaceToFillup);
            Log.i(TAG, "fillWithNewEvents: Number of events in prelace-list is: " + mReplaceList.size());
            // checking if there is event in queue and if those fit whitin the selected space.
            if (mReplaceList.size() > 0) {
                int space = remainingSpaceToFillup;

                boolean found = false;
                for (int i = 0; i < mReplaceList.size() && !found; i++) {
                    Event current = mReplaceList.get(i);
                    Log.i(TAG, "fillWithNewEvents: Event: " + current.getName());
                    Log.i(TAG, "fillWithNewEvents: duration: " + current.getDuration());
                    // Will only run if there is space for this event.
                    if (current.getDuration() <= remainingSpaceToFillup) {
                        Log.i(TAG, "There was a events in queue");
                        addAndSetTimeForNewEvent(current, index);
                        remainingSpaceToFillup -= current.getDuration();
                        mReplaceList.remove(i);
                        found = true;

                    }
                }
                if (remainingSpaceToFillup < space && found) {
                    index += 2;
                }
                if (!found) {
                    Log.i(TAG, "fillWithNewEvents Trying to combine events.");
                    if (!findingAndChangingFreetime(index, remainingSpaceToFillup)) {
                        Log.i(TAG, "fillWithNewEvents Failed and adding a fillup instead.");
                        Event freetime = new Event(mContext.getString(R.string.freetime_label), duration, true);
                        freetime.setDuration(remainingSpaceToFillup);
                        addAndSetTimeForNewEvent(freetime, index);

                    }
                    remainingSpaceToFillup = 0;
                }
            } else {
                Log.i(TAG, "fillWithNewEvents Trying to combine events.");
                if (!findingAndChangingFreetime(index, remainingSpaceToFillup)) {
                    Log.i(TAG, "fillWithNewEvents Failed and adding a fillup instead.");
                    Event freetime = new Event(mContext.getString(R.string.freetime_label), duration, true);
                    freetime.setDuration(remainingSpaceToFillup);
                    addAndSetTimeForNewEvent(freetime, index);

                }
                remainingSpaceToFillup = 0;
            }
        }

    }


    private void choseEventsToReplace(int fromDataPos) {

        // changeToFreetime(fromDataPos);

        Log.i(TAG, "choseEventsToReplace");
        Log.i(TAG, "Deleting event");
        for (Event event : mEvent) {
            Log.i(TAG, "choseEventsToReplace(): " + ConvertToTime.convertToTime(event.getTime()) + "");
        }

        highlightArea();

        Toast.makeText(mContext, R.string.event_removal_query, Toast.LENGTH_LONG).show();
        notifyDataSetChanged();
        Log.d(TAG, "choseEventsToReplace() called with: " + "fromDataPos = [" + fromDataPos + "]");

        mChosen = true;
    }

    public void choseEventsToReplace(int id, boolean brandNew) {
        // I choosing events to replace with here.

        // If it's just a small change of an event then I want to delete the original and replace it
        // at the same spot, (if it fits that is, otherwise I need to higligth area.)

        Event newEvent = mReplaceList.get(0);
        int fromEventPos = 0;
        for (int i = 0; i < mReplaceList.size(); i++) {
            if (mReplaceList.get(i).getId() == id) {
                newEvent = mReplaceList.get(i);
                fromEventPos = i;
            }
        }
        int newDuration = newEvent.getDuration();
        if (brandNew) {

            mReplaceList.add(newEvent);
            mNeededDur = newDuration;
            mReplaceList.remove(fromEventPos);

            Log.i(TAG, "Adding to mEventQuene");

            highlightArea();

            Toast.makeText(mContext, R.string.put_event_query, Toast.LENGTH_LONG).show();
            mChosen = true;
        } else {
            if (newDuration <= mNeededDur) {
                // Firstly delete the original event.
                replaceWithChangedEvent(fromEventPos, mNeededDur);

            } else {

                // if, Check if there is a freetime event to take time from

                int freetimeDataPos = findingNearbyFreetimePosition(fromEventPos);
                if (freetimeDataPos != -1) {
                    replaceWithChangedEvent(fromEventPos, newDuration);
                    int oldFreetimeDuration = mEvent.get(freetimeDataPos).getDuration();

                    int freetimeDuration = oldFreetimeDuration - (newDuration - mNeededDur);

                    deleteAndReplaceEvent(freetimeDataPos);

                    fillWithNewEvents(freetimeDuration, freetimeDataPos);

                } else {
                    // Let the user pick events to replace with.
                    // Dosen't work!
                    // Todo: The following method always delete the event in fromEventPos and replace it with an "fillup". This will probably cause problem here.
                    choseEventsToReplace(fromEventPos);

                    replaceWithChangedEvent(fromEventPos, newDuration);

                }

                // else if, activate mChose and set higlights and prompt the user to chose events to replace.


                // Todo: 1. If the event is bigger then originaly I first need to check if there is freetime in that fixedTimeArea.
                // 2. If true then I replace the old event with the new one.
                // 3. Then take the new time and subtract it with the original time and it becomes the new mNeededTime.
                // 4. Delete the old freetime and set a new one with mNeededTime as duration and the position I searched and found.

                notifyDataSetChanged();
            }


        }
    }

    private void replaceWithChangedEvent(int fromDataPos, int dur) {
        mReplaceList.add(mReplaceList.get(0));
        mReplaceList.remove(0);


        deleteAndReplaceEvent(fromDataPos);

        // And then add the new one, the first in mEventQueneList

        fillWithNewEvents(dur, fromDataPos);

        notifyDataSetChanged();
    }


    private int findRecentFixedTime(int index, boolean up) {
        int theIndexInData = -1;
        int dir;
        if (up) {
            dir = 1;
        } else {
            dir = -1;
        }
        for (int i = index; i < mEvent.size(); i+= dir) {
            // First event that is an fixed time is returned.
            if (mEvent.get(i).getFixedTime() > 0) {
                return i;
            }
        }

        return theIndexInData;
    }

    public void addAndSetTimeForNewEvent(Event event, int LowEventIndex) {
        // Here I add a brand new reference and insert it at correct index
        // todo: When I replace events I'm only going to check from the lowest index to the higest index, and as a safety percation  I migth add a quality check to a few following indexes.

        mEvent.add(LowEventIndex - 1, event);
        mEvent.add(LowEventIndex - 1, event);

        //setTimes(LowEventIndex, true);
        updateAllTimes();

        notifyItemInserted(LowEventIndex);
        notifyItemInserted(LowEventIndex - 1);
        Log.i(TAG, "addAndSetTimeForNewEvent");
        Log.i(TAG, "Adding one event");
        for (Event event1 : mEvent) {
            Log.i(TAG, ConvertToTime.convertToTime(event1.getTime()) + "");
            Log.i(TAG, event1.getName() + "");
        }
        Log.i(TAG, "addAndSetTimeForNewEvent() called with: " + "event = [" + event.getName() + "], LowEventIndex = [" + LowEventIndex + "]");
    }

    private int setTimes(int LowEventIndex, boolean ignoreFixedTime) {
        int timesCorrected = 0;
        boolean afterFixedTime = false;


        // Change all times from the startIndex until the next fixedTime occurs.
        for (int i = LowEventIndex; i < mEvent.size() && !afterFixedTime; i += 2) {
            // Here I should change the time for all corresponding events
            Event current = mEvent.get(i);
            // TODO: 2015-09-30 Changed the if statement to something that seemed more resomble. Che if it really was!
            //if (i > 2)
            if (i > 1) {

                Event last = mEvent.get(i - 2);
                // Chould only change the time if the event is'nt an fixedTime. Otherwise the fixedTime should act as an base for the next time.
                current.setTime(last.getTime() + last.getDuration());

                current = mEvent.get(i - 1);
                current.setTime(last.getTime() + last.getDuration());
                Log.i(TAG, "setTimes Corrected the time of " + current.getName() + " into " + ConvertToTime.convertToTime(current.getTime()));
                timesCorrected++;
                // If the next event is an fixedTime then the loop will stop.
                //if (mEvent.get(data.get(i + 1)).getFixedTime() == 0) {
                if (mEvent.get(i).getFixedTime() == 0) {
                    if (!ignoreFixedTime) {
                        afterFixedTime = true;
                    }
                    ignoreFixedTime = false;
                }

            } else {

                current.setTime(0);
            }
        }

        Log.i(TAG, "setTimes whent trough " + timesCorrected);
        Log.i(TAG, "All events time is corrected");
        Log.i(TAG, "setTimes() called with: " + "LowEventIndex = [" + LowEventIndex + "], ignoreFixedTime = [" + ignoreFixedTime + "]");
        for (Event event : mEvent) {
            Log.i(TAG, event.getTime() + "");
            Log.i(TAG, event.getName() + "");
        }

        return timesCorrected;
    }

    public void highlightArea() {
        // Every push on a event will turn it yellow and when ready I will push "OK" and get a new event at the first one that is replaced
        mAreaChoice = !mAreaChoice;
    }



    public int findingNearbyFreetimePosition(int dataPos) {

        int dataPosOfFreeTime = -1;

        // Here I search for an Freetime event until I find a fixedEvent.
        for (int i = dataPos; i < mEvent.size() && mEvent.get(i).getFixedTime() == 0; i += 2) {

            Event currentEvent = mEvent.get(i);
            if (currentEvent.isFreeTime()) {
                // If I find a freetime event then I run a metod that combine there duration.
                Log.i(TAG, "findingNearbyFreetimePosition() returned: " + i);
                return i;
            }

        }
        for (int i = dataPos; i > 0 && mEvent.get(i).getFixedTime() == 0; i -= 2) {

            Event currentEvent = mEvent.get(i);
            if (currentEvent.isFreeTime()) {
                // If I find a freetime event then I run a metod that combine there duration.
                Log.i(TAG, "findingNearbyFreetimePosition() returned: " + i);
                return i;
            }

        }
        Log.d(TAG, "findingNearbyFreetimePosition() returned: " + dataPosOfFreeTime);
        return dataPosOfFreeTime;
    }


    public int findingFreetimePosition(int dataPos, boolean up) {

        int dataPosOfFreeTime = -1;

        // Here I search for an Freetime event until I
        // find a fixedEvent.
        if (!up) {
            for (int i = dataPos + 2; i < mEvent.size() && mEvent.get(i).getFixedTime() == 0; i += 2) {

                Event currentEvent = mEvent.get(i);
                if (currentEvent.isFreeTime()) {
                    // If I find a freetime event then I run a metod that combine there duration.
                    Log.i(TAG, "findingNearbyFreetimePosition() returned: " + i);
                    return i;
                }

            }
        } else {

            for (int i = dataPos - 2; i > 0 && mEvent.get(i).getFixedTime() == 0; i -= 2) {

                Event currentEvent = mEvent.get(i);
                if (currentEvent.isFreeTime()) {
                    // If I find a freetime event then I run a metod that combine there duration.
                    Log.i(TAG, "findingNearbyFreetimePosition() returned: " + i);
                    return i;
                }

            }
        }
        Log.d(TAG, "findingNearbyFreetimePosition() returned: " + dataPosOfFreeTime);
        return dataPosOfFreeTime;

    }

    public void addEvent(Event event, boolean brandNew) {
        // This method insert an fixedTime at it's place.
        if (!brandNew) {
            if (!event.isFixedTime()) {
                mReplaceList.add(0, event);
                int pos = 0;
                for (int i = 0; i < mEvent.size(); i++) {
                    if (mEvent.get(i).getId() == event.getId()) {
                        pos = i;
                    }
                }
                replaceEvent(pos, false);
                if (mEvent.get(pos).getId() != event.getId()) {
                    Toast.makeText(mContext, "Putting the event in the event queue.", Toast.LENGTH_SHORT).show();
                }
            } else {
                placeFixedTime(event);
            }
        } else {
            if (event.isFixedTime()) {
                placeFixedTime(event);
            } else {
                // If it is an ordinary event then it's put in the replaceList.
                mReplaceList.add(event);
                Toast.makeText(mContext, R.string.adding_to_replacelist_message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void placeFixedTime(Event event) {
        int potentialFreetimeIndex = 0;
        int potentialFreetime = 0;
        int endOfPotentialFreetime = 0;
        boolean found = false;
        Event potentialFreeEvent = new Event("", 1);
        for (int i = 2; i < mEvent.size() && !found; i += 2) {
            // loop till you get to where the fixedTime ends and save int in variable.
            endOfPotentialFreetime = mEvent.get(i).getTime();
            if (endOfPotentialFreetime > event.getFixedTime() + event.getDuration()) {
                // FIXME: 2015-10-09 Potential bug if the user pick a time before dawn.
                potentialFreetimeIndex = i - 2;
                potentialFreetime = mEvent.get(i).getTime();
                potentialFreeEvent = mEvent.get(potentialFreetimeIndex);
                found = true;
            }
        }
        Log.i(TAG, "addEvent: My potential event is at " + potentialFreeEvent.getTime() + " and my fixedTime at " + event.getFixedTime());
        Log.i(TAG, "addEvent: The potential event freetime: " + potentialFreeEvent.isFreeTime());
        // Check if the space is freeTime and that it start before the fixedTime.
        if (found) {
            if (potentialFreeEvent.isFreeTime() && potentialFreeEvent.getTime() < event.getFixedTime()) {
                // if it is then delete it and calculate how much freetime there is before and after the event.

                int freeTimeBefore = event.getFixedTime() - potentialFreeEvent.getTime();
                Log.i(TAG, "addEvent: Integer freeTimeBefore is: " + freeTimeBefore);
                int freeTimeAfter = potentialFreetime - (event.getFixedTime() + event.getDuration());
                Log.i(TAG, "addEvent: Integer freeTimeAfter is: " + freeTimeAfter);

                // add it at its fixedtime at it's place and do fillWithNewEvent in both side of the fixedTime.
                mEvent.remove(potentialFreetimeIndex);
                notifyItemRemoved(potentialFreetimeIndex);
                Log.i(TAG, "addEvent: Removing '" + mEvent.get(potentialFreetimeIndex).getName() + "' at " + ConvertToTime.convertToTime(potentialFreeEvent.getTime()));
                mEvent.remove(potentialFreetimeIndex);
                notifyItemRemoved(potentialFreetimeIndex);
                Log.i(TAG, "addEvent: Removing '" + mEvent.get(potentialFreetimeIndex).getName() + "' at " + ConvertToTime.convertToTime(potentialFreeEvent.getTime()));
                Event freeEventBefore = new Event("FreeTime", freeTimeBefore, true);
                freeEventBefore.setTime(event.getFixedTime() - freeTimeBefore);
                mEvent.add(potentialFreetimeIndex, freeEventBefore);

                notifyItemInserted(potentialFreetimeIndex);

                mEvent.add(potentialFreetimeIndex, freeEventBefore);

                notifyItemInserted(potentialFreetimeIndex);
                event.setTime(event.getFixedTime());
                mEvent.add(potentialFreetimeIndex + 2, event);
                notifyItemInserted(potentialFreetimeIndex + 2);
                mEvent.add(potentialFreetimeIndex + 2, event);
                notifyItemInserted(potentialFreetimeIndex + 2);
                Event freeEventAfter = new Event("FreeTime", freeTimeAfter, true);
                freeEventAfter.setTime(event.getTime() + event.getDuration());
                mEvent.add(potentialFreetimeIndex + 4, freeEventAfter);
                notifyItemInserted(potentialFreetimeIndex + 4);
                mEvent.add(potentialFreetimeIndex + 4, freeEventAfter);
                notifyItemInserted(potentialFreetimeIndex + 4);
                for (Event event1 : mEvent) {
                    Log.i(TAG, "addEvent: '" + event1.getName() + "' at " + event1.getTime());
                }
                notifyDataSetChanged();
            }
            // Check if there is a freetime around, no fixedTime in the spot, and that the freetime is big enough to absorb this event.
            else if (potentialFreeEvent.getFixedTime() <= 0 && !potentialFreeEvent.getName().equals("")) {

                // checking if the there is a freetime around and saving it's duration.

                int freetimeIndex = findingNearbyFreetimePosition(potentialFreetimeIndex);


                // looking for a fixedtime that migh be in the way. Make a boolean that becomes false if a fixedtime is in the way.
                boolean clearWay = true;
                for (int i = potentialFreetimeIndex - 2; i < mEvent.size() && clearWay; i += 2) {
                    Event iEvent = mEvent.get(i);
                    int duration = iEvent.getDuration();
                    int iTime = iEvent.getTime();
                    // If the i event ends before the event I want to insert is going to end, and that the i event ends after my fixedtime starts, then clearWay is false.
                    if (iTime + duration <= event.getFixedTime() + event.getDuration() &&
                            iTime + duration >= event.getFixedTime() &&
                            iEvent.getFixedTime() > 0) {
                        clearWay = false;
                    }
                }

                int freetimeDuration = 0;
                if (freetimeIndex != -1) {
                    freetimeDuration = mEvent.get(freetimeIndex).getDuration();
                }
                // if the duration is less then the freeTime and no fixedTime is in the way and the freetime is after then run this code.
                if (event.getDuration() < freetimeDuration && clearWay && freetimeIndex > potentialFreetimeIndex) {
                    // Now I come to the point where I need to insert the event at correct spot.
                    // fill up the time in the gap right before my fixedtime and the event before it.

                    int freeTimeBefore = event.getFixedTime() - potentialFreeEvent.getTime();
                    Log.i(TAG, "addEvent: Integer freeTimeBefore is: " + freeTimeBefore);
                    // minimizing the freetime.
                    mEvent.get(freetimeIndex).setDuration(freetimeDuration - (event.getDuration() + freeTimeBefore));
                    mEvent.get(freetimeIndex + 1).setDuration(freetimeDuration - (event.getDuration() + freeTimeBefore));

                    // add it at its fixedtime at it's place and do fillWithNewEvent in both side of the fixedTime.
                    Event freeEventBefore = new Event("FreeTime", freeTimeBefore, true);
                    freeEventBefore.setTime(event.getFixedTime() - freeTimeBefore);
                    if (freeTimeBefore > 0) {
                        mEvent.add(potentialFreetimeIndex, freeEventBefore);
                        mEvent.add(potentialFreetimeIndex, freeEventBefore);
                    }
                    event.setTime(event.getFixedTime());
                    mEvent.add(potentialFreetimeIndex + 2, event);
                    mEvent.add(potentialFreetimeIndex + 2, event);

                    for (Event event1 : mEvent) {
                        Log.i(TAG, "addEvent: '" + event1.getName() + "' at " + event1.getTime());
                    }
                    updateAllTimes();
                    notifyDataSetChanged();


                } else {
                    if (freetimeIndex < potentialFreetimeIndex) {
                        // remove the freetime
                        mEvent.remove(freetimeIndex);
                        mEvent.remove(freetimeIndex);
                        // use index of fixed time and put you're freetime there
                        int beforeFixedTimeIndex = findRecentFixedTime(potentialFreetimeIndex, true);
                        addFreeTime(freetimeDuration, beforeFixedTimeIndex);
                        // correctAllTimes
                        updateAllTimes();
                        // re-run the method
                        placeFixedTime(event);

                    }
                    // TODO: 2015-11-22 rename
                    Toast.makeText(mContext, R.string.exuse_of_fixedtime_placement, Toast.LENGTH_SHORT).show();
                    // Else let the user know and put it in the binedEvents list.
                    mBinedFixedEvents.add(event);
                    notifyDataSetChanged();
                }


            }
        } else {
            Toast.makeText(mContext, R.string.exuse_of_fixedtime_placement, Toast.LENGTH_SHORT).show();
            // Else let the user know and put it in the binedEvents list.
            mBinedFixedEvents.add(event);
        }
    }

    public void replaceEvent(int pos, boolean recycle) {
        // TODO: 2015-10-05 Should only add it if it is a real event and not a freeTime.
        Event event = mEvent.get(pos);
        int duration = event.getDuration();
        Log.i(TAG, "replaceEvent() called with: " + "pos = [" + pos + "]");

        int freetimePositionUp = findingFreetimePosition(pos, true);
        int freetimePositionDown = findingFreetimePosition(pos, false);
        Log.i(TAG, "replaceEvent: Found freetime at " + freetimePositionDown);
        Log.i(TAG, "replaceEvent: Found freetime at " + freetimePositionUp);
        if (freetimePositionUp != -1) {
            duration += mEvent.get(freetimePositionUp).getDuration();
        }
        if (freetimePositionDown != -1) {
            duration += mEvent.get(freetimePositionDown).getDuration();
            mEvent.remove(freetimePositionDown - 1);
            mEvent.remove(freetimePositionDown - 1);
            notifyItemRemoved(freetimePositionDown - 1);
            notifyItemRemoved(freetimePositionDown);
        }
        if (freetimePositionUp != -1) {
            mEvent.remove(freetimePositionUp - 1);
            mEvent.remove(freetimePositionUp - 1);
            pos -= 2;
            notifyItemRemoved(freetimePositionUp - 1);
            notifyItemRemoved(freetimePositionUp);
        }
        mEvent.remove(pos - 1);
        mEvent.remove(pos - 1);

        notifyItemRemoved(pos - 1);
        notifyItemRemoved(pos);

        //completelyDeleteEvent(Pos,true);
        addFreeTime(duration, pos - 1);
        updateAllTimes();
        notifyDataSetChanged();

        if (recycle && !event.isFreeTime()) {
            if (event.getFixedTime() > 0) {
                mBinedFixedEvents.add(event);
                Toast.makeText(mContext, R.string.adding_to_appointment_message, Toast.LENGTH_SHORT).show();

            } else {
                mReplaceList.add(event);
                Toast.makeText(mContext, R.string.adding_to_replacelist_message, Toast.LENGTH_SHORT).show();
            }

        }
        else if (!recycle) {
            mRemovedEvents.add(event);
        }


    }

    private void addFreeTime(int duration, int dataPos) {
        //Creating a freetime.
        Event freetime = new Event(mContext.getString(R.string.freetime_label), duration, true);
        // Adding freetime
        mEvent.add(dataPos, freetime);
        mEvent.add(dataPos, freetime);
        // notify the adapter
        notifyDataSetChanged();

    }

    public Bundle getEventQuene(int layoutPosition, boolean ordinary) {
        // TODO: 2015-09-23 Add the binedEvents and the events I have my overflow.
        ArrayList<Integer> eventQueneList = new ArrayList<>();

        Bundle bundle = new Bundle();

        if (ordinary) {
            for (Event event : mReplaceList) {
                eventQueneList.add(event.getId());
                bundle.putInt(Constants.FREETIME_DURATION, mEvent.get(layoutPosition).getDuration());
                bundle.putBoolean(Constants.ORDINARY_LIST, true);
            }
        } else {
            for (Event event : mBinedFixedEvents) {
                eventQueneList.add(event.getId());
                bundle.putInt(Constants.FREETIME_DURATION, -1);
                bundle.putBoolean(Constants.ORDINARY_LIST, false);
            }
        }
        bundle.putIntegerArrayList("event quene", eventQueneList);
        bundle.putInt("event position", layoutPosition);

        return bundle;
    }

    public Bundle communicateEventpreferences(Event event, long timeStamp) {
// FIXME: 2015-09-23 stupid to do this in the adapter. Clean up!
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


    private void setHighligth(Event chosen, int dataPos) {
        if (mAreaChoice) {
            boolean inSelectedArea = chosen.getIsInSelectedArea();
            if (!inSelectedArea) {
                searchAndSelectInArea(dataPos, true);
            } else {
                searchAndSelectInArea(dataPos, false);
            }
        }
    }

    public boolean move(int fromPos, int toPos) {

        if (mEvent.get(fromPos).isFixedTime()) {
            notifyDataSetChanged();
            Toast.makeText(mContext, R.string.exuse_when_moving_fixedtime, Toast.LENGTH_SHORT).show();
            return false;
        }

        int fromEventDuration = mEvent.get(fromPos).getDuration();


        int toTime = mEvent.get(toPos - 1).getTime();
        int fromTime = mEvent.get(fromPos - 1).getTime();

        int toTimeDuration = mEvent.get(toPos - 1).getDuration();
        int fromTimeDuration = mEvent.get(fromPos - 1).getDuration();

        // If the position that is to be replaced isn't a fixedTime, then change positions.
        if (!mEvent.get(toPos).isFixedTime()) {


            int direction = -1;
            int range = 1;
            if (fromPos > toPos) {
                // Drags UPWARD
                direction = -1;
            }
            if (toPos > fromPos) {
                // DOWNWARD
                direction = 1;
            }
            swappingEvent(fromPos, toPos, toTime, fromTime, toTimeDuration, fromTimeDuration, direction);
            Log.i(TAG, "move() ");
            for (Event event : mEvent) {
                Log.i(TAG, "move() " + event.getName());
                Log.i(TAG, "move() " + event.getTime());
            }
        } else {

            if (!mEvent.get(fromPos).isFreeTime()) {
                if (toPos == 1 || toPos == mEvent.size() - 1) {
                    return false;
                }


                int direction;


                if (fromPos > toPos) {
                    // UPWARD
                    direction = -1;
                    // I
                    findingAndChangingFreetime(toPos + 2, fromEventDuration, -1);
                    boolean keepGoing = findingAndChangingFreetime(toPos - 2, fromEventDuration, 1);
                    if (!keepGoing) {
                        Toast.makeText(mContext, R.string.exuse_when_to_big_message, Toast.LENGTH_SHORT).show();
                        notifyDataSetChanged();
                        return false;
                    }
                    swappingFixedEvent(fromPos, toPos, fromTime, toTimeDuration, direction);
                    //setTimes(toPos-2, true);
                    // correctAllTimesBeetweenTwoFixedTimes(findRecentFixedTime(toPos-2));
                    int wrongFixedTime = updateAllTimes();

                    fixingFixedTime(wrongFixedTime);

                    notifyDataSetChanged();
                    Log.i(TAG, "move: Duration of the moved Event: " + ConvertToTime.convertToTime(fromEventDuration));
                    Log.i(TAG, "move: Swapping '" + mEvent.get(fromPos).getName() + "' with '" + mEvent.get(toPos).getName() + "'");
                }

                if (toPos > fromPos) {
                    // DOWNWARD

                    direction = 1;
                    boolean keepGoing = findingAndChangingFreetime(toPos + 2, fromEventDuration, 1);
                    findingAndChangingFreetime(toPos - 2, fromEventDuration, -1);

                    if (!keepGoing) {
                        Toast.makeText(mContext, R.string.exuse_when_to_big_message, Toast.LENGTH_SHORT).show();
                        notifyDataSetChanged();
                        return false;
                    }

                    swappingFixedEvent(fromPos, toPos, fromTime, toTimeDuration, direction);

                    // I think the problem is that it fixing from the wrong startIndex.
                    //setTimes(fromPos-2, true);

                    for (Event event : mEvent) {
                        Log.i(TAG, "move() " + event.getName());
                        Log.i(TAG, "move() " + ConvertToTime.convertToTime(event.getTime()));
                    }

                    int wrongFixedTime = updateAllTimes();

                    fixingFixedTime(wrongFixedTime);

                    notifyDataSetChanged();
                    Log.i(TAG, "move: Duration of the moved Event: " + ConvertToTime.convertToTime(fromEventDuration));
                    Log.i(TAG, "move: Swapping '" + mEvent.get(fromPos).getName() + "' with '" + mEvent.get(toPos).getName() + "'");

                }
                Log.i(TAG, "move() ");
                for (Event event : mEvent) {
                    Log.i(TAG, "move() " + event.getName());
                    Log.i(TAG, "move() " + ConvertToTime.convertToTime(event.getTime()));
                }
            } else {
                Toast.makeText(mContext, R.string.exuse_for_impossible, Toast.LENGTH_SHORT).show();
                notifyDataSetChanged();
            }
        }

        return true;
    }

    private void fixingFixedTime(int wrongFixedTime) {
        if (wrongFixedTime != -1) {
            // Must check if viewType is 1.
            if (getItemViewType(wrongFixedTime) == 0) {
                wrongFixedTime++;
            }
            // saving the fixedTime before I remove it.
            Event event = mEvent.get(wrongFixedTime);
            // Removing the event.
            replaceEvent(wrongFixedTime, false);
            // adding the event aging
            addEvent(event, true);
            updateAllTimes();
        }
    }

    private void searchAndSelectInArea(Integer mEventPos, boolean inSelectedArea) {
        for (int i = mEventPos; i < mEvent.size() && mEvent.get(i).getFixedTime() == 0; i++) {
            mEvent.get(i).setInSelectedArea(inSelectedArea);

        }
        for (int i = mEventPos; i > 0 && mEvent.get(i).getFixedTime() == 0; i--) {
            mEvent.get(i).setInSelectedArea(inSelectedArea);

        }

        notifyDataSetChanged();
    }

    private int updateAllTimes() {
        int time = mEvent.get(0).getTime();
        int duration = mEvent.get(0).getDuration();
        for (int i = 2; i < mEvent.size(); i += 2) {
            // getting the event to change
            Event currentTime = mEvent.get(i);
            Event current = mEvent.get(i+1);
            // set it's time with the last events time and adding it's duration
            currentTime.setTime(time + duration);
            current.setTime(time + duration);
            // after I set the current event's time I update 'time' and 'duration' for the next event to use.
            Log.i(TAG, "updateAllTimes: Current: '" + current.getName() + "' time: " + time + " duration: " + duration);
            time = current.getTime();
            duration = current.getDuration();
        }
        for (int y = 0; y < mEvent.size(); y++) {
            Event event = mEvent.get(y);
            if (       event.isFixedTime()
                    && event.getFixedTime() != event.getTime()
                    && event.getId() != 1
                    && event.getId() != 2
                    ) {
                return y;
            }
        }
        return -1;
    }

    private boolean findingAndChangingFreetime(int fromDataPos, int fromEventDur, int addOrSub) {
        int freetimeDataPos = findingNearbyFreetimePosition(fromDataPos);
        // Here I calculate how much that will be subtracted.

        if (freetimeDataPos != -1) {
            if (addOrSub == -1) {
                changingFreetime(fromEventDur, addOrSub, freetimeDataPos);
            } else if (addOrSub == 1 && mEvent.get(freetimeDataPos).getDuration() > fromEventDur) {
                changingFreetime(fromEventDur, addOrSub, freetimeDataPos);
            } else {
                return false;
            }
        } else {
            if (addOrSub == -1) {
                return false;
            } else {
                fillWithNewEvents(fromDataPos, fromEventDur);
            }
        }
        return true;
    }

    private void changingFreetime(int fromEventDur, int addOrSub, int freetimeDataPos) {
        Event freeTimeEvent = mEvent.get(freetimeDataPos);
        Event freeTimeEventTime = mEvent.get(freetimeDataPos - 1);
        int durationToChange = freeTimeEvent.getDuration();
        Log.i(TAG, "findingAndChangingFreetime: Finding '" + freeTimeEvent.getName() + "' with duration of: " + ConvertToTime.convertToTime(durationToChange));
        Log.i(TAG, "findingAndChangingFreetime: Finding '" + freeTimeEventTime.getName() + "' with duration of: " + ConvertToTime.convertToTime(freeTimeEventTime.getDuration()));

        int howMuchToChange = fromEventDur * addOrSub;
        freeTimeEvent.setDuration(durationToChange - howMuchToChange);
        freeTimeEventTime.setDuration(durationToChange - howMuchToChange);

        Log.i(TAG, "findingAndChangingFreetime: changing with this amount: " + howMuchToChange);

        Log.i(TAG, "findingAndChangingFreetime: Changing duration of '" + freeTimeEventTime.getName() + "' at " + ConvertToTime.convertToTime(freeTimeEventTime.getTime()) + " to " + ConvertToTime.convertToTime(freeTimeEventTime.getDuration()));
        Log.i(TAG, "findingAndChangingFreetime: Changing changing of '" + freeTimeEvent.getName() + "' at " + ConvertToTime.convertToTime(freeTimeEvent.getTime()) + " to " + ConvertToTime.convertToTime(durationToChange));

        notifyItemChanged(freetimeDataPos);
        notifyItemChanged(freetimeDataPos - 1);
    }

    private boolean findingAndChangingFreetime(int fromDataPos, int fromEventDur) {
        int freetimeDataPos = findingNearbyFreetimePosition(fromDataPos);
        Log.i(TAG, "findingAndChangingFreetime Found freetime at position " + freetimeDataPos);
        // Here I calculate how much that will be subtracted.

        if (freetimeDataPos != -1) {
            Event event = deleteAndReplaceEvent(freetimeDataPos);
            int freetimeDuration = event.getDuration() + fromEventDur;

            fillWithNewEvents(freetimeDuration, freetimeDataPos);
            notifyItemChanged(freetimeDataPos);
            return true;
        } else {

            return false;
        }

    }

    private void swappingEvent(int fromPos, int toPos, int toTime, int fromTime, int toTimeDuration, int fromTimeDuration, int dir) {
        int newToTime = toTime - fromTimeDuration * dir;
        int newFromTime = fromTime + toTimeDuration * dir;

        Log.i(TAG, "swappingEvent: New 'to' time is " + newToTime);
        Log.i(TAG, "swappingEvent: New 'from' time is " + newFromTime);


        // Change the time before I switch the events.
        mEvent.get(toPos - 1).setTime(newToTime);
        mEvent.get(toPos).setTime(newToTime);
        mEvent.get(fromPos - 1).setTime(newFromTime);
        mEvent.get(fromPos).setTime(newFromTime);

        Event eventDraged = mEvent.get(fromPos);

        mEvent.remove(fromPos - 1);
        mEvent.remove(fromPos - 1);
        mEvent.add(toPos - 1, eventDraged);
        mEvent.add(toPos - 1, eventDraged);


        notifyItemChanged(toPos - 1);
        notifyItemChanged(fromPos - 1);
        notifyItemMoved(fromPos, toPos);

    }

    private void swappingFixedEvent(int fromPos, int toPos, int fromTime, int toTimeDuration, int dir) {
        //  int newToTime = toTime - fromTimeDuration*dir;
        // TODO: 2015-10-09 The swapping can only be called if there is freeTime avalable for the swapped event.
        int newFromTime = fromTime + toTimeDuration * dir;
        if (dir == -1) {
            newFromTime = mEvent.get(toPos).getFixedTime() - mEvent.get(fromPos).getDuration();
        } else {
            newFromTime = mEvent.get(toPos).getFixedTime() + mEvent.get(fromPos).getDuration() + mEvent.get(toPos).getDuration();
        }

        //   Log.i(TAG, "swappingEvent: New 'to' time is " + newToTime);
        Log.i(TAG, "swappingEvent: New 'from' time is " + ConvertToTime.convertToTime(newFromTime));


        // Change the time before I switch the events.
        mEvent.get(fromPos - 1).setTime(newFromTime);
        mEvent.get(fromPos).setTime(newFromTime);

        Event eventDraged = mEvent.get(fromPos);

        mEvent.remove(fromPos - 1);
        mEvent.remove(fromPos - 1);
        mEvent.add(toPos - 1, eventDraged);
        mEvent.add(toPos - 1, eventDraged);


        notifyItemChanged(toPos - 1);
        notifyItemChanged(fromPos - 1);
        notifyItemMoved(fromPos, toPos);

    }

    public void setCalendar(Calendar calendar) {
        mCalendar = calendar;
    }

    public void saveDate(EventDataSource datasource) {
        // TODO: 2015-09-30 Check if the second list really is the correct replace list.
        datasource.saveDate(mCalendar, mEvent, mReplaceList);
    }

    public Event removeEvent(int eventId) {
        Event deleteEvent = new Event("Failed to find any", 1);
        for (Event event : mReplaceList) {
            if (event.getId() == eventId) {
                deleteEvent = event;
            }
        }
        if (!deleteEvent.getName().equals("Failed to find any")) {
            mReplaceList.remove(deleteEvent);
            mReplaceList.remove(deleteEvent);
        } else {
            for (Event event : mBinedFixedEvents) {
                if (event.getId() == eventId) {
                    deleteEvent = event;
                }
            }
        }
        if (!deleteEvent.getName().equals("Failed to find any")) {
            mBinedFixedEvents.remove(deleteEvent);
            mBinedFixedEvents.remove(deleteEvent);
        }
        Log.i(TAG, "removeEvent Removed event " + deleteEvent.getName() + " from my replaceList");
        return deleteEvent;
    }

    public void removeFreetime(int evenId, int pos) {
        Log.i(TAG, "removeFreetime() called with: " + "evenId = [" + evenId + "], pos = [" + pos + "]");
        Event event = removeEvent(evenId);
        int eventDuration = event.getDuration();
        // if the chosen event is smaler then the freetime then you simply remove the freetime and fill it up.
        if (event.getDuration() < mEvent.get(pos).getDuration()) {
            int replacedDuration = mEvent.get(pos).getDuration();
            mEvent.remove(pos - 1);
            mEvent.remove(pos - 1);

            notifyItemRemoved(pos);
            notifyItemRemoved(pos + 1);

            addFreeTime(replacedDuration - eventDuration, pos - 1);
            mEvent.add(pos - 1, event);
            mEvent.add(pos - 1, event);
            updateAllTimes();
            notifyDataSetChanged();
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.exuse_when_to_big_message), Toast.LENGTH_SHORT).show();
            // TODO: 2015-10-31 Implement chooseEventsToReplace when it's stable.
            //choseEventsToReplace(0, true);
        }


    }

    public void reload(ArrayList<Event> event) {
        // TODO: 2015-10-18 Implement a dialog that let the user choes if it's a complete replan or only reorder the ones in your list.
        /*
        ArrayList<Event> newList = new ArrayList<>();
        for (int i = 0; i < mEvent.size(); i += 2) {
            newList.add(mEvent.get(i));
        }
        newList.addAll(mBinedFixedEvents);
        newList.addAll(mReplaceList);
        */
        changeStartTime(event);
        update(event);
        notifyDataSetChanged();
    }

    public Event getEventFromId(int evenId) {
        for (Event event : mReplaceList) {
            if (event.getId() == evenId) {
                Log.i(TAG, "getEventFromId: Found in Replacelist");

                return event;
            }
        }
        for (Event binedFixedEvent : mBinedFixedEvents) {
            if (binedFixedEvent.getId() == evenId) {
                Log.i(TAG, "getEventFromId: Found in mu eventBin");

                return binedFixedEvent;
            }
        }
        for (Event event : mEvent) {
            if (event.getId() == evenId) {
                Log.i(TAG, "getEventFromId: Found in mEvent");
                return event;
            }
        }
        return null;
    }

    public void updateMorning(int totalPixelMinutes) {

        // check if there is a freetime around and save bothe place and its duration.
        int freetimePos = findingFreetimePosition(0, false);

        // change morning duration if it is time. If
        int originalMorningDur = mEvent.get(0).getDuration();
        // a positive number if later on day and negative if earlier.
        int deltaDur = totalPixelMinutes - originalMorningDur;

        if (freetimePos != -1) {
            int freetimeDuration = mEvent.get(freetimePos).getDuration();

            // change the time of the morning with the difference of old morning duration with the new one.
            if (deltaDur > 0 && deltaDur < freetimeDuration) {
                changeMorning(totalPixelMinutes, freetimePos, freetimeDuration, deltaDur);
            } else if (deltaDur <= 0) {
                changeMorning(totalPixelMinutes, freetimePos, freetimeDuration, deltaDur);
            } else {
                mEvent.get(0).setDuration(originalMorningDur + freetimeDuration);
                mEvent.get(1).setDuration(originalMorningDur + freetimeDuration);
                mEvent.remove(freetimePos);
                mEvent.remove(freetimePos);
                Toast.makeText(mContext, mContext.getString(R.string.exuse_for_clock) + ConvertToTime.convertToTime(originalMorningDur + freetimeDuration), Toast.LENGTH_SHORT).show();
            }
            // else the freetime is removed and morning consumes its place.
            // correcting all times in the list.
            updateAllTimes();
            notifyDataSetChanged();
        } else {
            if (deltaDur <= 0) {
                mEvent.get(0).setDuration(totalPixelMinutes);
                mEvent.get(1).setDuration(totalPixelMinutes);
                addFreeTime(deltaDur * (-1), 2);
                updateAllTimes();
                notifyDataSetChanged();
            } else {
                Toast.makeText(mContext, R.string.exuse_for_clock_not_moving, Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void changeMorning(int totalPixelMinutes, int freetimePos, int freetimeDuration, int deltaDur) {
        mEvent.get(0).setDuration(totalPixelMinutes);
        mEvent.get(1).setDuration(totalPixelMinutes);
        mEvent.get(freetimePos).setDuration(freetimeDuration - deltaDur);
        mEvent.get(freetimePos + 1).setDuration(freetimeDuration - deltaDur);
    }

    private void changeNight(int totalPixelMinutes, int freetimePos, int freetimeDuration, int deltaDur) {
        mEvent.get(getItemCount()-2).setDuration(totalPixelMinutes);
        mEvent.get(getItemCount()-1).setDuration(totalPixelMinutes);
        mEvent.get(freetimePos).setDuration(freetimeDuration - deltaDur);
        mEvent.get(freetimePos - 1).setDuration(freetimeDuration - deltaDur);
        notifyItemChanged(getItemCount() - 1);
        notifyItemChanged(getItemCount() - 2);
    }




    public boolean replacingFixedTime(Event event) {

        removeEvent(event.getId());
        replaceEvent(mReplaceIndex, false);


        /*
        int id = event.getId();
        Log.i(TAG, "replacingFixedTime: loking for " + event.getName() + ".");
        for (int i = 0; i < mEvent.size(); i += 2) {
            Event current = mEvent.get(i);
            if (current.getId() == id) {
                if (current.getFixedTime() == event.getFixedTime()
                        && current.getDuration() == event.getDuration()) {
                    current.setName(event.getName());
                    current.setRecurring(event.getRecurring());
                    notifyItemChanged(i);
                    Log.i(TAG, "replacingFixedTime: Changed name of " + current.getName() + " to " + event.getName());
                    return false;
                } else if (current.getFixedTime() == event.getFixedTime()
                        && current.getDuration() != event.getDuration()) {

                    int freeTimeIndex = findingFreetimePosition(i, false);
                    int timeToChange = current.getDuration() - event.getDuration();

                    current.setDuration(event.getDuration());
                    mEvent.get(i+1).setDuration(event.getDuration());
                    current.setRecurring(event.getRecurring());
                    notifyItemChanged(i);
                    notifyItemChanged(i + 1);

                    if (freeTimeIndex != -1) {
                        Event freeEvent = mEvent.get(freeTimeIndex);
                        Event freeEventTime = mEvent.get(freeTimeIndex + 1);
                        if (freeEvent.getDuration() > timeToChange) {
                            int newFreetimeDuration = freeEvent.getDuration() + timeToChange;
                            freeEvent.setDuration(newFreetimeDuration);
                            freeEventTime.setDuration(newFreetimeDuration);
                            notifyItemChanged(freeTimeIndex);
                            notifyItemChanged(freeTimeIndex + 1);
                            updateAllTimes();
                            notifyDataSetChanged();
                            return true;
                        } else if (freeEvent.getDuration() == timeToChange) {
                            mEvent.remove(freeTimeIndex);
                            mEvent.remove(freeTimeIndex);
                            notifyItemRemoved(freeTimeIndex);
                            notifyItemRemoved(freeTimeIndex);
                            updateAllTimes();
                            notifyDataSetChanged();
                            return true;
                        } else if (freeEvent.getDuration() < timeToChange) {
                            Log.i(TAG, "replacingFixedTime: removing event: " + current.getName() + " and putting in binned list.");
                            replaceEvent(i + 1, true);
                            return false;
                        }
                    }
                } else {
                    // Remove event and put it in replacelist.
                    Log.i(TAG, "replacingFixedTime: removing event: " + current.getName() + " and putting in binned list.");
                    mEvent.remove(i);
                    mEvent.remove(i);
                    mEvent.add(i, event);
                    mEvent.add(i, event);

                    replaceEvent(i + 1, true);
                    removeEvent(event.getId());
                    addEvent(event, false);
                    return false;
                }
            }
        }

*/
        return false;


    }

    public void updateNight(int totalPixelMinutes) {
        // check if there is a freetime around and save bothe place and its duration.
        int nigthPos = getItemCount()-1;
        int freetimePos = findingFreetimePosition(nigthPos, true);

        // change morning duration if it is time. If
        int originalNightDur = mEvent.get(nigthPos).getDuration();
        int originalNightTime = mEvent.get(nigthPos).getFixedTime();
        // setting the fixedtime for my nighttime.
        // FIXME: 2015-10-24 totalpixelMinutes is not the duartion of the nigt but the duration until the night starts.
        int nightDuration = 24*Constants.HOUR - totalPixelMinutes;

        // a positive number if later on day and negative if earlier.
        int deltaDur = nightDuration - originalNightDur;

        if (freetimePos != -1) {
            int freetimeDuration = mEvent.get(freetimePos).getDuration();

            // change the time of the morning with the difference of old morning duration with the new one.
            if (deltaDur > 0 && deltaDur < freetimeDuration) {
                changeNight(nightDuration, freetimePos, freetimeDuration, deltaDur);
                mEvent.get(nigthPos-1).setFixedTime(totalPixelMinutes);
                mEvent.get(nigthPos).setFixedTime(totalPixelMinutes);
            } else if (deltaDur <= 0) {
                changeNight(nightDuration, freetimePos, freetimeDuration, deltaDur);
                mEvent.get(nigthPos-1).setFixedTime(totalPixelMinutes);
                mEvent.get(nigthPos).setFixedTime(totalPixelMinutes);
            } else {
                mEvent.get(nigthPos-1).setDuration(originalNightDur + freetimeDuration);
                mEvent.get(nigthPos).setDuration(originalNightDur + freetimeDuration);

                mEvent.get(nigthPos-1).setFixedTime(originalNightTime - freetimeDuration);
                mEvent.get(nigthPos).setFixedTime(originalNightTime - freetimeDuration);
                mEvent.remove(freetimePos);
                mEvent.remove(freetimePos-1);
                Toast.makeText(mContext, R.string.exuse_for_clock + ConvertToTime.convertToTime(originalNightDur + freetimeDuration), Toast.LENGTH_SHORT).show();
            }
            // else the freetime is removed and morning consumes its place.
            // correcting all times in the list.
            updateAllTimes();
            notifyDataSetChanged();
        } else {
            if (deltaDur <= 0) {
                mEvent.get(nigthPos-1).setDuration(nightDuration);
                mEvent.get(nigthPos-1).setFixedTime(totalPixelMinutes);
                mEvent.get(nigthPos).setDuration(nightDuration);
                mEvent.get(nigthPos).setFixedTime(totalPixelMinutes);
                addFreeTime(deltaDur * (-1), nigthPos - 1);
                updateAllTimes();
                notifyDataSetChanged();
            } else {
                Toast.makeText(mContext, R.string.exuse_for_clock_not_moving, Toast.LENGTH_SHORT).show();

            }
        }

    }

    public int getNightTime() {
        return mEvent.get(getItemCount()-1).getFixedTime();
    }


    public void resetEvents() {
        for (Event removedEvent : mRemovedEvents) {
            if (removedEvent.isFixedTime()) {
                mBinedFixedEvents.add(removedEvent);
            } else {
                mReplaceList.add(removedEvent);
            }
        }
        mRemovedEvents.clear();
    }


    public class EventViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        public TextView
                mEventLabel,
                mRefillBtn;
        public ImageView
                mTravelFromIcon,
                mTravelToIcon,
                mSunDrawable;



        public EventViewHolder(View itemView) {
            super(itemView);

            mEventLabel = (TextView) itemView.findViewById(R.id.event_text);
            mTravelFromIcon = (ImageView) itemView.findViewById(R.id.travel_image_to);
            mSunDrawable = (ImageView) itemView.findViewById(R.id.sun_draw);
            mTravelToIcon = (ImageView) itemView.findViewById(R.id.travel_image_from);
            mRefillBtn = (TextView) itemView.findViewById(R.id.fillup_button);




            itemView.setOnClickListener(this);
        }

        public void bindHour(String nameText, final int duration, final int position, int time) {
            if (getItemViewType() == 1) {
                mRefillBtn.setVisibility(View.GONE);
                if (duration > HOUR/2) {
                    itemView.setMinimumHeight(HOUR/2);
                    mEventLabel.setText(nameText + "  " + ConvertToTime.convertToTime(duration) + mContext.getString(R.string.long_duration_extra_label));

                }
                else {
                    itemView.setMinimumHeight(duration);
                    mEventLabel.setText(nameText);
                }
                Event currentEvent = mEvent.get(getAdapterPosition());
                if (mEvent.get(position).isFixedTime()) {
                    itemView.setBackgroundColor(currentEvent.getColor());
                } else {
                    // If the current event is a normal event it's normal color.
                    itemView.setBackgroundColor(currentEvent.getColor());
                }
                mEventLabel.setMinimumWidth(150);

                if (position == 1 || position == mEvent.size() - 1) {
                    mEventLabel.setText("");
                    mSunDrawable.setVisibility(View.VISIBLE);
                } else {
                    mSunDrawable.setVisibility(View.GONE);
                }

                // Make the travel icons appear if there is any traveltime.
                if (mEvent.get(position).getTravelDurationFrom() > 0) {
                    mTravelFromIcon.setVisibility(View.VISIBLE);
                } else {
                    mTravelFromIcon.setVisibility(View.GONE);
                }

                if (mEvent.get(position).getTravelDurationTo() > 0) {
                    mTravelToIcon.setVisibility(View.VISIBLE);
                } else {
                    mTravelToIcon.setVisibility(View.GONE);
                }
            }

            if (getItemViewType() == 0) {
                mEventLabel.setText(ConvertToTime.convertToTime(time) + "");
                mEventLabel.setMinimumWidth(50);
                if (position == 0) {
                    mEventLabel.setText("");
                    //itemView.setBackgroundColor(mEvent.get(position()).getColor());
                }
            }


            if (mEvent.get(position).isFreeTime() && getItemViewType() == 1) {
                mRefillBtn.setVisibility(View.VISIBLE);
                int size = mReplaceList.size();
                mRefillBtn.setText(size + " ");
                mRefillBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Save duration for freetime.
                        int duration = mEvent.get(position).getDuration();
                        // Remove the freetime
                        mEvent.remove(position - 1);
                        mEvent.remove(position - 1);
                        // fillup the empty space.
                        fillWithNewEvents(duration, position);

                        notifyDataSetChanged();
                    }
                });
            }



        }

        @Override
        public void onClick(View v) {
            Event chosen = mEvent.get(getLayoutPosition());

                //choseEventsToReplace(getLayoutPosition());
                mCommunicator = (Communicator) mContext;

                if (getItemViewType() == 1) {
                    if (mEvent.get(getLayoutPosition()).isFreeTime()) {
                        Bundle bundle = getEventQuene(getLayoutPosition(), true);
                        mCommunicator.startEventQueneFragment(bundle);

                    } else if (getLayoutPosition() == 1) {
                        mCommunicator.changeMorningTime(true);
                    } else if (getLayoutPosition() == mEvent.size() - 1) {
                        mCommunicator.changeMorningTime(false);
                    } else {
                        // Starting NewEventFrag whit a bundle.
                        Bundle bundle = new Bundle();

                        bundle.putString(Constants.NEW_EVENT_NAME, chosen.getName());
                        bundle.putString(Constants.REPEATING, chosen.getRecurring());
                        bundle.putInt(Constants.FIXEDTIME_VALUE, chosen.getFixedTime());
                        bundle.putInt(Constants.NEW_MINUTE, ConvertToTime.getMinute(chosen.getDuration()));
                        bundle.putInt(Constants.NEW_HOUR, ConvertToTime.getHour(chosen.getDuration()));
                        bundle.putInt(Constants.TIMESTAMP, (int) chosen.getStartTimeStamp());
                        bundle.putBoolean(Constants.NEW_EVENT_BOOLEAN, false);
                        bundle.putInt(Constants.EVENT_POSITION, getLayoutPosition());
                        bundle.putInt(Constants.NEW_EVENT_ID, mEvent.get(getLayoutPosition()).getId());
                        bundle.putInt(Constants.TRAVEL_DURATION_FROM, mEvent.get(getLayoutPosition()).getTravelDurationFrom());
                        bundle.putInt(Constants.TRAVEL_DURATION_TO, mEvent.get(getLayoutPosition()).getTravelDurationTo());


                        mNeededDur = chosen.getDuration();

                        mCommunicator.startNewEventFragment(bundle);

                        if (chosen.isFixedTime()) {
                            mReplaceIndex = getLayoutPosition();
                        }
                    }
                }
        }

        private void setHighligth(Event chosen) {
            if (mAreaChoice) {
                boolean inSelectedArea = chosen.getIsInSelectedArea();
                if (!inSelectedArea) {


                    searchAndSelectInArea(getLayoutPosition(), true);


                }
                // Todo: Because I only want the selected area to be deselected when there is no events left in the mEventQueneRef then
                else if (mEventsToBeReplacedRef.size() == 0) {


                    searchAndSelectInArea(getLayoutPosition(), false);
                }

            }
        }

    }


}
