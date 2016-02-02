package com.glenn.hatter.Shedly.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.glenn.hatter.Shedly.R;
import com.glenn.hatter.Shedly.data.Event;


/**
 * Created by hatter on 2015-11-02.
 */
public class StartEventsAdapter extends RecyclerView.Adapter<StartEventsAdapter.StartEventsHolder> {

    private Event[] mEvents;

    public StartEventsAdapter(Event[] events) {
        mEvents = events;
    }

    public Event[] getChosenEvents() {
        return mEvents;
    }

    @Override
    public StartEventsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.start_event_item, parent, false);
        StartEventsHolder viewHolder = new StartEventsHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(StartEventsHolder holder, final int position) {
        final Event current = mEvents[position];
        holder.startEventLabel.setText(current.getName());
        holder.startEventCheckbox.setChecked(!current.isChosenToBeRemoved());

        holder.startEventCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                current.setChosenToBeRemoved(!isChecked);
            }
        });
    }



    @Override
    public int getItemCount() {
        return mEvents.length;
    }

    public class StartEventsHolder extends RecyclerView.ViewHolder{
        public TextView startEventLabel;
        public CheckBox startEventCheckbox;

        public StartEventsHolder(View itemView) {
            super(itemView);
            startEventLabel = (TextView) itemView.findViewById(R.id.start_event_label);
            startEventCheckbox = (CheckBox) itemView.findViewById(R.id.start_event_checkBox);;
        }
    }
}
