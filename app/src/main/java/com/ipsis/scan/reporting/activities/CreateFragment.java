package com.ipsis.scan.reporting.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.ipsis.scan.R;
import com.ipsis.scan.database.RatpDataSource;
import com.ipsis.scan.database.model.Route;
import com.ipsis.scan.database.model.Stop;
import com.ipsis.scan.reporting.activities.search.SearchLocationActivity;
import com.ipsis.scan.reporting.data.CacheManager;
import com.ipsis.scan.reporting.edition.MissionValidation;
import com.ipsis.scan.reporting.edition.model.MissionLocation;
import com.ipsis.scan.reporting.edition.model.MissionReport;
import com.ipsis.scan.reporting.edition.model.MissionType;
import com.ipsis.scan.ui.TextViewTimePicker;
import com.ipsis.scan.utils.AppUtils;
import com.ipsis.scan.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class CreateFragment extends Fragment {

    /**
     * Location request code (startActivityForResult)
     */
    private static final int LOCATION_REQUEST_CODE = 1000;

    /**
     * Activity
     */
    private CreateActivity mActivity;

    /**
     * Mission forms for the current type
     */
    ArrayList<MissionType> mMissionTypes;

    /**
     * Selected mission type
     */
    private int mMissionTypeIndex;

    /**
     * Views
     */
    private TextViewTimePicker mTimePicker;
    private TextView mLocationPicker;
    private ImageView mClearLocationButton;
    private TextView mStartingLocationTextView;
    private TextView mEndingLocationTextView;
    private TextView mMissionTypeTextView;

    /**
     * Selected route if type
     */
    private Route mRoute;

    /**
     * Stops associated to the current route
     */
    private CharSequence[] mStops;

    public CreateFragment() {
        super();

        mMissionTypeIndex = -1;

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = (CreateActivity) getActivity();
        Log.i("activitest", "fragment " + mActivity.getLocalClassName()  );


        return inflater.inflate(R.layout.fragment_create, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTimePicker = (TextViewTimePicker) view.findViewById(R.id.timeTextView);
        mLocationPicker = (TextView) view.findViewById(R.id.locationTextView);
        mClearLocationButton = (ImageView) view.findViewById(R.id.clearLocationButton);
        mStartingLocationTextView = (TextView) view.findViewById(R.id.startingLocationTextView);
        mEndingLocationTextView = (TextView) view.findViewById(R.id.endingLocationTextView);
        mMissionTypeTextView = (TextView) view.findViewById(R.id.missionTypeTextView);

        switch (mActivity.getType()) {
            case Constants.TYPE_SUR_LIGNE:
                mMissionTypes = CacheManager.getInstance().getData().getMissionForms().getSurLigneForms();
                mLocationPicker.setHint(R.string.create_location_ligne);
                break;
            case Constants.TYPE_LIEU_FIXE:
                mMissionTypes = CacheManager.getInstance().getData().getMissionForms().getLieuFixeForms();
                mLocationPicker.setHint(R.string.create_location_lieu);
                break;
            default:
                mMissionTypes = CacheManager.getInstance().getData().getMissionForms().getHorsReseauForms();
                mLocationPicker.setHint(R.string.create_location_lieu);
                break;
        }

        // Date start
        mTimePicker.setOnTimeChangeListener(new TextViewTimePicker.OnTimeChangeListener() {
            @Override
            public boolean onTimeChanged(TextViewTimePicker view, final String oldTime, final String newTime) {
                boolean valid = MissionValidation.isValidDateStart(getContext(), null, newTime);

                if (valid) {
                    if (MissionValidation.hasToExtendDateStart(newTime)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage(R.string.edit_extend_date_start)
                                .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        MissionValidation.extendDateStart(getContext(), newTime);
                                    }
                                }).setNeutralButton(R.string.button_no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (!newTime.equals(oldTime)) {
                                            mTimePicker.setTime(oldTime);
                                        }
                                    }
                                }).setCancelable(false);
                        builder.create().show();
                    } else if (MissionValidation.hasToExtendDateEnd(newTime)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage(R.string.edit_extend_date_end)
                                .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        MissionValidation.extendDateEnd(getContext(), newTime);
                                    }
                                }).setNeutralButton(R.string.button_no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (!newTime.equals(oldTime)) {
                                            mTimePicker.setTime(oldTime);
                                        }
                                    }
                                }).setCancelable(false);
                        builder.create().show();
                    }
                }

                return valid;
            }
        });

        // Si mission sur ligne, afficher choix départ et arrivée
        if (mActivity.getType() != Constants.TYPE_SUR_LIGNE) {
            CardView lineCardView = (CardView) view.findViewById(R.id.lineCardView);
            lineCardView.setVisibility(View.GONE);
        } else {
            mStartingLocationTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!mLocationPicker.getText().toString().isEmpty() && mStops != null) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle(R.string.edit_starting_location);
                        builder.setItems(mStops, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                mStartingLocationTextView.setText(mStops[item]);
                            }
                        });
                        builder.create().show();
                    } else {
                        AppUtils.showErrorDialog(getContext(), R.string.create_error_no_line);
                    }
                }
            });

            mEndingLocationTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!mLocationPicker.getText().toString().isEmpty() && mStops != null) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle(R.string.edit_ending_location);
                        builder.setItems(mStops, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                mEndingLocationTextView.setText(mStops[item]);
                            }
                        });
                        builder.create().show();
                    } else {
                        AppUtils.showErrorDialog(getContext(), R.string.create_error_no_line);
                    }
                }
            });
        }

        // Si mission hors réseau, bloquer la sélection du lieu
        if (mActivity.getType() != Constants.TYPE_HORS_RESEAU) {
            mClearLocationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mLocationPicker.setText("");
                    mLocationPicker.setTag(null);
                    mRoute = null;
                    mStops = null;

                    mStartingLocationTextView.setText("");
                    mEndingLocationTextView.setText("");

                    startSearchLocationActivity();
                }
            });

            mLocationPicker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startSearchLocationActivity();
                }
            });
        } else {
            mClearLocationButton.setVisibility(View.GONE);
            mLocationPicker.setText(R.string.create_hors_reseau);
        }

        mMissionTypeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final CharSequence[] items = new CharSequence[mMissionTypes.size()];
                for (int i = 0 ; i < mMissionTypes.size() ; i++) {
                    items[i] = mMissionTypes.get(i).getName();
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.edit_mission_type);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        mMissionTypeIndex = item;

                        mMissionTypeTextView.setText(items[item]);
                    }
                });
                builder.create().show();
            }
        });

        Button nextButton = (Button) view.findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createReport();
            }
        });

        initReport();

        if (CacheManager.getInstance().getData().isSynced() || CacheManager.getInstance().getData().isSyncing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setCancelable(false);
            builder.setMessage(R.string.create_reports_sent)
                    .setPositiveButton(R.string.button_back, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();

                            mActivity.finish();
                        }
                    });
            builder.create().show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


    }

    private void initReport() {
        MissionReport lastReport = mActivity.getLastReport();

        mTimePicker.setTime(mActivity.getDateTimeStart());

        if (lastReport != null) {
            String locationMode = lastReport.getMissionType().getForm().getLocationMode();
            if (locationMode.equals(Constants.FORM_SUR_LIGNE)) {
                switch (mActivity.getType()) {
                    case Constants.TYPE_SUR_LIGNE:
                        mLocationPicker.setText(lastReport.getLocation().toString());
                        mLocationPicker.setTag(lastReport.getLocation());
                        mRoute = lastReport.getRoute();
                        if (mRoute != null) {
                            new UpdateStopsTask().start();
                        }
                        mStartingLocationTextView.setText(lastReport.getLocationEnd());
                        break;
                    case Constants.TYPE_LIEU_FIXE:
                        mLocationPicker.setText(lastReport.getLocationEnd());
                        break;
                }
            } else if (locationMode.equals(Constants.FORM_LIEU_FIXE)) {
                switch (mActivity.getType()) {
                    case Constants.TYPE_LIEU_FIXE:
                        mLocationPicker.setText(lastReport.getLocation().toString());
                        mLocationPicker.setTag(lastReport.getLocation());
                        break;
                }
            }
        }
    }

    private void createReport() {
        if (mMissionTypeIndex != -1) {
            if (!MissionValidation.isValidDateStart(getContext(), null, mTimePicker.getText().toString())) {
                return;
            }
            MissionValidation.extendDateStart(getContext(), mTimePicker.getText().toString());
            MissionValidation.extendDateEnd(getContext(), mTimePicker.getText().toString());

            MissionType missionType = mMissionTypes.get(mMissionTypeIndex);
            MissionReport missionReport = CacheManager.getInstance().getData().getMissionForms().createReport(missionType);

            missionReport.setDateTimeStart(mTimePicker.getText().toString());
            if (!mActivity.getDateTimeEnd().isEmpty()) {
                missionReport.setDateTimeEnd(mActivity.getDateTimeEnd());
            }
            if (mLocationPicker.getTag() != null) {
                missionReport.setLocation((MissionLocation) mLocationPicker.getTag());
            } else {
                missionReport.setLocation(new MissionLocation(mLocationPicker.getText().toString()));
            }
            missionReport.setRoute(mRoute);
            missionReport.setLocationStart(mStartingLocationTextView.getText().toString());
            missionReport.setLocationEnd(mEndingLocationTextView.getText().toString());
            missionReport.setLocationUser("");

            // add the new report TODO move to cachemanager or cachedata
            ArrayList<MissionReport> reports = CacheManager.getInstance().getData().getMissionReports();
            reports.add(missionReport);

            // set datetimeend to the closest report if not set
            if (reports.size() >= 2) {
                int duration = 2500;
                MissionReport report = null;

                for (int i = 0 ; i < reports.size() - 1 ; i++) {
                    MissionReport currentReport = reports.get(i);

                    if (currentReport.getCompareDateStart() < missionReport.getCompareDateStart()) {
                        int currentDuration = missionReport.getCompareDateStart() - currentReport.getCompareDateStart();
                        if (currentDuration < duration) {
                            duration = currentDuration;
                            report = currentReport;
                        }
                    }
                }

                if (duration > 0 && report != null && report.getDateTimeEnd() != null && report.getDateTimeEnd().equals("-1:-1")) { // TODO get date end null
                    report.setDateTimeEnd(missionReport.getDateTimeStart());
                }
            }

            CacheManager.getInstance().saveCache(getContext(), new CacheManager.CacheSavedCallback() {
                @Override
                public void onCacheSaved() {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(getContext(), EditActivity.class);
                            intent.putExtra(EditActivity.EXTRA_REPORT_INDEX, CacheManager.getInstance().getData().getMissionReports().size() - 1);
                            startActivity(intent);

                            mActivity.finish();
                        }
                    });
                }
            });

            sendFirebaseEvent();
        } else {
            AppUtils.showErrorDialog(getContext(), R.string.create_error_no_type);
        }
    }

    public void startSearchLocationActivity() {
        Intent intent = new Intent(getContext(), SearchLocationActivity.class);

        switch (mActivity.getType()) {
            case Constants.TYPE_SUR_LIGNE:
                intent.putExtra(SearchLocationActivity.EXTRA_SEARCH_TYPE, Constants.TYPE_SEARCH_ROUTE);
                break;
            case Constants.TYPE_LIEU_FIXE:
                intent.putExtra(SearchLocationActivity.EXTRA_SEARCH_TYPE, Constants.TYPE_SEARCH_STOP);
                break;
            default:
                intent.putExtra(SearchLocationActivity.EXTRA_SEARCH_TYPE, Constants.TYPE_SEARCH_LOCATION);
                break;
        }

        intent.putExtra(SearchLocationActivity.EXTRA_SEARCH_LOCATION, (MissionLocation) mLocationPicker.getTag());

        startActivityForResult(intent, LOCATION_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOCATION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            MissionLocation location = (MissionLocation) data.getSerializableExtra(SearchLocationActivity.EXTRA_SEARCH_LOCATION);
            if (location != null && !location.equals(mLocationPicker.getTag())) {
                mLocationPicker.setText(location.toString());
                mLocationPicker.setTag(location);

                mStartingLocationTextView.setText("");
                mEndingLocationTextView.setText("");

                int type = data.getIntExtra(SearchLocationActivity.EXTRA_SEARCH_TYPE, Constants.TYPE_SEARCH_LOCATION);
                if (type == Constants.TYPE_SEARCH_ROUTE) {
                    mRoute = (Route) data.getSerializableExtra(SearchLocationActivity.EXTRA_SEARCH_ROUTE);

                    new UpdateStopsTask().start();
                }
            }
        }
    }

    private void sendFirebaseEvent() {

    }

    public class UpdateStopsTask extends Thread {
        @Override
        public void run() {
            RatpDataSource mRatpDataSource = new RatpDataSource(getContext());
            List<Stop> stops = mRatpDataSource.getStopsByRoute(mRoute);

            mStops = new CharSequence[stops.size()];
            for (int i = 0 ; i < stops.size() ; i++) {
                mStops[i] = stops.get(i).getName();
            }
        }
    }
}
