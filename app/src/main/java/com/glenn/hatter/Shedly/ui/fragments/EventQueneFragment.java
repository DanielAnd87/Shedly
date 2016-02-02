package com.glenn.hatter.Shedly.ui.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.glenn.hatter.Shedly.R;
import com.glenn.hatter.Shedly.adapters.EventQueneAdapter;
import com.glenn.hatter.Shedly.constants.Constants;
import com.glenn.hatter.Shedly.interfaces.Communicator;

import java.util.ArrayList;

/**
 * Created by hatter on 2015-07-07.
 */
public class EventQueneFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "EventQueneFragment";
    private ArrayList<Integer> mEventIds;
    private boolean ordinary;
    private int mPos;
    private int mFreetimeDuration;
    private Communicator mCommunicator;
    private TextView mTypeLabel;

    public void setCommunicator(Communicator communicator) {
        mCommunicator = communicator;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        mEventIds = args.getIntegerArrayList("event quene");
        mPos = args.getInt("event position");
        ordinary = args.getBoolean(Constants.ORDINARY_LIST);
        if (ordinary) {
            mFreetimeDuration = args.getInt(Constants.FREETIME_DURATION);
        } else {
            mFreetimeDuration = -1;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.eventquene_fragment, container, false);
        rootView.setTag(TAG);
        mCommunicator = (Communicator) getActivity();

        mTypeLabel = (TextView) rootView.findViewById(R.id.type_label);
        if (!ordinary) {
            mTypeLabel.setText(R.string.quene_type_label_fixed);
        }
        ImageButton restoreButton = (ImageButton) rootView.findViewById(R.id.restore_button);
        restoreButton.setOnClickListener(this);


        RecyclerView eventRecyclerView = (RecyclerView) rootView.findViewById(R.id.evenRecyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());

        EventQueneAdapter adapter = new EventQueneAdapter(getActivity(), mEventIds, mPos, mFreetimeDuration);
        eventRecyclerView.setAdapter(adapter);
        eventRecyclerView.setHasFixedSize(false);
        eventRecyclerView.setLayoutManager(layoutManager);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        mCommunicator.restoreRemovedEvents(mPos, ordinary);
        mCommunicator.removeFraktion(Constants.QUEUE_FRACTION);
    }
}
