package com.glenn.hatter.Shedly.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.glenn.hatter.Shedly.data.Event;
import com.glenn.hatter.Shedly.R;
import com.glenn.hatter.Shedly.data.db.EventDataSource;
import com.glenn.hatter.Shedly.interfaces.Communicator;
import com.glenn.hatter.Shedly.model.ConvertToTime;


import java.util.ArrayList;

/**
 * Created by hatter on 2015-04-26.
 */
public class EventQueneAdapter extends RecyclerView.Adapter<EventQueneAdapter.EventViewHolder>  {

    private Context mContext;
    private ArrayList<Event> mEvents;
    private ArrayList<Event> mSortedEvents = new ArrayList<>();
    private EventDataSource mDatasource;
    // TODO: 2015-09-18 I want to send the id's for the correct events and only display dose.
    private ArrayList<Integer> mEventIds;
    private int
            mPos,
            mFreetimeDuration;
    private Communicator mCommunicator;


    public void setCommunicator(Communicator communicator) {
        mCommunicator = communicator;
    }


    public EventQueneAdapter(Context context, ArrayList<Integer> eventIds, int mEvntPos, int freetimeDuration) {
        mContext = context;
        mEventIds = eventIds;
        mPos = mEvntPos;
        mDatasource = new EventDataSource(context);
        // TODO: 2015-09-18 When I switch over to only use the database then I implement the sorted list.
        mEvents = mDatasource.read();
        sortList();
        mFreetimeDuration = freetimeDuration;
        mCommunicator = (Communicator) context;

    }

    private void sortList() {
        for (int i = 0; i < mEventIds.size(); i++) {
            for (int y = 0; y < mEvents.size(); y++) {
                if (mEventIds.get(i) == mEvents.get(y).getId()) {

                    mSortedEvents.add(mEvents.get(y));
                    // Here I remove the event from the first deck after I copied it from the other.
                    mEvents.remove(y);
                    // Backing down one step so that the loop can run it again.
                    y--;
                }
            }
        }
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_quene_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, final int position) {
        holder.bindEvent(mSortedEvents.get(position));

        holder.mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int eventId = mSortedEvents.get(position).getId();
                mDatasource.delete(eventId);
                mSortedEvents.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, mSortedEvents.size());
                mCommunicator.deleteEvent(eventId);
            }
        });
        holder.mItemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int eventDuration = mSortedEvents.get(position).getDuration();
                final int durationLeft = eventDuration - mFreetimeDuration;
                if (mFreetimeDuration == -1) {
                    sendEvent(position);
                } else {
                    if (durationLeft < 0) {
                        sendEvent(position);
                    } else {
                        // TODO: 2015-11-17 Foramt the text correctly such that it can be translated.
                        Toast.makeText(mContext, "You need " + ConvertToTime.getMinute(durationLeft) + " more minutes.", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });


    }

    private void sendEvent(int position) {
        int eventId = mSortedEvents.get(position).getId();
        mCommunicator.sendQueneIndex(eventId, mPos);
        mCommunicator.removeFraktion("event quene");
    }


    @Override
    public int getItemCount() {
        return mSortedEvents.size();
    }


    public class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        private TextView mEventText;
        private ImageButton mDeleteButton;
        private LinearLayout mItemLayout;

        public EventViewHolder(View itemView) {
            super(itemView);

            mEventText = (TextView) itemView.findViewById(R.id.event_label);
            mDeleteButton = (ImageButton) itemView.findViewById(R.id.delete_button);
            mItemLayout = (LinearLayout) itemView.findViewById(R.id.queue_list_item_layout);


        }

        public void bindEvent(Event event) {
            mEventText.setText(event.getName());
            mItemLayout.setBackgroundColor(event.getColor());

            if (event.getFixedTime() > 0) {
                mEventText.append("   " + ConvertToTime.convertToTime(event.getFixedTime()) + "");
            } else {
                // The color should only indicate size if it is'nt a fixedTime list.
                if (event.getDuration() > mFreetimeDuration) {
                    mItemLayout.setBackgroundColor(Color.parseColor("#FF5722"));
                }
            }
        }

        @Override
        public void onClick(View v) {

        }
    }
}
