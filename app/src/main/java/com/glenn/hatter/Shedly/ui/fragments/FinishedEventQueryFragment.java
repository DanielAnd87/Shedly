package com.glenn.hatter.Shedly.ui.fragments;

import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.glenn.hatter.Shedly.R;
import com.glenn.hatter.Shedly.adapters.ColorRowAdaper;
import com.glenn.hatter.Shedly.adapters.EventQueneAdapter;
import com.glenn.hatter.Shedly.adapters.EventQueryAdapter;
import com.glenn.hatter.Shedly.constants.Constants;
import com.glenn.hatter.Shedly.data.Event;
import com.glenn.hatter.Shedly.interfaces.Communicator;
import com.glenn.hatter.Shedly.model.ConvertToTime;
import com.glenn.hatter.Shedly.model.QueryHandler;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

/**
 * Created by hatter on 2015-07-05.
 */
public class FinishedEventQueryFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private Communicator mCommunicator;
    private ArrayList<Event> mEvents;
    private QueryHandler mQueryHandler;
    private EventQueryAdapter adapter;
    private ImageButton mDoneButton;


    public void setCommunicator(Communicator communicator) {
        mCommunicator = communicator;
    }


    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.progress_query_fragment, container, false);

        RecyclerView eventRecyclerView = (RecyclerView) view.findViewById(R.id.query_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());

        Calendar mCalendar = Calendar.getInstance();
        Integer currentHour = mCalendar.get(Calendar.HOUR_OF_DAY);
        Integer currentMinute = mCalendar.get(Calendar.MINUTE);
        int currentTime = currentHour * Constants.HOUR + currentMinute * Constants.MINUTE;

        Bundle bundle = getArguments();
        //Parcelable[] parcelable = bundle.getParcelableArray(Constants.PARCELD_EVENT);
        //Event[] events = Arrays.copyOf(parcelable, parcelable.length, Event[].class);
        mEvents = bundle.getParcelableArrayList(Constants.PARCELD_EVENT);
        // Removing all doubles and unnecessary events.
        for (int i = 0; i < mEvents.size(); i++) {
            mEvents.remove(i);
        }
        for (int i = 0; i < mEvents.size(); i++) {
            Event current = mEvents.get(i);
            if (current.getId() == Constants.MORNING_ID || current.getId() == Constants.NIGHT_ID || current.isFreeTime()) {
                mEvents.remove(i);
                i--;
            }
        }
        //Collections.addAll(mEvents, events);
        //mQueryHandler = new QueryHandler(mEvents);

        adapter = new EventQueryAdapter(mEvents, new boolean[mEvents.size()], currentTime);
        eventRecyclerView.setAdapter(adapter);
        eventRecyclerView.setHasFixedSize(false);
        eventRecyclerView.setLayoutManager(layoutManager);




        return view;
    }



    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);




        mCommunicator = (Communicator) getActivity();
        // TODO: 2016-02-27  getting all info from the bundle.

        // TODO: 2016-02-27 Change this reference.
            mDoneButton = (ImageButton) getActivity().findViewById(R.id.query_done_btn);
            mDoneButton.setOnClickListener(this);

        }






    @Override
    public void onClick(View v) {
        // Creating id array.
        boolean[] checkBoxes = adapter.getCheckBoxes();
        boolean[] binBoxes = adapter.getBinedBooleans();

        ArrayList<Integer> idsToRemove = new ArrayList<>();
        ArrayList<Integer> idsToBin = new ArrayList<>();
        for (int i = 0; i < mEvents.size(); i++) {
            if (checkBoxes[i]) {
                idsToRemove.add(mEvents.get(i).getId());
            }
            if (binBoxes[i]) {
                idsToBin.add(mEvents.get(i).getId());
            }
        }
        // Sending event info to Main.
        mCommunicator.respondToQuery(idsToRemove, idsToBin);
        mCommunicator.removeFraktion(Constants.BACKSTACK_QUERY);

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
