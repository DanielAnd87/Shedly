package com.glenn.hatter.Shedly.model;

import com.glenn.hatter.Shedly.data.Event;

import java.util.ArrayList;

/**
 * Created by hatter on 2016-02-24.
 */
public class QueryHandler {

    private ArrayList<Event> mEvent;

    public QueryHandler(ArrayList<Event> eventList) {
        mEvent = eventList;
    }

    public boolean[] getCheckedBoxes(int time) {
        // A method that sets an array of booleans to true if the corresponding event is before the current date.
        boolean[] checkBoxes = new boolean[mEvent.size()];
        for (int i = 0; i < checkBoxes.length; i++) {
            // If the events time is less than the current then the box should be false.
            Event current = mEvent.get(i);
            int currentEventTime = current.getTime() + current.getDuration();
            checkBoxes[i] = currentEventTime < time;
        }


        return checkBoxes;

    }

    public int setScheduleStatus(boolean[] checkedBoxes, int time) {
        // Checking that the user is ahead of schedule if correct boxes is checked.
        int status = 0;
        for (int i = 0; i < checkedBoxes.length; i++) {
                if (checkedBoxes[i]) {
                    int currentTime = mEvent.get(i).getTime();
                    int nextTime = mEvent.get(i + 1).getTime();
                    status += nextTime - currentTime;
                }
            }
        return  status - time;
    }

    public int getCombinedFreeTimeLeft(ArrayList<Event> scheckpoints) {
        // Combine all values from freetimes into one integer.
        int freeDuration = 0;
        for (int i = 0; i < scheckpoints.size(); i ++) {
            Event current = scheckpoints.get(i);
            if (current.isFreeTime()) {
                freeDuration += current.getDuration();
            }
        }
        return freeDuration;
    }
}
