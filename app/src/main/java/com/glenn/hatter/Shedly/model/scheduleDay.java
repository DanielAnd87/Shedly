package com.glenn.hatter.Shedly.model;

import android.content.res.Resources;
import android.util.Log;

import com.glenn.hatter.Shedly.R;
import com.glenn.hatter.Shedly.constants.Constants;
import com.glenn.hatter.Shedly.data.Event;
import com.glenn.hatter.Shedly.ui.MainActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by hatter on 2016-02-09.
 */
public class scheduleDay {

    private static final String TAG = MainActivity.class.getSimpleName();


    private ArrayList<Event> mEvent = new ArrayList<>();
    private ArrayList<Event> mReplaceList = new ArrayList<>();
    private ArrayList<Event> mRemovedEvents = new ArrayList<>();
    private ArrayList<Event> mOrdinaryEventQueue = new ArrayList<>();
    private ArrayList<Event> mBinedFixedEvents = new ArrayList<>();

    public scheduleDay(ArrayList<Event> event) {
        // TODO: 2016-02-10 initiate all fields here
        update(event);
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
                    int fixedTime = currentEndTime + (5 * Constants.MINUTE);
                    after.setFixedTime(fixedTime);
                    // Set the duration such that it end at midnight.
                    after.setDuration(24 * Constants.HOUR - fixedTime);
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
        // TODO: 2016-02-10 Find a way to get resourses without using Context, perhaps I can send it when I constrict the class since it is only one string?
        //Event freetime = new Event(Resources.getSystem().getString(R.string.freetime_label), duration, true);
        Event freetime = new Event("", duration, true);
        freetime.setTime(time);
        // Adding freetime
        mEvent.add(dataPos, freetime);
        mEvent.add(dataPos, freetime);
    }

    private int addingFixedTime(int time) {
        mEvent.add(mReplaceList.get(0));
        mEvent.add(mReplaceList.get(0));

        mReplaceList.remove(0);
        mEvent.get(mEvent.size() - 1).setTime(time);
        mEvent.get(mEvent.size() - 2).setTime(time);


        return mEvent.size() - 2;
    }

    private void sortForReplaceList() {
        for (int i = 0; i < mReplaceList.size(); i++) {
            Log.i(TAG, mReplaceList.get(i).getName() + " with the fixedTime at" + mReplaceList.get(i).getFixedTime() + ".");
        }
    }

    public ArrayList<Event> getEvent() {
        return mEvent;
    }

    public ArrayList<Event> getReplaceList() {
        return mReplaceList;
    }

    public ArrayList<Event> getRemovedEvents() {
        return mRemovedEvents;
    }

    public ArrayList<Event> getOrdinaryEventQueue() {
        return mOrdinaryEventQueue;
    }

    public ArrayList<Event> getBinedFixedEvents() {
        return mBinedFixedEvents;
    }


}
