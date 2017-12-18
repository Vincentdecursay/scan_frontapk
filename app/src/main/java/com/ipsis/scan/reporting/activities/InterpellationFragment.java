package com.ipsis.scan.reporting.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.*;
import android.widget.*;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.ipsis.scan.R;
import com.ipsis.scan.database.RatpDataSource;
import com.ipsis.scan.database.model.Route;
import com.ipsis.scan.database.model.Stop;
import com.ipsis.scan.geolocation.LocationManager;
import com.ipsis.scan.reporting.activities.search.SearchLocationActivity;
import com.ipsis.scan.reporting.communication.service.SynchronisationClient;
import com.ipsis.scan.reporting.data.CacheManager;
import com.ipsis.scan.reporting.edition.MissionForms;
import com.ipsis.scan.reporting.edition.MissionValidation;
import com.ipsis.scan.reporting.edition.model.MissionInterpellation;
import com.ipsis.scan.reporting.edition.model.MissionLocation;
import com.ipsis.scan.reporting.edition.model.MissionReport;
import com.ipsis.scan.ui.TextViewTimePicker;
import com.ipsis.scan.utils.AppUtils;
import com.ipsis.scan.utils.Constants;
import com.ipsis.scan.utils.SerializationUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class InterpellationFragment extends Fragment {

    /**
     * Location request code (startActivityForResult)
     */
    private static final int LOCATION_REQUEST = 1000;

    private InterpellationActivity mActivity;

    private MissionForms mMissionForms;

    private MissionReport mMissionReport;

    private MissionInterpellation mMissionInterpellationReport;

    private TextViewTimePicker mTimePicker;
    private TextView mLocationPicker;
    private ImageView mClearLocationButton;
    private TextView mStartingLocationTextView;
    private TextView mEndingLocationTextView;

    private LocationManager mLocationManager;

    private FirebaseAnalytics mFirebaseAnalytics;

    private SynchronisationClient mSynchronisationClient;

    /**
     * Stops associated to the current route
     */
    private CharSequence[] mStops;

    public InterpellationFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = (InterpellationActivity) getActivity();

        setHasOptionsMenu(true);

        return inflater.inflate(R.layout.fragment_interpellation, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        mLocationManager = LocationManager.getInstance(getActivity());
        mLocationManager.onStart();

        mSynchronisationClient = new SynchronisationClient(getContext());

        mTimePicker = (TextViewTimePicker) view.findViewById(R.id.timeTextView);
        mLocationPicker = (TextView) view.findViewById(R.id.locationTextView);
        mClearLocationButton = (ImageView) view.findViewById(R.id.clearLocationButton);
        mStartingLocationTextView = (TextView) view.findViewById(R.id.startingLocationTextView);
        mEndingLocationTextView = (TextView) view.findViewById(R.id.endingLocationTextView);

        mMissionForms = CacheManager.getInstance().getData().getMissionForms();

        mMissionReport = CacheManager.getInstance().getData().getMissionReports().get(mActivity.getReportIndex());

        String locationMode = mMissionReport.getMissionType().getForm().getLocationMode();

        ArrayList<MissionInterpellation> interpellations = mMissionReport.getInterpellations();
        if (mActivity.getInterpellationIndex() == -1) {
            mMissionInterpellationReport = mMissionForms.createInterpellation(mMissionReport.getTempId());

            if (locationMode.equals(Constants.FORM_LIEU_FIXE)) {
                mMissionInterpellationReport.setLocation(mMissionReport.getLocation());
            } else if (locationMode.equals(Constants.FORM_SUR_LIGNE)) {
                mMissionInterpellationReport.setLocation(mMissionReport.getLocation());
                mMissionInterpellationReport.setRoute(mMissionReport.getRoute());
            }

            if (mLocationManager.getCurrentLocation() != null) {
                mMissionInterpellationReport.setLocationUser(mLocationManager.getCurrentLocation().getLatitude() + "," + mLocationManager.getCurrentLocation().getLongitude());
            } else {
                mMissionInterpellationReport.setLocationUser("");
            }

            interpellations.add(mMissionInterpellationReport);

            mActivity.setInterpellationIndex(interpellations.size() - 1);

            sendFirebaseEvent();
        } else {
            mMissionInterpellationReport = interpellations.get(mActivity.getInterpellationIndex());
        }
        mMissionInterpellationReport.buildView(getContext());

        // MissionInterpellationReport.print();

        RelativeLayout formLayout = (RelativeLayout) view.findViewById(R.id.formLayout);
        formLayout.removeAllViews();
        formLayout.addView(mMissionInterpellationReport.getView());

        mActivity.setTitle(mMissionInterpellationReport.getMissionType().getName());

        // Date
        mTimePicker.setTime(mMissionInterpellationReport.getDateTimeStart());
        if (!mMissionInterpellationReport.getDateTimeStart().isEmpty()) {
            mTimePicker.setTime(mMissionInterpellationReport.getDateTimeStart());
        }
        mTimePicker.setOnTimeChangeListener(new TextViewTimePicker.OnTimeChangeListener() {
            @Override
            public boolean onTimeChanged(TextViewTimePicker view, String oldTime, String newTime) {
                return MissionValidation.isValidInterpellationDate(getContext(), mMissionReport, newTime);
            }
        });

        // Location
        mLocationPicker.setText(mMissionInterpellationReport.getLocation().toString());
        mLocationPicker.setTag(mMissionInterpellationReport.getLocation());
        if (locationMode.equals(Constants.FORM_SUR_LIGNE)) {
            mLocationPicker.setHint(R.string.create_location_ligne);
        } else if (locationMode.equals(Constants.FORM_LIEU_FIXE)) {
            View lineCardView = view.findViewById(R.id.lineCardView);
            lineCardView.setVisibility(View.GONE);

            mLocationPicker.setHint(R.string.create_location_lieu);
        } else {
            View lineCardView = view.findViewById(R.id.lineCardView);
            lineCardView.setVisibility(View.GONE);

            mLocationPicker.setHint(R.string.create_location_lieu);
        }

        mClearLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLocationPicker.setText("");
                mLocationPicker.setTag(null);
                mMissionInterpellationReport.setRoute(null);
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

        // Station A
        mStartingLocationTextView = (TextView) view.findViewById(R.id.startingLocationTextView);
        mStartingLocationTextView.setText(mMissionInterpellationReport.getLocationStart());
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

        // Station B
        mEndingLocationTextView = (TextView) view.findViewById(R.id.endingLocationTextView);
        mEndingLocationTextView.setText(mMissionInterpellationReport.getLocationEnd());
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

        Button saveButton = (Button) view.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.finish();
            }
        });

        Button sendButton = (Button) view.findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.interpellation_send_report_title)
                        .setMessage(R.string.interpellation_send_report_text)
                        .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                saveInterpellation();

                                mSynchronisationClient.sendInterpellation(mMissionInterpellationReport);

                                // mMissionInterpellationReport.send(getActivity(), null);

                                mActivity.finish();
                            }
                        }).setNeutralButton(R.string.button_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.create().show();
            }
        });

        new UpdateStopsTask().start();
    }

    @Override
    public void onPause() {
        super.onPause();

        saveInterpellation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSynchronisationClient.disconnect();
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.interpellation_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        switch (item.getItemId()) {
            case R.id.action_remove_interpellation:
                builder.setMessage(R.string.interpellation_dialog_remove)
                        .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                CacheManager.getInstance().getData().getMissionReports().get(mActivity.getReportIndex()).getInterpellations().remove(mActivity.getInterpellationIndex());
                                CacheManager.getInstance().saveCache(getContext());

                                mActivity.finish();
                            }
                        }).setNeutralButton(R.string.button_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.create().show();

                return true;
            case R.id.action_copy_interpellation:
                builder.setMessage(R.string.interpellation_dialog_copy)
                        .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                saveInterpellation();

                                CacheManager.getInstance().getData().getMissionReports().get(mActivity.getReportIndex()).getInterpellations().add(SerializationUtils.deepClone(mMissionInterpellationReport));

                                mActivity.finish();
                            }
                        }).setNeutralButton(R.string.button_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.create().show();

                return true;
            case android.R.id.home:
                mActivity.finish();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void saveInterpellation() {
        if (mMissionInterpellationReport != null) {
            if (mLocationPicker.getTag() != null) {
                mMissionInterpellationReport.setLocation((MissionLocation) mLocationPicker.getTag());
            } else {
                mMissionInterpellationReport.setLocation(new MissionLocation(mLocationPicker.getText().toString()));
            }
            mMissionInterpellationReport.setLocationStart(mStartingLocationTextView.getText().toString());
            mMissionInterpellationReport.setLocationEnd(mEndingLocationTextView.getText().toString());
            if (!mTimePicker.isEmpty()) {
                mMissionInterpellationReport.setDateTimeStart(mTimePicker.getText().toString());
            }

            CacheManager.getInstance().saveCache(getContext());
        }
    }

    public void startSearchLocationActivity() {
        Intent intent = new Intent(getContext(), SearchLocationActivity.class);

        String searchType = mMissionReport.getMissionType().getForm().getLocationMode();
        if (searchType.equals(Constants.FORM_SUR_LIGNE)) {
            intent.putExtra(SearchLocationActivity.EXTRA_SEARCH_TYPE, Constants.TYPE_SEARCH_ROUTE);
        } else if (searchType.equals(Constants.FORM_LIEU_FIXE)) {
            intent.putExtra(SearchLocationActivity.EXTRA_SEARCH_TYPE, Constants.TYPE_SEARCH_STOP);
        } else {
            intent.putExtra(SearchLocationActivity.EXTRA_SEARCH_TYPE, Constants.TYPE_SEARCH_LOCATION);
        }
        intent.putExtra(SearchLocationActivity.EXTRA_SEARCH_LOCATION, (MissionLocation) mLocationPicker.getTag());

        startActivityForResult(intent, LOCATION_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOCATION_REQUEST && resultCode == Activity.RESULT_OK) {
            MissionLocation location = (MissionLocation) data.getSerializableExtra(SearchLocationActivity.EXTRA_SEARCH_LOCATION);
            if (location != null) {
                mLocationPicker.setText(location.toString());
                mLocationPicker.setTag(location);

                mStartingLocationTextView.setText("");
                mEndingLocationTextView.setText("");

                int type = data.getIntExtra(SearchLocationActivity.EXTRA_SEARCH_TYPE, Constants.TYPE_SEARCH_LOCATION);
                if (type == Constants.TYPE_SEARCH_ROUTE) {
                    Route route = (Route) data.getSerializableExtra(SearchLocationActivity.EXTRA_SEARCH_ROUTE);

                    mMissionInterpellationReport.setRoute(route);

                    new UpdateStopsTask().start();
                }
            }
        }
    }

    private void sendFirebaseEvent() {
        if (mLocationManager.getCurrentLocation() != null) {
            Bundle interpellationEventParams = new Bundle();
            interpellationEventParams.putLong("interpellation_time", mLocationManager.getCurrentLocation().getTime());
            interpellationEventParams.putDouble("interpellation_lon", mLocationManager.getCurrentLocation().getLongitude());
            interpellationEventParams.putDouble("interpellation_lat", mLocationManager.getCurrentLocation().getLatitude());
            mFirebaseAnalytics.logEvent("interpellation_new", interpellationEventParams);
        }
    }

    public class UpdateStopsTask extends Thread {
        @Override
        public void run() {
            if (mMissionInterpellationReport.getRoute() != null) {
                RatpDataSource mRatpDataSource = new RatpDataSource(getContext());
                List<Stop> stops = mRatpDataSource.getStopsByRoute(mMissionInterpellationReport.getRoute());

                mStops = new CharSequence[stops.size()];
                for (int i = 0; i < stops.size(); i++) {
                    mStops[i] = stops.get(i).getName();
                }
            }
        }
    }
}
