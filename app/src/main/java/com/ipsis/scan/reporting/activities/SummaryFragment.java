package com.ipsis.scan.reporting.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.ipsis.scan.R;
import com.ipsis.scan.reporting.communication.service.SynchronisationClient;
import com.ipsis.scan.reporting.data.CacheData;
import com.ipsis.scan.reporting.data.CacheManager;
import com.ipsis.scan.reporting.edition.model.MissionInterpellation;
import com.ipsis.scan.reporting.edition.model.MissionReport;
import com.ipsis.scan.utils.AppUtils;
import com.ipsis.scan.utils.DateUtils;

import static com.ipsis.scan.utils.AppUtils.*;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * A placeholder fragment containing a simple view.
 */
public class SummaryFragment extends Fragment implements MissionReport.SyncCallback, MissionReport.SyncObserver {

    public static final String SHOW_ALL_DAY_PREFERENCE = "showAllDay";

    private SummaryActivity mActivity;

    private ArrayList<MissionReportAdapter> mReports;
    private MissionReportsArrayAdapter mReportsAdapter;

    private ListView mReportsListView;
    private Calendar mHourStart;
    private Calendar mHourEnd;

    private boolean mShowAllDay;
    private SharedPreferences mPreferences;

    private ImageView mStatusImageView;
    private TextView mStatusTextView;
    private Button mSendButton;
    private SynchronisationClient mSynchronisationClient;

    public SummaryFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = (SummaryActivity) getActivity();

        mPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        // mShowAllDay = mPreferences.getBoolean(SHOW_ALL_DAY_PREFERENCE, true); TODO
        mShowAllDay = true;

        setHasOptionsMenu(true);

