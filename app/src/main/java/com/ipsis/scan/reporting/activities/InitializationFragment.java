package com.ipsis.scan.reporting.activities;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import com.ipsis.scan.R;
import com.ipsis.scan.reporting.data.CacheData;
import com.ipsis.scan.reporting.data.CacheManager;
import com.ipsis.scan.utils.DateUtils;

import java.util.Calendar;

/**
 * A placeholder fragment containing a simple view.
 */
public class InitializationFragment extends Fragment {

    private InitializationActivity mActivity;

    public InitializationFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = (InitializationActivity) getActivity();

        return inflater.inflate(R.layout.fragment_initialization, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button editButton = (Button) view.findViewById(R.id.editButton);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editHours();
            }
        });

        Button startButton = (Button) view.findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CacheData cacheData = CacheManager.getInstance().getData();
                cacheData.setDate(Calendar.getInstance());

                CacheManager.getInstance().saveCache(getContext());

                /*startActivity(new Intent(getActivity(), SummaryActivity.class));

                mActivity.finish();*/

                startActivity(new Intent(getActivity(), SummaryActivity.class));
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        CacheData cacheData = CacheManager.getInstance().getData();
        cacheData.setDate(Calendar.getInstance());

        CacheManager.getInstance().saveCache(getContext());

        mActivity.finish();
    }

    private void editHours() {
        final CacheData cacheData = CacheManager.getInstance().getData();

        TimePickerDialog startingHourPicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, final int selectedHour, final int selectedMinute) {
                askEndingHour(selectedHour, selectedMinute);
            }
        }, cacheData.getStartDate().get(Calendar.HOUR_OF_DAY), cacheData.getStartDate().get(Calendar.MINUTE), true);
        startingHourPicker.setTitle(R.string.settings_starting_hour);
        startingHourPicker.show();
    }

    private void askEndingHour(final int startingHour, final int startingMinute) {
        final CacheData cacheData = CacheManager.getInstance().getData();

        TimePickerDialog endingHourPicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour2, int selectedMinute2) {
                if (startingHour * 60 + startingMinute < selectedHour2 * 60 + selectedMinute2) {
                    cacheData.getStartDate().set(Calendar.HOUR_OF_DAY, startingHour);
                    cacheData.getStartDate().set(Calendar.MINUTE, startingMinute);

                    cacheData.getEndDate().set(Calendar.HOUR_OF_DAY, selectedHour2);
                    cacheData.getEndDate().set(Calendar.MINUTE, selectedMinute2);

                    TextView startingHourTextView = (TextView) getView().findViewById(R.id.startingHourTextView);
                    startingHourTextView.setText(DateUtils.formatDate(cacheData.getStartDate()));

                    TextView endingHourTextView = (TextView) getView().findViewById(R.id.endingHourTextView);
                    endingHourTextView.setText(DateUtils.formatDate(cacheData.getEndDate()));
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage(R.string.initialization_error_ending_hour)
                            .setPositiveButton(R.string.button_edit, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();

                                    askEndingHour(startingHour, startingMinute);
                                }
                            });
                    builder.create().show();
                }
            }
        }, cacheData.getEndDate().get(Calendar.HOUR_OF_DAY), cacheData.getEndDate().get(Calendar.MINUTE), true);
        endingHourPicker.setTitle(R.string.settings_ending_hour);
        endingHourPicker.show();
    }
}
