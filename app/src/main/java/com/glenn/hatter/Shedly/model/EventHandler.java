package com.glenn.hatter.Shedly.model;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.glenn.hatter.Shedly.R;
import com.glenn.hatter.Shedly.adapters.EventAdapter;
import com.glenn.hatter.Shedly.constants.Constants;
import com.glenn.hatter.Shedly.data.Event;
import com.glenn.hatter.Shedly.data.db.EventDataSource;
import com.glenn.hatter.Shedly.ui.MainActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by hatter on 2016-02-02.
 */
public class EventHandler {
    private static final String TAG = MainActivity.class.getSimpleName();

    private ArrayList<Event> mEvent = new ArrayList<>();
    private ArrayList<Event> mReplaceList = new ArrayList<>();
    private ArrayList<Event> mRemovedEvents = new ArrayList<>();


    private int mReplaceIndex = -1;

    // for now I replace mReplaceList where it is needed.
    private ArrayList<Event> mOrdinaryEventQueue = new ArrayList<>();
    private ArrayList<Event> mBinedFixedEvents = new ArrayList<>();

    private EventDataSource mDatasource;
    private Calendar mCalendar;

    private Context mContext;

    public EventHandler(Context context, EventDataSource datasource, Calendar calendar, ArrayList<Event> list) {
        mDatasource = datasource;
        mCalendar = calendar;
        mContext = context;
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

    public boolean move(int fromPos, int toPos) {

        int fromEventDuration = mEvent.get(fromPos).getDuration();


        int toTime = mEvent.get(toPos - 1).getTime();
        int fromTime = mEvent.get(fromPos - 1).getTime();

        int toTimeDuration = mEvent.get(toPos - 1).getDuration();
        int fromTimeDuration = mEvent.get(fromPos - 1).getDuration();

        // If the position that is to be replaced isn't a fixedTime, then change positions.
        if (!mEvent.get(toPos).isFixedTime()) {
            int direction = -1;
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
                        return false;
                    }
                    swappingFixedEvent(fromPos, toPos, fromTime, toTimeDuration, direction);
                    //setTimes(toPos-2, true);
                    // correctAllTimesBeetweenTwoFixedTimes(findRecentFixedTime(toPos-2));
                    int wrongFixedTime = updateAllTimes();

                    fixingFixedTime(wrongFixedTime);

                    //mAdapterContext.notifyDataSetChanged();
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

                    //mAdapterContext.notifyDataSetChanged();
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
                return false;
            }
        }

