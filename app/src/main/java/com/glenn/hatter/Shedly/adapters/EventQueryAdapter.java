package com.glenn.hatter.Shedly.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.glenn.hatter.Shedly.R;
import com.glenn.hatter.Shedly.data.Event;

import java.util.ArrayList;


/**
 * Created by hatter on 2015-11-02.
 */
public class EventQueryAdapter extends RecyclerView.Adapter<EventQueryAdapter.StartEventsHolder> {

    private final boolean[] mCheckBoxes;

    public boolean[] getBinedBooleans() {
        return mBinedBooleans;
    }

    private final boolean[] mBinedBooleans;
    private Context mContext;
    private ArrayList<Event> mEvents;
    private int mTime;

    public EventQueryAdapter(ArrayList<Event> events, boolean[] checkBoxes, int time, Context context) {
        mEvents = events;
        mCheckBoxes = checkBoxes;
        mTime = time;
        mBinedBooleans = new boolean[checkBoxes.length];
        mContext = context;
    }

    public ArrayList<Event> getChosenEvents() {
        return mEvents;
    }

    @Override
    public StartEventsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.progress_query_item, parent, false);
        StartEventsHolder viewHolder = new StartEventsHolder(view);
        return viewHolder;
    }

    public boolean[] getCheckBoxes() {
        return mCheckBoxes;
    }

    @Override
    public void onBindViewHolder(final StartEventsHolder holder, final int position) {
        final Event current = mEvents.get(position);
        String name = current.getName() + "";
        holder.queryItemLabel.setText(name);

        if (mCheckBoxes[position]) {
            int color = ContextCompat.getColor(mContext, R.color.colorGreen);
            holder.mLayout.setBackgroundColor(color);
        } else {
            int colorYellow = R.color.colorYellow;
            int color = ContextCompat.getColor(mContext, colorYellow);
            holder.mLayout.setBackgroundColor(color);
        }

        if (mBinedBooleans[position]) {
            int color = ContextCompat.getColor(mContext, R.color.colorRed);
            holder.mLayout.setBackgroundColor(color);
        }


        holder.binedEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Checking the checkbox if the event is deleted.
                if (!mBinedBooleans[position]) {
                    mBinedBooleans[position] = true;
                    // Setting normal color.
               //     holder.binedEvents.setColorFilter(Color.parseColor("#FFD6181F"));
                } else {
                    mBinedBooleans[position] = false;
                    // Setting red color
                 //   holder.binedEvents.setColorFilter(Color.parseColor("#727272"));

                }
                notifyItemChanged(position);
            }
        });
    }



    @Override
    public int getItemCount() {
        return mEvents.size();
    }

    private int setScheduleStatus(boolean[] checkedBoxes) {
        // Checking that the user is ahead of schedule if correct boxes is checked.
        int status = 0;
        for (int i = 0; i < checkedBoxes.length; i++) {
            if (checkedBoxes[i]) {
                int currentTime = mEvents.get(i).getTime();
                int nextTime = mEvents.get(i + 1).getTime();
                status += nextTime - currentTime;
            }
        }
        return  status - mTime;
    }

    private int getCombinedFreeTimeLeft(ArrayList<Event> scheckpoints) {
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

    public class StartEventsHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView queryItemLabel;
        private ImageButton binedEvents;
        private RelativeLayout mLayout;

        public StartEventsHolder(View itemView) {
            super(itemView);
            queryItemLabel = (TextView) itemView.findViewById(R.id.query_row_textview);
            binedEvents = (ImageButton) itemView.findViewById(R.id.query_item_binedbox);
            mLayout = (RelativeLayout) itemView.findViewById(R.id.query_layout);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            if (mCheckBoxes[getAdapterPosition()]) {
                int color = ContextCompat.getColor(mContext, R.color.colorGreen);
                mLayout.setBackgroundColor(color);
            } else {
                int colorYellow = R.color.colorYellow;
                int color = ContextCompat.getColor(mContext, colorYellow);
                mLayout.setBackgroundColor(color);
            }
            mCheckBoxes[getAdapterPosition()] = !mCheckBoxes[getAdapterPosition()];

            if (mBinedBooleans[getAdapterPosition()]) {
                int color = ContextCompat.getColor(mContext, R.color.colorRed);
                mLayout.setBackgroundColor(color);
                mCheckBoxes[getAdapterPosition()] = false;

            }

            notifyItemChanged(getAdapterPosition());
        }

    }
}
