package com.glenn.hatter.Shedly.ui.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import com.glenn.hatter.Shedly.interfaces.Communicator;



import java.util.Calendar;


/**
 * Created by hatter on 2015-07-20.
 */
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener{
    private Communicator mCommunicator;
    private Calendar mCalendar;

    public void setCommunicator(Communicator communicator) {
        mCommunicator = communicator;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        // Todo: Find out if this calendar really updates with the selected times.

        mCommunicator = (Communicator) getActivity();


        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }


    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        mCalendar = Calendar.getInstance();
        mCalendar.set(year,monthOfYear,dayOfMonth);
        mCommunicator.communicatingDate(mCalendar);
    }

}
