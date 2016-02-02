package com.glenn.hatter.Shedly.model;

import com.glenn.hatter.Shedly.data.Event;

import java.util.ArrayList;

/**
 * Created by hatter on 2016-02-02.
 */
public class EventHandler {
    private ArrayList<Event> mEvent = new ArrayList<>();
    private ArrayList<Event> mReplaceList = new ArrayList<>();
    private ArrayList<Event> mRemovedEvents = new ArrayList<>();


    // for now I replace mReplaceList where it is needed.
    private ArrayList<Event> mOrdinaryEventQueue = new ArrayList<>();
    private ArrayList<Event> mBinedFixedEvents = new ArrayList<>();

}
