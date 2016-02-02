package com.glenn.hatter.Shedly.ui.fragments;

import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.glenn.hatter.Shedly.adapters.ColorRowAdaper;
import com.glenn.hatter.Shedly.constants.Constants;
import com.glenn.hatter.Shedly.interfaces.Communicator;
import com.glenn.hatter.Shedly.R;
import com.glenn.hatter.Shedly.data.Event;
import com.glenn.hatter.Shedly.model.ConvertToTime;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by hatter on 2015-07-05.
 */
public class NewEventFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private Communicator mCommunicator;
    private RecyclerView mColorRecycler;
    private ColorRowAdaper mColorRowAdaper;
    private EditText mName;
    private int mEventPos;
    private boolean hasStarted = false;
    private ImageView curtainView;
    private int mEventId;
    private int mFixedTime;
    private boolean mBrandNew = true;
    private int mTravelToInt;
    private int mTravelFromInt;
    private RelativeLayout base;
    private NumberPicker mMinutePicker;
    private NumberPicker mHourPicker;
    private NumberPicker mMinutePickerTravelTo;
    private NumberPicker mMinutePickerTravelFrom;
    private TextView startTimeLabel;
    private TextView stopTimeLabel;
    private DateFormat timeFormat;
    private Calendar mCalendar;
    private TimePicker mTimePicker;
    private Switch mTimeSwith;
    private Switch mFrekvensySwitch;
    private CheckBox mTravelToCheckbox;
    private CheckBox mTravelFromCheckbox;
    private Spinner mFrekvencySpinner;
    private String[] mFrekvencyList = {
            Constants.DAYLY,
            Constants.WEEKLY,
            Constants.BI_WEEKLY,
            Constants.MONTLY,
            Constants.WEEKDAYS,
            Constants.WEEKENDS,
            // Must initialize this.
            Constants.FOURTH_WEEK
    };
    private ArrayAdapter<String> mFrekvensyAdapter;
    private String mEventFrekvensy;


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
        View view = inflater.inflate(R.layout.new_event_fragment, container, false);

        mMinutePicker = (NumberPicker) view.findViewById(R.id.numberPicker);
        mHourPicker = (NumberPicker) view.findViewById(R.id.hour_picker);
        mName = (EditText) view.findViewById(R.id.events_name_textfield);
        base = (RelativeLayout) view.findViewById(R.id.new_event_relativelayout);
        // Initiate color row
        mColorRecycler = (RecyclerView) view.findViewById(R.id.color_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false);
        mColorRowAdaper = new ColorRowAdaper(getActivity());
        mColorRecycler.setAdapter(mColorRowAdaper);
        mColorRecycler.setLayoutManager(layoutManager);


        mCalendar = Calendar.getInstance();
        mTravelFromCheckbox = (CheckBox) view.findViewById(R.id.travel_from_checkbox);
        mMinutePickerTravelFrom = (NumberPicker) view.findViewById(R.id.numberPicker_travel_dur_from);
        mMinutePickerTravelTo = (NumberPicker) view.findViewById(R.id.numberPicker_travel_dur_to);
        mTravelToCheckbox = (CheckBox) view.findViewById(R.id.travel_to_checkbox);
        curtainView = (ImageView) view.findViewById(R.id.curtain_view);
        mTravelToCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mMinutePickerTravelTo.setEnabled(isChecked);
            }
        });
        mTravelFromCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mMinutePickerTravelFrom.setEnabled(isChecked);
            }
        });

        mFrekvencySpinner = (Spinner) view.findViewById(R.id.frekventspinner);
        String[] frequensyTexts = {
                getActivity().getString(R.string.new_event_fragmet_frequensy_daily),
                getActivity().getString(R.string.new_event_fragmet_frequensy_weekly),
                getActivity().getString(R.string.new_event_fragmet_frequensy_bi_weekly),
                getActivity().getString(R.string.new_event_fragmet_frequensy_monthly),
                getActivity().getString(R.string.new_event_fragmet_frequensy_weekdays),
                getActivity().getString(R.string.new_event_fragmet_frequensy_weekends),
                getActivity().getString(R.string.new_event_fragmet_frequensy_fourht_week)
        };
        mFrekvensyAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, frequensyTexts);
        mFrekvencySpinner.setAdapter(mFrekvensyAdapter);

        startTimeLabel = (TextView) view.findViewById(R.id.start_time_label);
        stopTimeLabel = (TextView) view.findViewById(R.id.stop_time_label);
        // TODO: 2015-10-07 Instead of letting the user pick a start date, for when to start repeat, I instead send an timeStamp for this day and initatie the calendar with it.
        mHourPicker.setOnScrollListener(new NumberPicker.OnScrollListener() {
            @Override
            public void onScrollStateChange(NumberPicker view, int scrollState) {
                if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE || scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {
                    if (mTimeSwith.isChecked()) {
                        updateLabel();
                    }
                }
            }
        });
        final TimePickerDialog.OnTimeSetListener timeDialog = new TimePickerDialog.OnTimeSetListener() {
            public void onTimeSet(TimePicker view, int hourOfDay,
                                  int minute) {
                mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mCalendar.set(Calendar.MINUTE, minute);
                updateLabel();
            }
        };

        timeFormat = DateFormat.getTimeInstance();

        startTimeLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimeSwith.setChecked(true);
                /*
                new TimePickerDialog(getActivity(),
                        timeDialog,
                        mCalendar.get(Calendar.HOUR_OF_DAY),
                        mCalendar.get(Calendar.MINUTE),
                        true).show();
                        */
            }

        });






        mTimeSwith = (Switch) view.findViewById(R.id.time_switch);
        mTimeSwith.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && hasStarted) {
                    mColorRowAdaper.setColor(1);
                    new TimePickerDialog(getActivity(),
                            timeDialog,
                            mCalendar.get(Calendar.HOUR_OF_DAY),
                            mCalendar.get(Calendar.MINUTE),
                            true).show();
                }else if (!isChecked && hasStarted) {
                    mColorRowAdaper.setColor(0);
                }
            }
        });
        mFrekvensySwitch = (Switch) view.findViewById(R.id.frekvensy_switch);
        mFrekvensySwitch.setChecked(true);
        mFrekvencySpinner.setEnabled(true);
        mFrekvensySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mFrekvencySpinner.setEnabled(true);
                } else {
                    mFrekvencySpinner.setEnabled(false);
                }
            }
        });

        mFrekvencySpinner.setOnItemSelectedListener(this);


        return view;
    }

    private void updateLabel() {

        //startTimeLabel.setText(timeFormat.format(mCalendar.getTime()));
        int startMinute = mCalendar.get(Calendar.MINUTE);

        int startHour = mCalendar.get(Calendar.HOUR_OF_DAY);
        while (startMinute > 59) {
            startHour++;
            startMinute -= 60;
        }
        int stopHour = mHourPicker.getValue() + startHour;
        int stopMinute = mMinutePicker.getValue() + startMinute;
        if (mTravelToCheckbox.isChecked()) {
            stopMinute += mMinutePickerTravelTo.getValue();
        }
        if (mTravelFromCheckbox.isChecked()) {
            stopMinute += mMinutePickerTravelFrom.getValue();
        }
        if (stopMinute > 59) {
            stopHour++;
            stopMinute -= 60;
        }
        if (startMinute == 60) {
            startMinute = 0;
            startHour++;
        }
        if (startMinute < 0) {
            startMinute = 60 + startMinute;
            startHour--;
        }
        if (stopMinute < 0) {
            stopMinute = 60 + stopMinute;
            startHour--;
        }

        startTimeLabel.setText(startHour + ":" + startMinute);
        if (startMinute > 9) {
            startTimeLabel.setText(startHour + ":" + startMinute);
        } else {
            startTimeLabel.setText(startHour + ":" + "0" + startMinute);
        }
        if (stopMinute > 9) {
            stopTimeLabel.setText(stopHour + ":" + stopMinute);
        } else {
            stopTimeLabel.setText(stopHour + ":" + "0" + stopMinute);
        }

    }