        return true;
    }



    private void fixingFixedTime(int wrongFixedTime) {
        if (wrongFixedTime != -1) {
            // Must check if viewType is 1.
            if (wrongFixedTime % 2 == 0) {
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

    // FIXME: 2016-02-02 UNTESTED and very unreliable!
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
                Log.i(TAG, "addEvent: Removing '" + mEvent.get(potentialFreetimeIndex).getName() + "' at " + ConvertToTime.convertToTime(potentialFreeEvent.getTime()));
                mEvent.remove(potentialFreetimeIndex);
                Log.i(TAG, "addEvent: Removing '" + mEvent.get(potentialFreetimeIndex).getName() + "' at " + ConvertToTime.convertToTime(potentialFreeEvent.getTime()));
                Event freeEventBefore = new Event("FreeTime", freeTimeBefore, true);
                freeEventBefore.setTime(event.getFixedTime() - freeTimeBefore);
                mEvent.add(potentialFreetimeIndex, freeEventBefore);


                mEvent.add(potentialFreetimeIndex, freeEventBefore);

                event.setTime(event.getFixedTime());
                mEvent.add(potentialFreetimeIndex + 2, event);
                mEvent.add(potentialFreetimeIndex + 2, event);
                Event freeEventAfter = new Event("FreeTime", freeTimeAfter, true);
                freeEventAfter.setTime(event.getTime() + event.getDuration());
                mEvent.add(potentialFreetimeIndex + 4, freeEventAfter);
                mEvent.add(potentialFreetimeIndex + 4, freeEventAfter);
                for (Event event1 : mEvent) {
                    Log.i(TAG, "addEvent: '" + event1.getName() + "' at " + event1.getTime());
                }
                removeEventFromBin(event);
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
                    removeEventFromBin(event);


                    updateAllTimes();

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
                        removeEventFromBin(event);
                    }
                    // TODO: 2015-11-22 rename
                    Toast.makeText(mContext, R.string.exuse_of_fixedtime_placement, Toast.LENGTH_SHORT).show();
                    // Else let the user know and put it in the binedEvents list.
                    mBinedFixedEvents.add(event);
                }


            }
        } else {
            Toast.makeText(mContext, R.string.exuse_of_fixedtime_placement, Toast.LENGTH_SHORT).show();
            // Else let the user know and put it in the binedEvents list.
            mBinedFixedEvents.add(event);
        }
    }

    private void removeEventFromBin(Event event) {
        // Remove event from eventbin because of succes.
        for (int i = 0; i < mBinedFixedEvents.size(); i++) {
            if (mBinedFixedEvents.get(i).getId() == event.getId()) {
                mBinedFixedEvents.remove(i);
                i--;
            }
        }
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
        }
        if (freetimePositionUp != -1) {
            mEvent.remove(freetimePositionUp - 1);
            mEvent.remove(freetimePositionUp - 1);
            pos -= 2;
        }
        mEvent.remove(pos - 1);
        mEvent.remove(pos - 1);


        //completelyDeleteEvent(Pos,true);
        addFreeTime(duration, pos - 1);
        updateAllTimes();

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


    public boolean replacingFixedTime(Event event) {

        removeEvent(event.getId());
        replaceEvent(mReplaceIndex, false);
        return false;
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

    private void swappingFixedEvent(int fromPos, int toPos, int fromTime, int toTimeDuration, int dir) {
        //  int newToTime = toTime - fromTimeDuration*dir;
        // The swapping can only be called if there is freeTime avalable for the swapped event.
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

/*
        mAdapterContext.notifyItemChanged(toPos - 1);
        mAdapterContext.notifyItemChanged(fromPos - 1);
        mAdapterContext.notifyItemMoved(fromPos, toPos);
*/
    }

    private int addingFixedTime(int time) {
        mEvent.add(mReplaceList.get(0));
        mEvent.add(mReplaceList.get(0));

        mReplaceList.remove(0);
        mEvent.get(mEvent.size() - 1).setTime(time);
        mEvent.get(mEvent.size() - 2).setTime(time);


        return mEvent.size() - 2;
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

    private void sortForReplaceList() {
        for (int i = 0; i < mReplaceList.size(); i++) {
            Log.i(TAG, mReplaceList.get(i).getName() + " with the fixedTime at" + mReplaceList.get(i).getFixedTime() + ".");
        }
    }

    private int addFillup(int time, int eventPlace, int timeLeft) {
        addFreeTime(timeLeft, eventPlace, time);
        time += timeLeft;
        mEvent.get(mEvent.size() - 1).setTime(time);
        mEvent.get(mEvent.size() - 2).setTime(time);
        Log.i(TAG, "addFillup: Adding a freeTime at " + mEvent.get(mEvent.size() - 3).getTime() + " with the duration of " + mEvent.get(mEvent.size() - 3).getDuration() + ".");
        return time;
    }
    private void addFreeTime(int duration, int dataPos) {
        //Creating a freetime.
        Event freetime = new Event(mContext.getString(R.string.freetime_label), duration, true);
        // Adding freetime
        mEvent.add(dataPos, freetime);
        mEvent.add(dataPos, freetime);
    }


    private void addFreeTime(int duration, int dataPos, int time) {
        //Creating a freetime.
        Event freetime = new Event(mContext.getString(R.string.freetime_label), duration, true);
        freetime.setTime(time);
        // Adding freetime
        mEvent.add(dataPos, freetime);
        mEvent.add(dataPos, freetime);
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

    public void fillWithNewEvents(int duration, int index) {
        // The unused space should be filled with new events
        int remainingSpaceToFillup = duration;
        if (duration == 0) {
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
    private boolean findingAndChangingFreetime(int fromDataPos, int fromEventDur) {
        int freetimeDataPos = findingNearbyFreetimePosition(fromDataPos);
        Log.i(TAG, "findingAndChangingFreetime Found freetime at position " + freetimeDataPos);
        // Here I calculate how much that will be subtracted.

        if (freetimeDataPos != -1) {
            Event event = deleteAndReplaceEvent(freetimeDataPos);
            int freetimeDuration = event.getDuration() + fromEventDur;

            fillWithNewEvents(freetimeDuration, freetimeDataPos);
            return true;
        } else {

            return false;
        }

    }
    private Event deleteAndReplaceEvent(int position) {
        Event deletedEvent = mEvent.get(position);
        mEvent.remove(position);
        mEvent.remove(position - 1);
        return deletedEvent;
    }

    public void removeAndFillUp(int position) {
        // Save duration for freetime.
        int duration = mEvent.get(position).getDuration();
        // Remove the freetime
        mEvent.remove(position - 1);
        mEvent.remove(position - 1);
        // fillup the empty space.
        fillWithNewEvents(duration, position);
    }


    public void saveDate() {
        mDatasource.saveDate(mCalendar, mEvent, mReplaceList);
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

            addFreeTime(replacedDuration - eventDuration, pos - 1);
            mEvent.add(pos - 1, event);
            mEvent.add(pos - 1, event);
            updateAllTimes();
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.exuse_when_to_big_message), Toast.LENGTH_SHORT).show();
            // TODO: 2015-10-31 Implement chooseEventsToReplace when it's stable.
            //choseEventsToReplace(0, true);
        }


    }

    public void updateNight(int totalPixelMinutes, int nigthPos, int itemCount) {
        // check if there is a freetime around and save bothe place and its duration.
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
                changeNight(nightDuration, freetimePos, freetimeDuration, deltaDur, itemCount);
                mEvent.get(nigthPos - 1).setFixedTime(totalPixelMinutes);
                mEvent.get(nigthPos).setFixedTime(totalPixelMinutes);
            } else if (deltaDur <= 0) {
                changeNight(nightDuration, freetimePos, freetimeDuration, deltaDur, itemCount);
                mEvent.get(nigthPos - 1).setFixedTime(totalPixelMinutes);
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
        } else {
            if (deltaDur <= 0) {
                mEvent.get(nigthPos-1).setDuration(nightDuration);
                mEvent.get(nigthPos-1).setFixedTime(totalPixelMinutes);
                mEvent.get(nigthPos).setDuration(nightDuration);
                mEvent.get(nigthPos).setFixedTime(totalPixelMinutes);
                addFreeTime(deltaDur * (-1), nigthPos - 1);
                updateAllTimes();
            } else {
                Toast.makeText(mContext, R.string.exuse_for_clock_not_moving, Toast.LENGTH_SHORT).show();

            }
        }

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
        } else {
            if (deltaDur <= 0) {
                mEvent.get(0).setDuration(totalPixelMinutes);
                mEvent.get(1).setDuration(totalPixelMinutes);
                addFreeTime(deltaDur * (-1), 2);
                updateAllTimes();
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

    private void changeNight(int totalPixelMinutes, int freetimePos, int freetimeDuration, int deltaDur, int itemCount) {
        mEvent.get(itemCount - 2).setDuration(totalPixelMinutes);
        mEvent.get(itemCount - 1).setDuration(totalPixelMinutes);
        mEvent.get(freetimePos).setDuration(freetimeDuration - deltaDur);
        mEvent.get(freetimePos - 1).setDuration(freetimeDuration - deltaDur);
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

    public Bundle getEventQuene(int layoutPosition, boolean ordinary) {
        // TODO: 2015-09-23 Add the binedEvents and the events I have my overflow.
        ArrayList<Integer> eventQueneList = new ArrayList<>();

        Bundle bundle = new Bundle();

        // Adding both my ordinary replace list and my bined events to the bundle.
        for (Event event : mReplaceList) {
            eventQueneList.add(event.getId());
        }
        for (Event event : mBinedFixedEvents) {
            eventQueneList.add(event.getId());
        }

        bundle.putInt(Constants.FREETIME_DURATION, mEvent.get(layoutPosition).getDuration());
        bundle.putBoolean(Constants.ORDINARY_LIST, true);
        bundle.putIntegerArrayList("event quene", eventQueneList);
        bundle.putInt("event position", layoutPosition);

        return bundle;
    }


    public void addAndSetTimeForNewEvent(Event event, int LowEventIndex) {
        // Here I add a brand new reference and insert it at correct index
        // todo: When I replace events I'm only going to check from the lowest index to the higest index, and as a safety percation  I migth add a quality check to a few following indexes.

        mEvent.add(LowEventIndex - 1, event);
        mEvent.add(LowEventIndex - 1, event);

        //setTimes(LowEventIndex, true);
        updateAllTimes();

        Log.i(TAG, "addAndSetTimeForNewEvent");
        Log.i(TAG, "Adding one event");
        for (Event event1 : mEvent) {
            Log.i(TAG, ConvertToTime.convertToTime(event1.getTime()) + "");
            Log.i(TAG, event1.getName() + "");
        }
        Log.i(TAG, "addAndSetTimeForNewEvent() called with: " + "event = [" + event.getName() + "], LowEventIndex = [" + LowEventIndex + "]");
    }

        // My Getters for all lists.
    public ArrayList<Event> getEvents() {
        return mEvent;
    }
    public Event getEvent(int pos) {
        return mEvent.get(pos);
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

    public int getReplaceIndex() {
        return mReplaceIndex;
    }
// My Setters.

    public void setReplaceIndex(int replaceIndex) {
        mReplaceIndex = replaceIndex;
    }

    public void setCalendar(Calendar calendar) {
        mCalendar = calendar;
    }
}
