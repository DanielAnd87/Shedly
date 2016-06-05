package com.glenn.hatter.Shedly.interfaces;

import android.os.Bundle;

import com.glenn.hatter.Shedly.data.Event;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by hatter on 2015-07-05.
 */
public interface Communicator {
    // Todo: It is better to incapsulate this interface and move it to the EventAdapter instead. See slidenerd 15 for reference.
    public void removeFraktion(String fractionTag);
    public void respond(Event event, boolean newEvent, int event_Id);
    public void sendQueneIndex(int eventId, int pos);

    public void startNewEventFragment(Bundle bundle);
    public void startEventQueneFragment(Bundle bundle);
    public void communicatingDate(Calendar calendar);
    public void changeMorningTime(boolean morning);

    public void deleteEvent(int eventId);

    public void restoreRemovedEvents(int pos, boolean ordinary);

    public void queryFinishedEvents(Bundle bundle);

    public void respondToQuery(ArrayList<Integer> checkBoxes, ArrayList<Integer> idsToBin);
}
