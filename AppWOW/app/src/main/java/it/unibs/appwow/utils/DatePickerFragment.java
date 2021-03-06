package it.unibs.appwow.utils;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;

import it.unibs.appwow.R;

/**
 * Created by Alessandro on 18/07/2016.
 */
public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener  {

    private EditText mEditText;

    public DatePickerFragment(){
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH); //MONTH VA DA 0 A 11
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
        EditText editText = (EditText) getActivity().findViewById(R.id.add_payment_date);
        editText.setText(DateUtils.formatSimpleDate(year, month, day));
        editText.setClickable(true);
    }
}
