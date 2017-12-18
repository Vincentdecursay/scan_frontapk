package com.ipsis.scan.reporting.edition.model;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ipsis.scan.R;
import com.ipsis.scan.communication.sending.NetworkConfiguration;
import com.ipsis.scan.reporting.communication.SynchronisationRequest;
import com.ipsis.scan.reporting.data.CacheManager;
import com.ipsis.scan.database.model.Route;
import com.ipsis.scan.reporting.edition.view.*;
import com.ipsis.scan.utils.SerializableSparseArray;
import com.ipsis.scan.utils.SerializationUtils;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

import java.io.*;
import java.util.ArrayList;

import static com.ipsis.scan.utils.AppUtils.*;
import static com.ipsis.scan.utils.AppUtils.getIconResource;

/**
 * Created by pobouteau on 10/3/16.
 */

public class MissionReport implements Serializable {

    private static final long serialVersionUID = 600004L;

    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("mLocalId", String.class),
            new ObjectStreamField("mFormId", Integer.class),
            new ObjectStreamField("mDateTime", String.class),
            new ObjectStreamField("mTempId", String.class),
            new ObjectStreamField("mLocation", MissionLocation.class),
            new ObjectStreamField("mRoute", Route.class),
            new ObjectStreamField("mLocationStart", String.class),
            new ObjectStreamField("mLocationEnd", String.class),
            new ObjectStreamField("mLocationUser", String.class),
            new ObjectStreamField("mDateTimeStart", String.class),
            new ObjectStreamField("mDateTimeEnd", String.class),
            new ObjectStreamField("mAutomatic", Integer.class),
            new ObjectStreamField("mSync", Boolean.class),
            new ObjectStreamField("mSyncByUser", Boolean.class),
            new ObjectStreamField("mMissionValues", SerializableSparseArray.class),
            new ObjectStreamField("mInterpellations", ArrayList.class),
            new ObjectStreamField("mMissionType", MissionType.class)
    };

    protected String mLocalId;

    private Integer mFormId;

    private String mDateTime;

    private String mTempId;

    private MissionLocation mLocation;

    private Route mRoute;

    private String mLocationStart;

    private String mLocationEnd;

    private String mLocationUser;

    private String mDateTimeStart;

    private String mDateTimeEnd;

    private Integer mAutomatic;

    protected Boolean mSync;

    protected transient boolean mSyncing;

    protected transient boolean mCopy;

    private Boolean mSyncByUser;

    private SerializableSparseArray<MissionValue> mMissionValues;

    private ArrayList<MissionInterpellation> mInterpellations;

    private MissionType mMissionType;

    protected transient LinearLayout mView;

    protected transient ArrayList<ViewField> mViewFields;

    protected transient SyncObserver mSyncObserver;

    public MissionReport() {
        super();

        mMissionValues = new SerializableSparseArray<>();

        mInterpellations = new ArrayList<>();

        mSync = false;
        mSyncing = false;
        mSyncByUser = true;
        mCopy = false;
    }

    public View buildView(Context context) {
        return buildView(context, false);
    }

    protected View buildView(Context context, boolean interpellation) {
        mViewFields = new ArrayList<>();

        return buildType(context, interpellation);
    }

    private View buildType(Context context, boolean interpellation) {
        mView = new LinearLayout(context);
        setLinearLayoutParams(mView, context, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 0, 0);
        mView.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0 ; i < mMissionType.getFields().size() ; i++) {
            int keyGroup = mMissionType.getFields().keyAt(i);

            View groupView;
            if (interpellation && i == 0) {
                groupView = buildGroup(context, mMissionType.getFields().get(keyGroup), false);
            } else {
                groupView = buildGroup(context, mMissionType.getFields().get(keyGroup), true);
            }

            mView.addView(groupView);

            if (interpellation && i == 0) {
                View dropShadow = new View(context);
                setLinearLayoutParams(dropShadow, context, ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(context, 3), 0, 0, 0, 1);
                dropShadow.setBackgroundResource(R.drawable.toolbar_dropshadow);

                TextView formTextView = new TextView(context);
                setLinearLayoutParams(formTextView, context, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 8, 8, 0, 4);
                formTextView.setText(R.string.edit_form);
                formTextView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));

                mView.addView(dropShadow);
                mView.addView(formTextView);
            }
        }

        return mView;
    }

    private View buildGroup(Context context, SerializableSparseArray<MissionField> group, boolean card) {
        ViewGroup view;
        if (card) {
            view = new CardView(context);
            setLinearLayoutParams(view, context, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 8, 4, 8, 4);
        } else {
            view = new RelativeLayout(context);
            setLinearLayoutParams(view, context, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 0, 0);
            view.setBackgroundColor(Color.WHITE);
        }

        LinearLayout contentLayout = new LinearLayout(context);
        setLinearLayoutParams(contentLayout, context, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 0, 0);
        contentLayout.setOrientation(LinearLayout.VERTICAL);

        ViewField hideGroupViewField = null;

        for (int i = 0 ; i < group.size() ; i++) {
            int keyField = group.keyAt(i);
            MissionField field = group.get(keyField);

            View fieldLayout = buildField(context, field);
            contentLayout.addView(fieldLayout);

            if (hideGroupViewField != null) {
                hideGroupViewField.getHideViews().add(fieldLayout);
            }

            ViewField lastViewField = mViewFields.get(mViewFields.size() - 1);
            if (lastViewField.isHideGroup()) {
                hideGroupViewField = lastViewField;
            }

            if (i < group.size() - 1) {
                View divider = new View(context);
                setLinearLayoutParams(divider, context, ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(context, 1), 40, 0, 0, 0);
                divider.setBackgroundColor(context.getResources().getColor(R.color.colorCardDivider));
                contentLayout.addView(divider);

                if (hideGroupViewField != null) {
                    hideGroupViewField.getHideViews().add(divider);
                }
            }
        }

        if (hideGroupViewField != null) {
            hideGroupViewField.updateView();
        }

        view.addView(contentLayout);

        return view;
    }

    protected View buildField(Context context, MissionField missionField) {
        RelativeLayout contentLayout = new RelativeLayout(context);
        setLinearLayoutParams(contentLayout, context, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 0, 0); // dpToPx(context, 52)
        int padding = dpToPx(context, 8);
        contentLayout.setPadding(padding, padding, padding, padding);

        if (!missionField.getIcon().isEmpty()) {
            ImageView iconImageView = new ImageView(context);
            setRelativeLayoutParams(iconImageView, context, dpToPx(context, 36), dpToPx(context, 36), 0, 0, 0, 0);
            setRelativeLayoutRule(iconImageView, RelativeLayout.CENTER_VERTICAL);

            int imageResource = getIconResource(context, missionField.getIcon());
            if (imageResource != 0) {
                iconImageView.setImageResource(imageResource);

                contentLayout.addView(iconImageView);
            }
        }

        MissionValue missionValue = getMissionValue(missionField.getId());
        if (missionValue == null) {
            missionValue = new MissionValue();
            missionValue.setFieldId(missionField.getId());
            missionValue.setLabel(missionField.getLabel());
            missionValue.setValue(missionField.getValue());
            addMissionValue(missionValue);
        }

        ViewField viewField = createViewField(missionField, missionValue);
        viewField.appendView(context, contentLayout);
        viewField.setEnabled(!mSync && !mSyncing);
        mViewFields.add(viewField);

        return contentLayout;
    }

    private ViewField createViewField(MissionField missionField, MissionValue missionValue) {
        if (missionField.getType().equals(FieldType.SPINNER.name())) {
            return new SpinnerField(missionField, missionValue);
        } else if (missionField.getType().equals(FieldType.EDIT_TEXT.name())) {
            return new EditTextField(missionField, missionValue);
        } else if (missionField.getType().equals(FieldType.CHECKBOX.name())) {
            return new CheckboxField(missionField, missionValue);
        } else if (missionField.getType().equals(FieldType.TIME.name())) {
            return new TimeField(missionField, missionValue);
        } else if (missionField.getType().equals(FieldType.DATE.name())) {
            return new DateField(missionField, missionValue);
        } else if (missionField.getType().equals(FieldType.PLACE.name())) {
            return new PlaceField(missionField, missionValue);
        } else if (missionField.getType().equals(FieldType.NUMBER.name())) {
            return new NumberField(missionField, missionValue);
        } else {
            return new TextViewField(missionField, missionValue);
        }
    }

    public void addMissionValue(MissionValue missionValue) {
        mMissionValues.append(missionValue.getFieldId(), missionValue);
    }

    public MissionValue getMissionValue(int fieldId) {
        return mMissionValues.get(fieldId);
    }

    public String getLocalId() {
        return mLocalId;
    }

    public void setLocalId(String localId) {
        mLocalId = localId;
    }

    public int getFormId() {
        return mFormId;
    }

    public void setFormId(int formId) {
        mFormId = formId;
    }

    public String getDateTime() {
        return mDateTime;
    }

    public void setDateTime(String dateTime) {
        mDateTime = dateTime;
    }

    public String getTempId() {
        return mTempId;
    }

    public void setTempId(String tempId) {
        mTempId = tempId;
    }

    public MissionLocation getLocation() {
        return mLocation;
    }

    public void setLocation(MissionLocation location) {
        mLocation = location;
    }

    public Route getRoute() {
        return mRoute;
    }

    public void setRoute(Route route) {
        mRoute = route;
    }

    public String getLocationStart() {
        return mLocationStart;
    }

    public void setLocationStart(String locationStart) {
        mLocationStart = locationStart;
    }

    public String getLocationEnd() {
        return mLocationEnd;
    }

    public void setLocationEnd(String locationEnd) {
        mLocationEnd = locationEnd;
    }

    public String getLocationUser() {
        return mLocationUser;
    }

    public void setLocationUser(String locationUser) {
        mLocationUser = locationUser;
    }

    public String getDateTimeStart() {
        return mDateTimeStart;
    }

    public void setDateTimeStart(String dateTimeStart) {
        mDateTimeStart = dateTimeStart;
    }

    public String getDateTimeEnd() {
        return mDateTimeEnd;
    }

    public void setDateTimeEnd(String dateTimeEnd) {
        mDateTimeEnd = dateTimeEnd;
    }

    public int getAutomatic() {
        return mAutomatic;
    }

    public void setAutomatic(int automatic) {
        mAutomatic = automatic;
    }

    public boolean isSync() {
        return mSync;
    }

    public void setSync(boolean sync) {
        mSync = sync;

        if (mSyncObserver != null) {
            mSyncObserver.onSyncUpdate(this);
        }
    }

    public boolean isSyncing() {
        return mSyncing;
    }

    public void setSyncing(boolean syncing) {
        mSyncing = syncing;

        if (mSyncObserver != null) {
            mSyncObserver.onSyncUpdate(this);
        }
    }

    public boolean isCopy() {
        return mCopy;
    }

    public void setCopy(boolean copy) {
        mCopy = copy;
    }

    public boolean isSyncByUser() {
        return mSyncByUser;
    }

    public void setSyncByUser(boolean syncByUser) {
        mSyncByUser = syncByUser;
    }

    public SerializableSparseArray<MissionValue> getMissionValues() {
        return mMissionValues;
    }

    public ArrayList<MissionInterpellation> getInterpellations() {
        return mInterpellations;
    }

    public MissionType getMissionType() {
        return mMissionType;
    }

    public void setMissionType(MissionType missionType) {
        mMissionType = missionType;
    }

    public LinearLayout getView() {
        return mView;
    }

    public SyncObserver getSyncObserver() {
        return mSyncObserver;
    }

    public void setSyncObserver(SyncObserver syncObserver) {
        mSyncObserver = syncObserver;
    }

    public int getCompareDateStart() {
        String[] tokens = getDateTimeStart().split(":");

        return Integer.parseInt(tokens[0]) * 60 + Integer.parseInt(tokens[1]);
    }

    public int getCompareDateEnd() {
        String date = getDateTimeEnd();
        if (date.equals("-1:-1")) {
            date = getDateTimeStart();
        }

        String[] tokens = date.split(":");

        return Integer.parseInt(tokens[0]) * 60 + Integer.parseInt(tokens[1]);
    }

    public int getTimelineDateStart() {
        String[] tokens = getDateTimeStart().split(":");

        return Integer.parseInt(tokens[0]) * 60 + Integer.parseInt(tokens[1]);
    }

    public int getTimelineDateEnd() {
        String date = getDateTimeEnd();
        if (date.equals("-1:-1")) {
            return -1;
        }

        String[] tokens = date.split(":");

        return Integer.parseInt(tokens[0]) * 60 + Integer.parseInt(tokens[1]);
    }

    @Override
    public String toString() {
        return "MissionReport{" +
                "mFormId=" + mFormId +
                ", mTempId='" + mTempId + '\'' +
                ", mLocation='" + mLocation + '\'' +
                ", mLocationStart='" + mLocationStart + '\'' +
                ", mLocationEnd='" + mLocationEnd + '\'' +
                ", mLocationUser='" + mLocationUser + '\'' +
                ", mDateTimeStart='" + mDateTimeStart + '\'' +
                ", mDateTimeEnd='" + mDateTimeEnd + '\'' +
                ", mAutomatic=" + mAutomatic +
                '}';
    }

    public void print() {
        Log.e("report", toString());

        for (int i = 0 ; i < mMissionValues.size() ; i++) {
            int keyField = mMissionValues.keyAt(i);
            MissionValue value = mMissionValues.get(keyField);

            Log.e("report", value.getFieldId() + " " + value.getValue());
        }
    }

    public static MissionReport createGeneratedReport(String startingHour, String endingHour) {
        MissionReport missionReport = new MissionReport();

        missionReport.mAutomatic = 1;
        missionReport.mDateTimeStart = startingHour;
        missionReport.mDateTimeEnd = endingHour;

        return missionReport;
    }

    public void send(final Context context, final NetworkConfiguration networkConfiguration, final SyncCallback syncCallback) {
        if (!isSync()) {
            mSyncing = true;

            if (mSyncObserver != null) {
                mSyncObserver.onSyncUpdate(this);
            }

            if (mInterpellations.size() == 0) {
                sendReport(context, networkConfiguration, syncCallback);
            } else {
                ArrayList<MissionInterpellation> missionInterpellations = new ArrayList<>();
                for (final MissionInterpellation missionInterpellation : mInterpellations) {
                    if (!missionInterpellation.isSync()) {
                        missionInterpellations.add(missionInterpellation);
                    }
                }

                Observable.from(missionInterpellations)
                        .flatMap(new Func1<MissionInterpellation, Observable<MissionInterpellation>>() {
                            @Override
                            public Observable<MissionInterpellation> call(final MissionInterpellation missionInterpellation) {
                                return Observable.create(new Observable.OnSubscribe<MissionInterpellation>() {

                                    @Override
                                    public void call(final Subscriber<? super MissionInterpellation> subscriber) {
                                        missionInterpellation.send(context, networkConfiguration, new SyncCallback() {
                                            @Override
                                            public void onSyncSucceed() {
                                                subscriber.onNext(missionInterpellation);
                                                subscriber.onCompleted();
                                            }

                                            @Override
                                            public void onSyncFailure(Exception e) {
                                                subscriber.onError(e);
                                            }
                                        });
                                    }
                                });
                            }
                        })
                        .retry(2)
                        .doOnCompleted(new Action0() {
                            @Override
                            public void call() {
                                sendReport(context, networkConfiguration, syncCallback);
                            }
                        })
                        .subscribe(new Action1<MissionInterpellation>() {
                            @Override
                            public void call(MissionInterpellation missionInterpellation) {

                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                // setSyncing(false);

                                if (syncCallback != null) {
                                    syncCallback.onSyncFailure(new Exception(throwable));
                                }
                            }
                        });
            }
        } else {
            if (syncCallback != null) {
                syncCallback.onSyncSucceed();
            }
        }
    }

    private void sendReport(final Context context, final NetworkConfiguration networkConfiguration, final SyncCallback syncCallback) {
        Observable.create(new Observable.OnSubscribe<MissionReport>() {
                    @Override
                    public void call(final Subscriber<? super MissionReport> subscriber) {
                        sendReportObservable(context, networkConfiguration, new SyncCallback() {
                            @Override
                            public void onSyncSucceed() {
                                subscriber.onNext(MissionReport.this);
                                subscriber.onCompleted();
                            }

                            @Override
                            public void onSyncFailure(Exception e) {
                                subscriber.onError(e);
                            }
                        });
                    }
                })
                .retry(2)
                .subscribe(new Action1<MissionReport>() {
                    @Override
                    public void call(MissionReport missionReport) {
                        syncCallback.onSyncSucceed();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        syncCallback.onSyncFailure(new Exception(throwable));
                    }
                });
    }

    private void sendReportObservable(final Context context, final NetworkConfiguration networkConfiguration, final SyncCallback syncCallback) {
        SynchronisationRequest synchronisationRequest = new SynchronisationRequest(networkConfiguration);
        synchronisationRequest.sendReport(this, new SynchronisationRequest.Callback() {
            @Override
            public void onSyncSucceed() {
                mSync = true;
                mSyncing = false;

                if (mSyncObserver != null) {
                    mSyncObserver.onSyncUpdate(MissionReport.this);
                }

                if (!mCopy) {
                    CacheManager.getInstance().saveCache(context);
                }

                if (syncCallback != null) {
                    syncCallback.onSyncSucceed();
                }
            }

            @Override
            public void onSyncFailure(Exception e) {
                Log.e("SendReport", "onSyncFailure: ", e);

                /*mSyncing = false;

                if (mSyncObserver != null) {
                    mSyncObserver.onSyncUpdate(MissionReport.this);
                }*/

                if (syncCallback != null) {
                    syncCallback.onSyncFailure(e);
                }
            }
        });
    }

    protected void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        SerializationUtils.readObject(this, input, serialPersistentFields);
    }

    protected void writeObject(ObjectOutputStream output) throws IOException {
        SerializationUtils.writeObject(this, output, serialPersistentFields);
    }

    public interface SyncCallback {
        void onSyncSucceed();

        void onSyncFailure(Exception exception);
    }

    public interface SyncObserver {
        void onSyncUpdate(MissionReport missionReport);
    }
}
