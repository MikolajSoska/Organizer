package ms.organizer.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

public class OrganizerDatePickerDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private int day;
    private int month;
    private int year;

    public interface DatePickerDialogListener {
        void onDateSet(int day, int month, int year);
    }

    DatePickerDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (DatePickerDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must  implement DatePickerDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int pickedYear, int pickedMonth, int pickedDay) {
        listener.onDateSet(pickedDay, pickedMonth, pickedYear);
    }

    @Override
    public void setArguments(Bundle args) {
        day = args.getInt("day");
        month = args.getInt("month");
        year = args.getInt("year");
    }
}
