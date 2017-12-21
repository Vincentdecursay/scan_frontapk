package com.ipsis.scan.reporting.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.ipsis.scan.R;
import com.ipsis.scan.database.RatpDataSource;
import com.ipsis.scan.database.model.Route;
import com.ipsis.scan.database.model.Stop;
import com.ipsis.scan.reporting.activities.search.SearchLocationActivity;
import com.ipsis.scan.reporting.data.CacheManager;
import com.ipsis.scan.reporting.edition.MissionValidation;
import com.ipsis.scan.reporting.edition.model.MissionInterpellation;
import com.ipsis.scan.reporting.edition.model.MissionLocation;
import com.ipsis.scan.reporting.edition.model.MissionReport;
import com.ipsis.scan.reporting.edition.model.MissionValue;
import com.ipsis.scan.ui.TextViewTimePicker;
import com.ipsis.scan.utils.AppUtils;
import com.ipsis.scan.utils.Constants;
import com.ipsis.scan.utils.SerializableSparseArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class EditFragment extends Fragment {

    /**
     * Location request code (startActivityForResult)
     */
    private static final int LOCATION_REQUEST = 1000;

    /**
     * Activity
     */
    private EditActivity mActivity;


    /**
     * Current mission report
     */
    private MissionReport mMissionReport;

    private boolean mSynced;

    /**
     * Views
     */
    private TextViewTimePicker mStartingHourTextView;
    private TextView mLocationPicker;
    private ImageView mClearLocationButton;
    private TextView mStartingLocationTextView;
    private ImageView mStartingLocationButton;
    private TextView mEndingLocationTextView;
    private ImageView mEndingLocationButton;
    private TextViewTimePicker mEndingHourTextView;
    private ListView mInterpellationsListView;
    private InterpellationsArrayAdapter mInterpellationsAdapter;

    /**
     * Interpellation adapter list
     */
    private ArrayList<InterpellationAdapter> mInterpellations;

    /**
     * Stops associated to the current route
     */
    private CharSequence[] mStops;

    public EditFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = (EditActivity) getActivity();
        Log.i("activitest", "fragment " + mActivity.getLocalClassName()  );


        return inflater.inflate(R.layout.fragment_edit2, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mActivity.getReportIndex() != -1) {
            mMissionReport = CacheManager.getInstance().getData().getMissionReports().get(mActivity.getReportIndex());
        } else {
            mActivity.finish();

            return;
        }

        mMissionReport.buildView(getContext());

        // mMissionReport.print();

        RelativeLayout formLayout = (RelativeLayout) view.findViewById(R.id.formLayout);
        formLayout.removeAllViews();
        formLayout.addView(mMissionReport.getView());

        if (mMissionReport.getView().getChildCount() == 0) {
            TextView formTitleTextView = (TextView) view.findViewById(R.id.formTitleTextView);
            formTitleTextView.setVisibility(View.GONE);
        }

        mActivity.setTitle(mMissionReport.getMissionType().getName());

        mSynced = mMissionReport.isSync() || mMissionReport.isSyncing();

        // Date start
        mStartingHourTextView = (TextViewTimePicker) view.findViewById(R.id.startingHourTextView);
        if (!mMissionReport.getDateTimeStart().isEmpty()) {
            mStartingHourTextView.setTime(mMissionReport.getDateTimeStart());
        }
        mStartingHourTextView.setOnTimeChangeListener(new TextViewTimePicker.OnTimeChangeListener() {
            @Override
            public boolean onTimeChanged(TextViewTimePicker view, final String oldTime, final String newTime) {
                boolean valid = MissionValidation.isValidDateStart(getContext(), mMissionReport, newTime) && MissionValidation.isValidDateStartEnd(getContext(), mMissionReport, newTime, mEndingHourTextView.getText().toString());

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
                                mStartingHourTextView.setTime(oldTime);
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
                                mStartingHourTextView.setTime(oldTime);
                            }
                        }).setCancelable(false);
                        builder.create().show();
                    }
                }

                return valid;
            }
        });
        mStartingHourTextView.setEnabled(!mSynced);

        // Date end
        mEndingHourTextView = (TextViewTimePicker) view.findViewById(R.id.endingHourTextView);
        mEndingHourTextView.setTime(mMissionReport.getDateTimeEnd());
        mEndingHourTextView.setOnTimeChangeListener(new TextViewTimePicker.OnTimeChangeListener() {
            @Override
            public boolean onTimeChanged(TextViewTimePicker view, final String oldTime, final String newTime) {
                boolean valid = MissionValidation.isValidDateStartEnd(getContext(), mMissionReport, mStartingHourTextView.getText().toString(), newTime);

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
                                mEndingHourTextView.setTime(oldTime);
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
                                mEndingHourTextView.setTime(oldTime);
                            }
                        }).setCancelable(false);
                        builder.create().show();
                    }
                }

                return valid;
            }
        });
        mEndingHourTextView.setEnabled(!mSynced);

        // Location
        mLocationPicker = (TextView) view.findViewById(R.id.locationTextView);
        mLocationPicker.setText(mMissionReport.getLocation().toString());
        mLocationPicker.setTag(mMissionReport.getLocation());
        if (mMissionReport.getMissionType().getForm().getLocationMode().equals(Constants.FORM_SUR_LIGNE)) {
            mLocationPicker.setHint(R.string.create_location_ligne);
        } else {
            mLocationPicker.setHint(R.string.create_location_lieu);
        }
        if (!mMissionReport.getMissionType().getForm().getLocationMode().equals(Constants.FORM_SUR_LIGNE)) {
            View lineCardView = view.findViewById(R.id.lineCardView);
            lineCardView.setVisibility(View.GONE);
        }

        // Station A
        mStartingLocationTextView = (TextView) view.findViewById(R.id.startingLocationTextView);
        mStartingLocationTextView.setText(mMissionReport.getLocationStart());
        if (!mSynced) {
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
        }
        mStartingLocationButton = (ImageView) view.findViewById(R.id.startingLocationButton);
        mStartingLocationButton.setVisibility(mSynced ? View.GONE : View.VISIBLE);

        // Station B
        mEndingLocationTextView = (TextView) view.findViewById(R.id.endingLocationTextView);
        mEndingLocationTextView.setText(mMissionReport.getLocationEnd());
        if (!mSynced) {
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
        mEndingLocationButton = (ImageView) view.findViewById(R.id.endingLocationButton);
        mEndingLocationButton.setVisibility(mSynced ? View.GONE : View.VISIBLE);

        // Si mission hors réseau, bloquer la sélection du lieu
        mClearLocationButton = (ImageView) view.findViewById(R.id.clearLocationButton);
        if (!mMissionReport.getMissionType().getForm().getLocationMode().equals(Constants.FORM_HORS_RESEAU)) {
            mClearLocationButton.setVisibility(mSynced ? View.GONE : View.VISIBLE);
            mClearLocationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mLocationPicker.setText("");
                    mLocationPicker.setTag(null);
                    mMissionReport.setRoute(null);
                    mStops = null;

                    mStartingLocationTextView.setText("");
                    mEndingLocationTextView.setText("");

                    startSearchLocationActivity();
                }
            });

            if (!mSynced) {
                mLocationPicker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startSearchLocationActivity();
                    }
                });
            }
        } else {
            mClearLocationButton.setVisibility(View.GONE);
            mLocationPicker.setText(R.string.create_hors_reseau);
        }

        mInterpellationsListView = (ListView) view.findViewById(R.id.listView);
        mInterpellationsListView.setScrollContainer(false);
        mInterpellationsListView.setFocusable(false);
        mInterpellationsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long id) {
                InterpellationAdapter interpellationAdapter = mInterpellations.get(index);

                if (!interpellationAdapter.getInterpellation().isSync() && !interpellationAdapter.getInterpellation().isSyncing()) {
                    Intent intent = new Intent(getContext(), InterpellationActivity.class);
                    intent.putExtra(InterpellationActivity.EXTRA_REPORT_INDEX, mActivity.getReportIndex());
                    intent.putExtra(InterpellationActivity.EXTRA_INTERPELLATION_INDEX, interpellationAdapter.getIndex());
                    startActivity(intent);
                } else {
                    AppUtils.showErrorDialog(getContext(), R.string.edit_dialog_deleted_interpellation);
                }
            }
        });
        mInterpellationsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int index, long id) {
                final InterpellationAdapter interpellationAdapter = mInterpellations.get(index);

                if (!interpellationAdapter.getInterpellation().isSync() && !interpellationAdapter.getInterpellation().isSyncing()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage(R.string.interpellation_dialog_remove)
                            .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    CacheManager.getInstance().getData().getMissionReports().get(mActivity.getReportIndex()).getInterpellations().remove(interpellationAdapter.getIndex());
                                    CacheManager.getInstance().saveCache(getContext());

                                    updateInterpellation();
                                }
                            }).setNeutralButton(R.string.button_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    builder.create().show();
                } else {
                    AppUtils.showErrorDialog(getContext(), R.string.edit_dialog_deleted_interpellation);
                }

                return true;
            }
        });

        mInterpellations = new ArrayList<>();
        mInterpellationsAdapter = new InterpellationsArrayAdapter(getContext(), mInterpellations);
        mInterpellationsListView.setAdapter(mInterpellationsAdapter);

        View addInterpellationLayout = view.findViewById(R.id.addInterpellationLayout);
        addInterpellationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mMissionReport.isSync() && !mMissionReport.isSyncing()) {
                    Intent intent = new Intent(getContext(), InterpellationActivity.class);
                    intent.putExtra(InterpellationActivity.EXTRA_REPORT_INDEX, mActivity.getReportIndex());
                    intent.putExtra(InterpellationActivity.EXTRA_INTERPELLATION_INDEX, -1);
                    startActivity(intent);
                } else {
                    AppUtils.showErrorDialog(getContext(), R.string.edit_report_sent_interpellation);
                }
            }
        });

        new UpdateStopsTask().start();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mMissionReport != null) {
            // MissionValidation.extendDateStart(getContext(), mStartingHourTextView.getText().toString());
            // MissionValidation.extendDateEnd(getContext(), mEndingHourTextView.getText().toString());

            //mMissionReport.setLocation(mLocationPicker.getText().toString());
            if (mLocationPicker.getTag() != null) {
                mMissionReport.setLocation((MissionLocation) mLocationPicker.getTag());
            } else {
                mMissionReport.setLocation(new MissionLocation(mLocationPicker.getText().toString()));
            }
            mMissionReport.setLocationStart(mStartingLocationTextView.getText().toString());
            mMissionReport.setLocationEnd(mEndingLocationTextView.getText().toString());
            if (!mStartingHourTextView.isEmpty()) {
                mMissionReport.setDateTimeStart(mStartingHourTextView.getText().toString());
            }
            if (!mEndingHourTextView.isEmpty()) {
                mMissionReport.setDateTimeEnd(mEndingHourTextView.getText().toString());
            }

            CacheManager.getInstance().saveCache(getContext());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        updateInterpellation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    public void updateInterpellation() {
        View noInterpellationCardView = getView().findViewById(R.id.noInterpellationCardView);
        CardView interpellationsCardView = (CardView) getView().findViewById(R.id.interpellationsCardView);
        TextView interpellationTextView = (TextView) getView().findViewById(R.id.interpellationTextView);
        ArrayList<MissionInterpellation> interpellations = mMissionReport.getInterpellations();

        if (mMissionReport.getMissionType().getForm().getInterpellationEnable().equals(Constants.INTERPELLATION_ENABLED)) {
            // Update interpellations array
            mInterpellations.clear();
            for (int i = 0; i < interpellations.size(); i++) {
                mInterpellations.add(new InterpellationAdapter(i, interpellations.get(i)));
            }

            // Sort interpellations
            Collections.sort(mInterpellations, new Comparator<InterpellationAdapter>() {
                @Override
                public int compare(InterpellationAdapter i1, InterpellationAdapter i2) {
                    return i1.getCompareHour() < i2.getCompareHour() ? -1 : 0;
                }
            });

            // Show interpellations
            mInterpellationsAdapter.notifyDataSetChanged();
            AppUtils.justifyListViewHeightBasedOnChildren(mInterpellationsListView);

            // Empty state
            if (mInterpellations.size() == 0) {
                //noInterpellationCardView.setVisibility(View.VISIBLE);
                interpellationsCardView.setVisibility(View.GONE);
            } else {
                //noInterpellationCardView.setVisibility(View.GONE);
                interpellationsCardView.setVisibility(View.VISIBLE);
            }
        } else {
            noInterpellationCardView.setVisibility(View.GONE);
            interpellationsCardView.setVisibility(View.GONE);
            interpellationTextView.setVisibility(View.GONE);
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

                    mMissionReport.setRoute(route);

                    new UpdateStopsTask().start();
                }
            }
        }
    }

    private class InterpellationAdapter {
        private int mIndex;
        private String mName;
        private String mHour;
        private MissionReport mInterpellation;

        public InterpellationAdapter(int index, MissionReport interpellation) {
            super();

            mIndex = index;
            mInterpellation = interpellation;

            mHour = "0:00";

            String surname = "";
            String firstname = "";
            SerializableSparseArray<MissionValue> values = interpellation.getMissionValues();

            for (int i = 0 ; i < values.size() ; i++) {
                int keyField = values.keyAt(i);
                MissionValue value = values.get(keyField);

                if (value.getLabel().indexOf("hour") == 0) {
                    mHour = value.getValue();
                } else if (value.getLabel().indexOf("surname") == 0) {
                    surname = value.getValue();
                } else if (value.getLabel().indexOf("firstname") == 0) {
                    firstname = value.getValue();
                }
            }

            mName = surname.toUpperCase() + " " + firstname;
        }

        public int getIndex() {
            return mIndex;
        }

        public String getName() {
            return mName;
        }

        public String getHour() {
            return mHour;
        }

        public int getCompareHour() {
            String[] tokens = getHour().split(":");
            if (tokens.length == 2) {
                return Integer.parseInt(tokens[0]) * 60 + Integer.parseInt(tokens[1]);
            } else {
                return -1;
            }
        }

        public MissionReport getInterpellation() {
            return mInterpellation;
        }
    }

    private class InterpellationsArrayAdapter extends ArrayAdapter<InterpellationAdapter> {
        List<InterpellationAdapter> mInterpellations;

        public InterpellationsArrayAdapter(Context context, List<InterpellationAdapter> objects) {
            super(context, android.R.layout.simple_list_item_1, objects);

            mInterpellations = objects;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            InterpellationAdapter interpellationAdapter = mInterpellations.get(position);
            final MissionReport interpellation = interpellationAdapter.getInterpellation();

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_interpellation, parent, false);
            }

            TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            nameTextView.setText(interpellationAdapter.getName());

            TextView hourTextView = (TextView) convertView.findViewById(R.id.hourTextView);
            if (!interpellation.getDateTimeStart().isEmpty()) {
                hourTextView.setText(interpellation.getDateTimeStart());
            } else {
                hourTextView.setText("-");
            }

            final ImageView statusImageView = (ImageView) convertView.findViewById(R.id.statusImageView);
            interpellation.setSyncObserver(new MissionReport.SyncObserver() {
                @Override
                public void onSyncUpdate(MissionReport report) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (interpellation.isSync()) {
                                statusImageView.clearAnimation();
                                statusImageView.setImageResource(R.drawable.ic_sync_yes_48dp);
                            } else if (interpellation.isSyncing()) {
                                statusImageView.setImageResource(R.drawable.ic_sync_inprogress_48dp);
                                statusImageView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.rotate));
                            } else {
                                statusImageView.clearAnimation();
                                statusImageView.setImageResource(R.drawable.ic_sync_no_48dp);
                            }
                        }
                    });
                }
            });

            if (interpellation.isSync()) {
                statusImageView.clearAnimation();
                statusImageView.setImageResource(R.drawable.ic_sync_yes_48dp);
            } else if (interpellation.isSyncing()) {
                statusImageView.setImageResource(R.drawable.ic_sync_inprogress_48dp);
                statusImageView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.rotate));
            } else {
                statusImageView.clearAnimation();
                statusImageView.setImageResource(R.drawable.ic_sync_no_48dp);
            }

            if (position == mInterpellations.size() - 1) {
                convertView.findViewById(R.id.view2).setVisibility(View.INVISIBLE);
            } else {
                convertView.findViewById(R.id.view2).setVisibility(View.VISIBLE);
            }

            return convertView;
        }
    }

    public class UpdateStopsTask extends Thread {
        @Override
        public void run() {
            if (mMissionReport.getRoute() != null) {
                RatpDataSource mRatpDataSource = new RatpDataSource(getContext());
                List<Stop> stops = mRatpDataSource.getStopsByRoute(mMissionReport.getRoute());

                mStops = new CharSequence[stops.size()];
                for (int i = 0; i < stops.size(); i++) {
                    mStops[i] = stops.get(i).getName();
                }
            }
        }
    }
}