private void updateLabel(int fixedTime) {

    //startTimeLabel.setText(timeFormat.format(mCalendar.getTime()));
    int startMinute;
    int startHour;
    if (mFixedTime > 0) {
        startMinute = ConvertToTime.getMinute(fixedTime);
        startHour = ConvertToTime.getHour(fixedTime);
    } else {
            startMinute = 0;
            startHour = 0;
        }
        while (startMinute > 59) {
            startHour++;
            startMinute -= 60;
        }
        int stopHour = mHourPicker.getValue() + startHour;
        int stopMinute = mMinutePicker.getValue() + startMinute;
        if (mTravelToCheckbox.isChecked()) {
            stopMinute += mMinutePickerTravelTo.getValue();
        }
        if (mTravelFromCheckbox.isChecked()) {
            stopMinute += mMinutePickerTravelFrom.getValue();
        }
        if (stopMinute > 59) {
            stopHour++;
            stopMinute -= 60;
        }
        if (startMinute == 60) {
            startMinute = 0;
            startHour++;
        }
        if (startMinute < 0) {
            startMinute = 60 + startMinute;
            startHour--;
        }
        if (stopMinute < 0) {
            stopMinute = 60 + stopMinute;
            startHour--;
        }

        startTimeLabel.setText(startHour + ":" + startMinute);
        if (startMinute > 9) {
            startTimeLabel.setText(startHour + ":" + startMinute);
        } else {
            startTimeLabel.setText(startHour + ":" + "0" + startMinute);
        }
        if (stopMinute > 9) {
            stopTimeLabel.setText(stopHour + ":" + stopMinute);
        } else {
            stopTimeLabel.setText(stopHour + ":" + "0" + stopMinute);
        }

    }



    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mCommunicator = (Communicator) getActivity();
        mMinutePicker.setMaxValue(59);
        mHourPicker.setMaxValue(23);
        mMinutePicker.setValue(10);

        mMinutePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if (oldVal == 59 && newVal == 0) {
                    mHourPicker.setValue(mHourPicker.getValue() + 1);
                }
                if (oldVal == 0 && newVal == 59) {
                    mHourPicker.setValue(mHourPicker.getValue() - 1);
                }
                if (mTimeSwith.isChecked()) {
                    updateLabel();
                }
            }
        });

        mMinutePickerTravelFrom.setMaxValue(59);
        mMinutePickerTravelTo.setMaxValue(59);
        mMinutePickerTravelFrom.setValue(15);
        mMinutePickerTravelTo.setValue(15);
        mMinutePickerTravelTo.setEnabled(false);
        mMinutePickerTravelFrom.setEnabled(false);


        // getting all info from the bundle.
        Bundle bundle = getArguments();
        int minutesDuration = bundle.getInt(Constants.NEW_MINUTE);
        int hourDuration = bundle.getInt(Constants.NEW_HOUR);
        mFixedTime = bundle.getInt(Constants.FIXEDTIME_VALUE);
        int timestamp = bundle.getInt(Constants.TIMESTAMP);
        mBrandNew = bundle.getBoolean(Constants.NEW_EVENT_BOOLEAN);
        if (mFixedTime > 0 && !mBrandNew) {
            mTimeSwith.setVisibility(View.INVISIBLE);
        } else if (mFixedTime == 0 && !mBrandNew) {
            curtainView.setVisibility(View.VISIBLE);
        }


        mMinutePicker.setValue(minutesDuration);
        mHourPicker.setValue(hourDuration);

        startTimeLabel.setText(ConvertToTime.convertToTime(mFixedTime));
        int stopTime = mFixedTime + minutesDuration + hourDuration;
        stopTimeLabel.setText(ConvertToTime.convertToTime(stopTime));
        if (timestamp > 0) {
            updateCalander(mFixedTime, timestamp);
        }

        mTravelToInt = bundle.getInt(Constants.TRAVEL_DURATION_TO);
        mTravelFromInt = bundle.getInt(Constants.TRAVEL_DURATION_FROM);
        if (mFixedTime > 0) {
            mColorRowAdaper.setColor(1);
            mTimeSwith.setChecked(true);
            updateLabel(mFixedTime);
            if (mTravelToInt > 0) {
                mTravelToCheckbox.setChecked(true);
                /*
                mMinutePicker.setValue(mMinutePicker.getValue() - mTravelToInt);
                if (    mMinutePicker.getValue() < mTravelToInt
                        && hourDuration > 0) {
                    mHourPicker.setValue(hourDuration-1);
                }
                */
                mFixedTime += mTravelToInt;
            }
            if (mTravelFromInt > 0) {
                mTravelFromCheckbox.setChecked(true);
                /*
                mMinutePicker.setValue(mMinutePicker.getValue() - mTravelFromInt);
                if (    mMinutePicker.getValue() < mTravelFromInt
                        && hourDuration > 0) {
                    mHourPicker.setValue(hourDuration-1);
                }
                */
            }
        }
        hasStarted = true;
        if (mTravelToInt > 0) {
            mMinutePickerTravelTo.setValue(mTravelToInt / Constants.MINUTE);
        }
        if (mTravelFromInt > 0) {
            mMinutePickerTravelFrom.setValue(mTravelFromInt / Constants.MINUTE);
        }

