package com.glenn.hatter.Shedly.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.glenn.hatter.Shedly.constants.Constants;
import com.glenn.hatter.Shedly.data.db.EventDataSource;
import com.glenn.hatter.Shedly.interfaces.Communicator;
import com.glenn.hatter.Shedly.model.EventHandler;
import com.glenn.hatter.Shedly.ui.MainActivity;
import com.glenn.hatter.Shedly.R;
import com.glenn.hatter.Shedly.data.Event;
import com.glenn.hatter.Shedly.model.ConvertToTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by hatter on 2015-04-29.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {


    private final EventHandler mEventHandler;

    private Context mContext;

    private Communicator mCommunicator;
    private int mNumberOfFreetimes;

    public void setCommunicator(Communicator communicator) {
        mCommunicator = communicator;
    }

    private int mNeededDur = 0;

    public EventAdapter(Context context, List<Event> list, EventDataSource datasource) {
        mContext = context;
        EventAdapter adapterContext = this;
        mNumberOfFreetimes = 0;
        mEventHandler = new EventHandler(context, datasource, Calendar.getInstance(), (ArrayList<Event>) list);
    }

    public void createDay(List<Event> list) {
        mEventHandler.createDay(list);
    }

    @Override
    public int getItemViewType(int position) {
        int viewType;

        if (position % 2 == 0) {
            viewType = 0;
        } else {
            viewType = 1;
        }
        return viewType;
    }


    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_list_time_item, parent, false);

        View view1 = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_list_item, parent, false);

        EventViewHolder viewHolder = new EventViewHolder(view);
        EventViewHolder viewHolder1 = new EventViewHolder(view1);

        switch (viewType) {
            case 0:
                return viewHolder;
            case 1:
                return viewHolder1;
        }

        return viewHolder;


    }


    @Override
    public void onBindViewHolder(EventViewHolder holder, int position) {
        holder.bindHour(mEventHandler.getEvents().get(position).getName(),
                mEventHandler.getEvents().get(position).getDuration(), position, mEventHandler.getEvents().get(position).getTime());

    }


    @Override
    public int getItemCount() {
        return mEventHandler.getEvents().size();
    }




    public void addEvent(Event event, boolean brandNew) {
        mEventHandler.addEvent(event, brandNew);
        notifyDataSetChanged();
    }


    public void checkingFreeTimeChanged() {
        int freeTimeNumber = 0;
        ArrayList<Event> events = mEventHandler.getEvents();
        for (int i = 0; i < events.size(); i++) {
            // Checking if it is a freeTime and add it to freetimeNumber.
            if (events.get(i).isFreeTime()) {
                freeTimeNumber++;
            }
        }

        // comparing the two freetime numbers and notifying change if fucked up.
        if (freeTimeNumber > mNumberOfFreetimes) {
            // Need to find the new one and add it.
        } else if (freeTimeNumber < mNumberOfFreetimes) {
            // Need to find the new one and remove it.
        }
    }

    public void notifyMoved(int from, int to) {
        notifyItemMoved(from, to);
        notifyItemChanged(from - 1);
        notifyItemChanged(to - 1);
    }
    public void notifyMoved(int from, int to, boolean movedOverFixedTime) {
        // FIXME: 2016-02-08 This wont work because there will be items that is inserted and I need to notify about it after I move event.
        /*
        notifyItemMoved(from, to);
        notifyItemChanged(from - 1);
        notifyItemChanged(to - 1);

        // Changing only the times.
        ArrayList<Event> events = mEventHandler.getEvents();

        for (int i = 2; i < events.size(); i+=2) {
            notifyItemChanged(i);
        }
        // Changing only the freeTimes.
        for (int i = 0; i < events.size(); i+=2) {
            if (events.get(i).isFreeTime()) {
                notifyItemChanged(i);
            }
        }
        */

        // TODO: 2016-02-08 This is a quickfix but try to do it properly in tha future!
        notifyDataSetChanged();
    }




    public Bundle getEventQuene(int layoutPosition, boolean ordinary) {

        return mEventHandler.getEventQuene(layoutPosition, ordinary);
    }

    public boolean move(int fromPos, int toPos) {
        if (mEventHandler.getEvent(fromPos).isFixedTime()) {
            notifyDataSetChanged();
            Toast.makeText(mContext, R.string.exuse_when_moving_fixedtime, Toast.LENGTH_SHORT).show();
            return false;
        }
        // If not a freeTime, as it ususally is'nt, then notify that two event has moved.
        if (!mEventHandler.getEvent(toPos).isFixedTime()) {
            notifyMoved(fromPos, toPos);
        } else {
            notifyMoved(fromPos, toPos, true);
        }
        return mEventHandler.move(fromPos, toPos);
    }


    public void replaceEvent(int pos, boolean recycle) {
        mEventHandler.replaceEvent(pos, recycle);
        // FIXME: 2016-02-08 Need to have a method that controls my freetimes and tells when they removes and appear.
        notifyItemRemoved(pos - 1);
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, 2);
        notifyFreeTimesChanged();


    }

    public void notifyFreeTimesChanged() {
        ArrayList<Event> events = mEventHandler.getEvents();
        for (int i = 3; i < events.size(); i+=2) {
            if (events.get(i).isFreeTime()) {
                notifyItemChanged(i);
            }
        }
        for (int i = 2; i < events.size(); i+=2) {
            notifyItemChanged(i);
        }
    }

    public void updateNight(int totalPixelMinutes) {
        mEventHandler.updateNight(totalPixelMinutes, getItemCount()-1, getItemCount());
        notifyDataSetChanged();
    }

    public void updateMorning(int totalPixelMinutes) {
        mEventHandler.updateMorning(totalPixelMinutes);
        notifyDataSetChanged();
    }



    public void setCalendar(Calendar calendar) {
        mEventHandler.setCalendar(calendar);
    }

    public void saveDate() {
        mEventHandler.saveDate();
    }


    public Event removeEvent(int eventId) {
        return mEventHandler.removeEvent(eventId);
    }


    public Event getEventFromId(int evenId) {
        return mEventHandler.getEventFromId(evenId);
    }

    public void removeFreetime(int evenId, int pos) {
        mEventHandler.removeFreetime(evenId, pos);
        notifyDataSetChanged();
    }


    public int getNightTime() {
        return mEventHandler.getEvents().get(getItemCount()-1).getFixedTime();
    }

    public boolean replacingFixedTime(Event event) {
        return mEventHandler.replacingFixedTime(event);
    }


    public void resetEvents() {
        mEventHandler.resetEvents();
    }




    public class EventViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        public TextView
                mEventLabel,
                mRefillBtn;
        public ImageView
                mTravelFromIcon,
                mTravelToIcon,
                mSunDrawable;



        public EventViewHolder(View itemView) {
            super(itemView);

            mEventLabel = (TextView) itemView.findViewById(R.id.event_text);
            mTravelFromIcon = (ImageView) itemView.findViewById(R.id.travel_image_to);
            mSunDrawable = (ImageView) itemView.findViewById(R.id.sun_draw);
            mTravelToIcon = (ImageView) itemView.findViewById(R.id.travel_image_from);
            mRefillBtn = (TextView) itemView.findViewById(R.id.fillup_button);




            itemView.setOnClickListener(this);
        }

        public void bindHour(String nameText, final int duration, final int position, int time) {
            if (getItemViewType() == 1) {
                mRefillBtn.setVisibility(View.GONE);
                if (duration > Constants.HOUR/2) {
                    itemView.setMinimumHeight(Constants.HOUR/2);
                    mEventLabel.setText(nameText + "  " + ConvertToTime.convertToTime(duration) + mContext.getString(R.string.long_duration_extra_label));

                }
                else {
                    itemView.setMinimumHeight(duration);
                    mEventLabel.setText(nameText);
                }
                Event currentEvent = mEventHandler.getEvents().get(getAdapterPosition());
                if (mEventHandler.getEvents().get(position).isFixedTime()) {
                    itemView.setBackgroundColor(currentEvent.getColor());
                } else {
                    // If the current event is a normal event it's normal color.
                    itemView.setBackgroundColor(currentEvent.getColor());
                }
                mEventLabel.setMinimumWidth(150);

                if (position == 1 || position == mEventHandler.getEvents().size() - 1) {
                    mEventLabel.setText("");
                    mSunDrawable.setVisibility(View.VISIBLE);
                } else {
                    mSunDrawable.setVisibility(View.GONE);
                }

                // Make the travel icons appear if there is any traveltime.
                if (mEventHandler.getEvents().get(position).getTravelDurationFrom() > 0) {
                    mTravelFromIcon.setVisibility(View.VISIBLE);
                } else {
                    mTravelFromIcon.setVisibility(View.GONE);
                }

                if (mEventHandler.getEvents().get(position).getTravelDurationTo() > 0) {
                    mTravelToIcon.setVisibility(View.VISIBLE);
                } else {
                    mTravelToIcon.setVisibility(View.GONE);
                }
            }

            if (getItemViewType() == 0) {
                mEventLabel.setText(ConvertToTime.convertToTime(time) + "");
                mEventLabel.setMinimumWidth(50);
                if (position == 0) {
                    mEventLabel.setText("");
                    //itemView.setBackgroundColor(mEvent.get(position()).getColor());
                }
            }


            if (mEventHandler.getEvents().get(position).isFreeTime() && getItemViewType() == 1) {
                mRefillBtn.setVisibility(View.VISIBLE);
                int size = mEventHandler.getReplaceList().size();
                mRefillBtn.setText(size + " ");
                mRefillBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mEventHandler.removeAndFillUp(position);
                        notifyDataSetChanged();
                    }
                });
            }



        }

        @Override
        public void onClick(View v) {
            Event chosen = mEventHandler.getEvents().get(getLayoutPosition());

                //choseEventsToReplace(getLayoutPosition());
                mCommunicator = (Communicator) mContext;

                if (getItemViewType() == 1) {
                    if (mEventHandler.getEvents().get(getLayoutPosition()).isFreeTime()) {
                        Bundle bundle = getEventQuene(getLayoutPosition(), true);
                        mCommunicator.startEventQueneFragment(bundle);

                    } else if (getLayoutPosition() == 1) {
                        mCommunicator.changeMorningTime(true);
                    } else if (getLayoutPosition() == mEventHandler.getEvents().size() - 1) {
                        mCommunicator.changeMorningTime(false);
                    } else {
                        // Starting NewEventFrag whit a bundle.
                        Bundle bundle = new Bundle();

                        bundle.putString(Constants.NEW_EVENT_NAME, chosen.getName());
                        bundle.putString(Constants.REPEATING, chosen.getRecurring());
                        bundle.putInt(Constants.FIXEDTIME_VALUE, chosen.getFixedTime());
                        bundle.putInt(Constants.NEW_MINUTE, ConvertToTime.getMinute(chosen.getDuration()));
                        bundle.putInt(Constants.NEW_HOUR, ConvertToTime.getHour(chosen.getDuration()));
                        bundle.putInt(Constants.TIMESTAMP, (int) chosen.getStartTimeStamp());
                        bundle.putBoolean(Constants.NEW_EVENT_BOOLEAN, false);
                        bundle.putInt(Constants.EVENT_POSITION, getLayoutPosition());
                        bundle.putInt(Constants.NEW_EVENT_ID, mEventHandler.getEvents().get(getLayoutPosition()).getId());
                        bundle.putInt(Constants.TRAVEL_DURATION_FROM, mEventHandler.getEvents().get(getLayoutPosition()).getTravelDurationFrom());
                        bundle.putInt(Constants.TRAVEL_DURATION_TO, mEventHandler.getEvents().get(getLayoutPosition()).getTravelDurationTo());


                        mNeededDur = chosen.getDuration();

                        mCommunicator.startNewEventFragment(bundle);

                        if (chosen.isFixedTime()) {
                            mEventHandler.setReplaceIndex(getLayoutPosition());
                        }
                    }
                }
        }

    }


}