        return inflater.inflate(R.layout.fragment_summary, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSynchronisationClient = new SynchronisationClient(getContext());

        mReportsListView = (ListView) view.findViewById(R.id.listView);
        mReportsListView.setScrollContainer(false);
        mReportsListView.setFocusable(false);
        mReportsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long id) {
                MissionReportAdapter missionReportAdapter = mReports.get(index);
                int reportIndex = missionReportAdapter.getIndex();
                final MissionReport report = missionReportAdapter.getMissionReport();
                boolean synced = CacheManager.getInstance().getData().isSynced() || CacheManager.getInstance().getData().isSyncing();

                if (reportIndex != -1) {
                    Intent intent = new Intent(getContext(), EditActivity.class);
                    intent.putExtra(EditActivity.EXTRA_REPORT_INDEX, reportIndex);
                    startActivity(intent);
                } else if (!synced) {
                    CharSequence[] items = {getString(R.string.create_sur_ligne), getString(R.string.create_lieu_fixe), getString(R.string.create_hors_reseau)};

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(getString(R.string.summary_create_report_title, report.getDateTimeStart(), report.getDateTimeEnd()));
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            Intent intent = new Intent(getContext(), CreateActivity.class);
                            intent.putExtra(CreateActivity.EXTRA_FORM_TYPE, item);
                            intent.putExtra(CreateActivity.EXTRA_FORM_DATE_TIME_START, report.getDateTimeStart());
                            intent.putExtra(CreateActivity.EXTRA_FORM_DATE_TIME_END, report.getDateTimeEnd());
                            intent.putExtra(CreateActivity.EXTRA_FORM_LAST_REPORT, mActivity.getLastMissionReport());
                            mActivity.startActivity(intent);
                        }
                    });
                    builder.create().show();
                } else {
                    AppUtils.showErrorDialog(getContext(), R.string.create_reports_sent);
                }
            }
        });
        mReportsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int index, long id) {
                MissionReportAdapter missionReportAdapter = mReports.get(index);
                final int reportIndex = missionReportAdapter.getIndex();
                final MissionReport missionReport = missionReportAdapter.getMissionReport();

                if (!missionReport.isSync() && !missionReport.isSyncing()) {
                    if (reportIndex != -1) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage(R.string.edit_dialog_remove_report)
                                .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        CacheManager.getInstance().getData().getMissionReports().remove(reportIndex);
                                        CacheManager.getInstance().saveCache(getContext());

                                        updateReports();
                                    }
                                }).setNeutralButton(R.string.button_no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        builder.create().show();
                    }
                } else {
                    AppUtils.showErrorDialog(getContext(), R.string.edit_report_sent_remove);
                }

                return true;
            }
        });

        mReports = new ArrayList<>();
        mReportsAdapter = new MissionReportsArrayAdapter(getContext(), mReports);
        mReportsAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();

                if (mReports.size() > 0) {
                    MissionReport lastReport = mReports.get(mReports.size() - 1).getMissionReport();
                    for (int i = mReports.size() - 1; i >= 0; i--) {
                        MissionReport report = mReports.get(i).getMissionReport();
                        if (report.getAutomatic() == 0) {
                            lastReport = report;
                            break;
                        }
                    }

                    String dateTimeEnd = lastReport.getDateTimeEnd();
                    if (!dateTimeEnd.equals("-1:-1")) {
                        mActivity.setLastDateTimeEnd(dateTimeEnd);
                    } else {
                        mActivity.setLastDateTimeEnd("");
                    }

                    mActivity.setLastMissionReport(lastReport);
                } else {
                    mActivity.setLastDateTimeEnd("");
                    mActivity.setLastMissionReport(null);
                }
            }
        });
        mReportsListView.setAdapter(mReportsAdapter);

        mStatusImageView = (ImageView) getView().findViewById(R.id.statusImageView);
        mStatusTextView = (TextView) getView().findViewById(R.id.reportStateTextView);

        mSendButton = (Button) view.findViewById(R.id.button2);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendReports();
            }
        });

        mSynchronisationClient.getData(new SynchronisationClient.DataCallback() {
            @Override
            public void onGetData(CacheData data) {
                // mSynchronisationClient.updateLocalData(getContext());

                mSynchronisationClient.scheduleSynchronisation();

                updateButton();
            }
        });
    }

    @Override
    public void onSyncSucceed() {
        ArrayList<MissionReport> reports = CacheManager.getInstance().getData().getMissionReports();

        if (reports.size() > 0) {
            for (MissionReport report : reports) {
                if (!report.isSync()) {
                    return;
                }
            }

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mStatusImageView.clearAnimation();
                    mStatusImageView.setImageResource(R.drawable.ic_sync_yes_48dp);

                    mStatusTextView.setText(R.string.summary_timeline_state_sent);

                    mSendButton.setEnabled(false);
                }
            });
        }
    }

    @Override
    public void onSyncFailure(Exception e) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStatusImageView.clearAnimation();
                mStatusImageView.setImageResource(R.drawable.ic_sync_no_48dp);

                mStatusTextView.setText(R.string.summary_timeline_state_unsent);

                mSendButton.setEnabled(true);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.summary_menu, menu);

        if (mShowAllDay) {
            MenuItem filterMenuItem = menu.findItem(R.id.filter);
            filterMenuItem.setIcon(R.drawable.ic_visibility_off_white_24dp);
        }

        /*MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if(null!=searchManager ) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }
        searchView.setIconifiedByDefault(false);*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            /*case R.id.search:
                SearchActivity.startActivity(this);

                return true;*/
            case R.id.filter:
                if (mShowAllDay) {
                    setShowAllDay(false);
                    item.setIcon(R.drawable.ic_remove_red_eye_white_24dp);
                } else {
                    setShowAllDay(true);
                    item.setIcon(R.drawable.ic_visibility_off_white_24dp);
                }

                updateReports();

                updateTimeline();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        updateReports();

        updateTimeline();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSynchronisationClient.disconnect();
    }

    public void sendReports() {
        if (!haveGeneratedReport()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.summary_send_reports_title)
                    .setMessage(R.string.summary_send_reports_text)
                    .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mSendButton.setEnabled(false);

                            mStatusImageView.setImageResource(R.drawable.ic_sync_inprogress_48dp);
                            mStatusImageView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.rotate));

                            mStatusTextView.setText(R.string.summary_timeline_state_sending);

                            for (MissionReport report : CacheManager.getInstance().getData().getMissionReports()) {
                                report.setSyncObserver(SummaryFragment.this);
                            }

                            mSynchronisationClient.sendReports(CacheManager.getInstance().getData().getMissionReports());
                        }
                    }).setNeutralButton(R.string.button_no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                    });
            builder.create().show();
        } else {
            AppUtils.showErrorDialog(getContext(), R.string.summary_send_reports_error);
        }
    }

    public void updateReports() {
        ArrayList<MissionReport> reports = CacheManager.getInstance().getData().getMissionReports();
        ArrayList<MissionReportAdapter> generatedReports = new ArrayList<>();

        // Update reports array
        for (int i = 0 ; i < reports.size() ; i++) {
            generatedReports.add(new MissionReportAdapter(i, reports.get(i)));
        }

        // Sort reports
        Collections.sort(generatedReports, new Comparator<MissionReportAdapter>() {
            @Override
            public int compare(MissionReportAdapter m1, MissionReportAdapter m2) {
                return m1.getMissionReport().getCompareDateStart() < m2.getMissionReport().getCompareDateStart() ? -1 : 0;
            }
        });

        // Add generated reports
        if (mShowAllDay) {
            ArrayList<MissionReportAdapter> allDayReports = getAllDayReports(generatedReports);
            generatedReports.clear();
            generatedReports.addAll(getAllDayReports(allDayReports));
        }

        // Empty state
        View noReportCardView = getView().findViewById(R.id.noReportCardView);
        View reportsList = getView().findViewById(R.id.card_view2);
        View sendButton = getView().findViewById(R.id.sendButtonLayout);
        if (generatedReports.size() == 0) {
            noReportCardView.setVisibility(View.VISIBLE);
            reportsList.setVisibility(View.GONE);
            sendButton.setVisibility(View.GONE);
        } else {
            noReportCardView.setVisibility(View.GONE);
            reportsList.setVisibility(View.VISIBLE);
            sendButton.setVisibility(View.VISIBLE);
        }

        mReports.clear();
        mReports.addAll(generatedReports);

        // Show reports
        mReportsAdapter.notifyDataSetChanged();
        AppUtils.justifyListViewHeightBasedOnChildren(mReportsListView);
    }

    private void updateTimeline() {
        mHourStart = CacheManager.getInstance().getData().getStartDate();
        mHourEnd = CacheManager.getInstance().getData().getEndDate();

        TextView startingHourTextView = (TextView) getView().findViewById(R.id.startingHourTextView);
        startingHourTextView.setText(DateUtils.formatDate(mHourStart));

        TextView endingHourTextView = (TextView) getView().findViewById(R.id.endingHourTextView);
        endingHourTextView.setText(DateUtils.formatDate(mHourEnd));

        RelativeLayout timelineLayout = (RelativeLayout) getView().findViewById(R.id.timelineLayout);
        timelineLayout.removeAllViews();

        for (MissionReportAdapter report : mReports) {
            int dateStart = report.getMissionReport().getTimelineDateStart();
            int dateEnd = report.getMissionReport().getTimelineDateEnd();

            if (dateEnd != -1) {
                if (report.getMissionReport().getMissionType() != null) {
                    addLineToTimeline(timelineLayout, dateStart, dateEnd, report.getMissionReport().getAutomatic() == 1, report.getMissionReport().getMissionType().getForm().getLocationMode());
                } else {
                    addLineToTimeline(timelineLayout, dateStart, dateEnd, report.getMissionReport().getAutomatic() == 1, null);

                }
                addCircleToTimeline(timelineLayout, dateEnd);
            }
            
            addCircleToTimeline(timelineLayout, dateStart);
        }

        updateButton();
    }

    @UiThread
    public void updateButton() {
        // Update sync icon and send button
        final ArrayList<MissionReport> cacheData = CacheManager.getInstance().getData().getMissionReports();
        final boolean synced = CacheManager.getInstance().getData().isSynced();
        final boolean syncing = CacheManager.getInstance().getData().isSyncing();

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (cacheData.size() > 0 && getContext() != null) {
                    if (synced) {
                        mStatusImageView.clearAnimation();
                        mSendButton.setEnabled(false);
                        mStatusImageView.setImageResource(R.drawable.ic_sync_yes_48dp);

                        mStatusTextView.setText(R.string.summary_timeline_state_sent);
                    } else {
                        if (syncing) {
                            mSendButton.setEnabled(false);
                            mStatusImageView.setImageResource(R.drawable.ic_sync_inprogress_48dp);
                            if (mStatusImageView.getAnimation() == null) {
                                mStatusImageView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.rotate));
                            }

                            mStatusTextView.setText(R.string.summary_timeline_state_sending);
                        } else {
                            mStatusImageView.clearAnimation();
                            mSendButton.setEnabled(true);
                            mStatusImageView.setImageResource(R.drawable.ic_sync_no_48dp);

                            mStatusTextView.setText(R.string.summary_timeline_state_unsent);
                        }
                    }
                } else {
                    mSendButton.setEnabled(false);
                    mStatusImageView.setImageResource(R.drawable.ic_sync_no_48dp);

                    mStatusTextView.setText(R.string.summary_timeline_state_unsent);
                }
            }
        });
    }

    public ArrayList<MissionReportAdapter> getAllDayReports(ArrayList<MissionReportAdapter> reports) {
        ArrayList<MissionReportAdapter> generatedReports = new ArrayList<>();
        Calendar startDate = CacheManager.getInstance().getData().getStartDate();
        Calendar endDate = CacheManager.getInstance().getData().getEndDate();
        SimpleDateFormat timeFormat = new SimpleDateFormat("H:mm", Locale.getDefault());

        String startingHour = timeFormat.format(startDate.getTime());
        String endingHour = timeFormat.format(endDate.getTime());
        int startingMinutes = startDate.get(Calendar.HOUR_OF_DAY) * 60 + startDate.get(Calendar.MINUTE);
        int endingMinutes = endDate.get(Calendar.HOUR_OF_DAY) * 60 + endDate.get(Calendar.MINUTE);

        for (int i = 0 ; i < reports.size() ; i++) {
            MissionReport report = reports.get(i).getMissionReport();

            if (i == 0 && !startingHour.equals(report.getDateTimeStart()) && report.getCompareDateStart() > startingMinutes) {
                generatedReports.add(new MissionReportAdapter(-1, MissionReport.createGeneratedReport(startingHour, report.getDateTimeStart())));
            }

            generatedReports.add(reports.get(i));

            if (i <= reports.size() - 2) {
                MissionReport nextReport = reports.get(i + 1).getMissionReport();

                if (!report.getDateTimeEnd().equals("-1:-1") && !report.getDateTimeEnd().equals(nextReport.getDateTimeStart()) && report.getCompareDateEnd() < nextReport.getCompareDateStart()) {
                    generatedReports.add(new MissionReportAdapter(-1, MissionReport.createGeneratedReport(report.getDateTimeEnd(), nextReport.getDateTimeStart())));
                }
            }

            if (i == reports.size() - 1 && !endingHour.equals(report.getDateTimeEnd()) && !report.getDateTimeEnd().equals("-1:-1") && report.getCompareDateEnd() < endingMinutes) {
                generatedReports.add(new MissionReportAdapter(-1, MissionReport.createGeneratedReport(report.getDateTimeEnd(), endingHour)));
            }
        }

        return generatedReports;
    }

    public boolean haveGeneratedReport() {
        ArrayList<MissionReport> reports = CacheManager.getInstance().getData().getMissionReports();
        ArrayList<MissionReportAdapter> generatedReports = new ArrayList<>();

        // Update reports array
        for (int i = 0 ; i < reports.size() ; i++) {
            generatedReports.add(new MissionReportAdapter(i, reports.get(i)));
        }

        // Sort reports
        Collections.sort(generatedReports, new Comparator<MissionReportAdapter>() {
            @Override
            public int compare(MissionReportAdapter m1, MissionReportAdapter m2) {
                return m1.getMissionReport().getCompareDateStart() < m2.getMissionReport().getCompareDateStart() ? -1 : 0;
            }
        });

        ArrayList<MissionReportAdapter> allDayReports = getAllDayReports(generatedReports);

        if (generatedReports.size() > 0) {
            MissionReport lastReport = generatedReports.get(reports.size() - 1).getMissionReport();
            if (lastReport.getTimelineDateEnd() == -1) {
                return true;
            }
        }

        return generatedReports.size() != allDayReports.size();
    }

    private void addLineToTimeline(RelativeLayout timelineLayout, int dateStart, int dateEnd, boolean generated, String formType) {
        int hourMin = mHourStart.get(Calendar.HOUR_OF_DAY) * 60 + mHourStart.get(Calendar.MINUTE);
        int hourMax = mHourEnd.get(Calendar.HOUR_OF_DAY) * 60 + mHourEnd.get(Calendar.MINUTE);

        float a = (0.95f) / (hourMax - hourMin);
        float b = - a * hourMin;

        float weightMin = a * dateStart + b;
        float weightMax = 1 - (a * dateEnd + b);
        float weightLine = 1 - weightMax - weightMin;

        LinearLayout linearLayout = new LinearLayout(getContext());
        setLinearLayoutParams(linearLayout, getContext(), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 0, 0);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        View view = new View(getContext());
        setLinearLayoutParams(view, getContext(), 0, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 0, 0);
        setLinearLayoutWeight(view, weightMin);

        View view2 = new View(getContext());
        setLinearLayoutParams(view2, getContext(), 0, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 0, 0);
        setLinearLayoutWeight(view2, weightMax);

        View line = new View(getContext());
        setLinearLayoutParams(line, getContext(), 0, dpToPx(getContext(), 2), 5, 0, 0, 0);
        setLinearLayoutWeight(line, weightLine);
        if (!generated) {
            if (formType != null) {
                if (formType.equals("sur-ligne")) {
                    line.setBackgroundColor(Color.parseColor("#AB47BC"));
                } else if (formType.equals("lieu-fixe")) {

                    line.setBackgroundColor(Color.parseColor("#1E88E5"));
                } else {
                    line.setBackgroundColor(Color.parseColor("#000000"));
                }
            }
            //line.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        } else {
            line.setBackgroundColor(getResources().getColor(R.color.colorTimelineGenerated));
        }

        linearLayout.addView(view);
        linearLayout.addView(line);
        linearLayout.addView(view2);

        timelineLayout.addView(linearLayout);
    }

    public void addCircleToTimeline(RelativeLayout timelineLayout, int hour) {
        int hourMin = mHourStart.get(Calendar.HOUR_OF_DAY) * 60 + mHourStart.get(Calendar.MINUTE);
        int hourMax = mHourEnd.get(Calendar.HOUR_OF_DAY) * 60 + mHourEnd.get(Calendar.MINUTE);

        float a = (0.95f) / (hourMax - hourMin);
        float b = - a * hourMin;
        float weight = a * hour + b;

        LinearLayout linearLayout = new LinearLayout(getContext());
        setLinearLayoutParams(linearLayout, getContext(), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 0, 0);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        View view = new View(getContext());
        setLinearLayoutParams(view, getContext(), 0, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 0, 0);
        setLinearLayoutWeight(view, weight);

        ImageView imageView = new ImageView(getContext());
        setLinearLayoutParams(imageView, getContext(), 0, dpToPx(getContext(), 10), 0, 0, 0, 0);
        setLinearLayoutWeight(imageView, 1.0f - weight);
        imageView.setImageResource(R.drawable.timeline_circle);
        imageView.setScaleType(ImageView.ScaleType.FIT_START);

        linearLayout.addView(view);
        linearLayout.addView(imageView);

        timelineLayout.addView(linearLayout);
    }

    private void setShowAllDay(boolean showAllDay) {
        mShowAllDay = showAllDay;

        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(SHOW_ALL_DAY_PREFERENCE, showAllDay);
        editor.apply();
    }

    @Override
    public void onSyncUpdate(MissionReport missionReport) {
        updateButton();
    }

    private class MissionReportAdapter {

        private int mIndex;

        private MissionReport mMissionReport;

        private int mHeight;

        public MissionReportAdapter(int index, MissionReport missionReport) {
            mIndex = index;
            mMissionReport = missionReport;
        }

        public int getIndex() {
            return mIndex;
        }

        public MissionReport getMissionReport() {
            return mMissionReport;
        }

        public int getHeight() {
            return mHeight;
        }

        public void setHeight(int height) {
            mHeight = height;
        }
    }

    private class MissionReportsArrayAdapter extends ArrayAdapter<MissionReportAdapter> {
        private List<MissionReportAdapter> mReports;

        public MissionReportsArrayAdapter(Context context, List<MissionReportAdapter> reports) {
            super(context, android.R.layout.simple_list_item_1, reports);

            mReports = reports;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public int getItemViewType(int position) {
            return mReports.get(position).getMissionReport().getAutomatic();
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final MissionReportAdapter missionReportAdapter = mReports.get(position);
            final MissionReport report = missionReportAdapter.getMissionReport();

            if (convertView == null) {
                if (report.getAutomatic() == 0) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_report_standard, parent, false);
                } else {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_report_generated, parent, false);
                }
            }

            TextView startingHourTextView = (TextView) convertView.findViewById(R.id.startingHourTextView);
            startingHourTextView.setText(report.getDateTimeStart());

            TextView endingHourTextView = (TextView) convertView.findViewById(R.id.endingHourTextView);
            if (!report.getDateTimeEnd().equals("-1:-1")) {
                endingHourTextView.setText(report.getDateTimeEnd());
            } else {
                endingHourTextView.setText("-");
            }

            if (report.getAutomatic() == 0) {
                TextView titleTextView = (TextView) convertView.findViewById(R.id.titleTextView);
                View colorSeparator = convertView.findViewById(R.id.view3);

                String formType = report.getMissionType().getForm().getLocationMode();

                if (formType.equals("sur-ligne")) {
                    String location = report.getLocation().toString();
                    /*if (!report.getLocationStart().isEmpty() && !report.getLocationEnd().isEmpty()) {
                        location += "\n" + report.getLocationStart() + " - " + report.getLocationEnd();
                    }*/ // TODO texte trop long

                    if (!location.isEmpty()) {
                        titleTextView.setText(location);
                    } else {
                        titleTextView.setText(R.string.create_sur_ligne);
                    }

                    colorSeparator.setBackgroundColor(Color.parseColor("#AB47BC"));
                } else if (formType.equals("lieu-fixe")) {
                    String location = report.getLocation().toString();

                    if (!location.isEmpty()) {
                        titleTextView.setText(location);
                    } else {
                        titleTextView.setText(R.string.create_lieu_fixe);
                    }

                    colorSeparator.setBackgroundColor(Color.parseColor("#1E88E5"));
                } else {
                    titleTextView.setText(R.string.create_hors_reseau);

                    colorSeparator.setBackgroundColor(Color.parseColor("#000000"));
                }
                // titleTextView.setText(AppUtils.getMissionFormType(getContext(), report.getMissionType().getForm().getLocationMode()));

                TextView missionTypeTextView = (TextView) convertView.findViewById(R.id.missionTypeTextView);
                missionTypeTextView.setText(report.getMissionType().getName());

                ImageView interpellationImageView = (ImageView) convertView.findViewById(R.id.interpellationImageView);
                TextView interpellationNumberTextView = (TextView) convertView.findViewById(R.id.interpellationNumberTextView);
                if (report.getMissionType().getForm().getInterpellationEnable().equals("1")) {
                    interpellationImageView.setVisibility(View.VISIBLE);
                    interpellationNumberTextView.setVisibility(View.VISIBLE);

                    interpellationNumberTextView.setText(String.valueOf(report.getInterpellations().size()));
                } else {
                    interpellationImageView.setVisibility(View.INVISIBLE);
                    interpellationNumberTextView.setVisibility(View.INVISIBLE);
                }
            }

            if (position == mReports.size() - 1) {
                convertView.findViewById(R.id.view2).setVisibility(View.INVISIBLE);
            } else {
                convertView.findViewById(R.id.view2).setVisibility(View.VISIBLE);
            }

            /*final View finalConvertView = convertView;
            convertView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    missionReportAdapter.setHeight(finalConvertView.getHeight());

                    Log.e("d", "" + finalConvertView.getHeight());

                    updateListHeight();
                }
            });*/

            return convertView;
        }
    }

    private void updateListHeight() {
        int height = 0;

        for (MissionReportAdapter reportAdapter : mReports) {
            height += reportAdapter.getHeight();
        }

        ViewGroup.LayoutParams par = mReportsListView.getLayoutParams();
        par.height = height;
        mReportsListView.setLayoutParams(par);
        mReportsListView.requestLayout();
    }
}