/*
        if (mMinutePickerTravelTo.getValue() > 0) {
            mTravelToCheckbox.setChecked(true);
            if (mTimeSwith.isChecked()) {
                updateLabel();
            }
        } else {
            mMinutePickerTravelTo.setEnabled(false);
        }
        if (mMinutePickerTravelFrom.getValue() > 0) {
            mTravelFromCheckbox.setChecked(true);
            if (mTimeSwith.isChecked()) {
                updateLabel();
            }
        } else {
            mMinutePickerTravelFrom.setEnabled(false);
        }
*/


            // setting the frequency spinner.
            if (bundle.getString(Constants.REPEATING) == null) {
                mFrekvencySpinner.setSelection(0);
            } else {
                if (bundle.getString(Constants.REPEATING).equals(Constants.ONCE)) {
                    mFrekvensySwitch.setChecked(false);
                } else if (bundle.getString(Constants.REPEATING).equals(Constants.WEEKLY)) {
                    mFrekvencySpinner.setSelection(1);
                } else if (bundle.getString(Constants.REPEATING).equals(Constants.BI_WEEKLY)) {
                    mFrekvencySpinner.setSelection(2);
                } else if (bundle.getString(Constants.REPEATING).equals(Constants.MONTLY)) {
                    mFrekvencySpinner.setSelection(3);
                } else if (bundle.getString(Constants.REPEATING).equals(Constants.WEEKDAYS)) {
                    mFrekvencySpinner.setSelection(4);
                } else if (bundle.getString(Constants.REPEATING).equals(Constants.WEEKENDS)) {
                    mFrekvencySpinner.setSelection(5);
                } else if (bundle.getString(Constants.REPEATING).equals(Constants.FOURTH_WEEK)) {
                    mFrekvencySpinner.setSelection(6);
                }
            }

            if (!mBrandNew) {
                mName.setText(bundle.getString(Constants.NEW_EVENT_NAME));
                mName.requestFocus();
            } else {
                mName.requestFocus();
            }
            mEventId = bundle.getInt(Constants.NEW_EVENT_ID);

            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (mBrandNew) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            } else {
                //imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                //imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            }

            ImageButton confirmAndCloseButton = (ImageButton) getActivity().findViewById(R.id.comfirm_event_button);
            confirmAndCloseButton.setOnClickListener(this);
            ImageButton cancelButton = (ImageButton) getActivity().findViewById(R.id.cancel_event);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCommunicator.removeFraktion("new Event");
                }
            });
        }

    private void updateCalander(int fixedTime, int timestamp) {
        int hour = ConvertToTime.getHour(fixedTime);
        int minute = ConvertToTime.getMinute(fixedTime);
        Date date = new Date((long) timestamp * 1000);
        mCalendar.setTime(date);
        mCalendar.set(Calendar.HOUR_OF_DAY, hour);
        mCalendar.set(Calendar.MINUTE, minute);


        DateFormat dateFormat = DateFormat.getDateInstance();
        dateFormat.format(date);
    }



    @Override
    public void onClick(View v) {
        // Sending event info to Main.
        String name = mName.getText().toString();
        // TODO: 2015-09-22 set duration for hour as well.
        int duration = mMinutePicker.getValue();
        int hourDuration = mHourPicker.getValue();
        Event event;
        int color = Color.parseColor(mColorRowAdaper.getColor());
        if (mTimeSwith.isChecked()) {

            Integer currentHour = mCalendar.get(Calendar.HOUR_OF_DAY);
            Integer currentMinute = mCalendar.get(Calendar.MINUTE);

            event = new Event(name, duration * Constants.MINUTE + hourDuration * Constants.HOUR, currentHour * Constants.HOUR + currentMinute * Constants.MINUTE);

        } else {
            event = new Event(name, duration * Constants.MINUTE + hourDuration * Constants.HOUR);
        }
        if (mFrekvensySwitch.isChecked() && !mEventFrekvensy.equals(Constants.DAYLY)) {
            event.setRecurring(mEventFrekvensy);
            event.setStartTimeStamp(mCalendar);

            // FIXME: 2015-09-23 This only works for events created today but since we cant set event for future days yet it is'nt a problem yet.
        } else if (!mFrekvensySwitch.isChecked()) {
            event.setRecurring(Constants.ONCE);
            event.setStartTimeStamp(mCalendar);
        }

        if (mFixedTime > 0) {
            event.setFixedTime(mFixedTime);
        }
        event.setColor(color);


        if (mTravelToCheckbox.isChecked()) {
            int travelDur = mMinutePickerTravelTo.getValue() * Constants.MINUTE;
            if (event.isFixedTime()) {
                event.setFixedTime(event.getFixedTime() - travelDur);
            }
            event.setDuration(event.getDuration() - mTravelToInt);
            event.setTravelDurationTo(travelDur);
            event.setDuration(event.getDuration() + travelDur);
        }
        if (mTravelFromCheckbox.isChecked()) {
            int travelDur = mMinutePickerTravelFrom.getValue() * Constants.MINUTE;
            event.setDuration(event.getDuration() - mTravelFromInt);
            event.setTravelDurationFrom(travelDur);
            event.setDuration(event.getDuration() + travelDur);
        }

        event.setId(mEventId);
        if (!name.equals("") || duration * Constants.MINUTE + hourDuration * Constants.HOUR != 0) {
            mCommunicator.respond(event, mBrandNew, mEventPos);
            mCommunicator.removeFraktion("new Event");

        } else {
            // todo: Need to pass a context in the constructor (if I can use a constructor that is.
            Toast.makeText(getActivity(), "Could you be a peach and please name your event.", Toast.LENGTH_LONG).show();

        }
    }



    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        /*
        TextView chosenText = (TextView) view;
        */
        mEventFrekvensy = mFrekvencyList[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
