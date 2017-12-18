package com.ipsis.scan.reporting.edition.model;

import android.content.Context;
import android.util.Log;
import android.view.View;
import com.ipsis.scan.communication.sending.NetworkConfiguration;
import com.ipsis.scan.reporting.communication.SynchronisationRequest;
import com.ipsis.scan.reporting.data.CacheManager;
import com.ipsis.scan.reporting.edition.view.PlaceField;
import com.ipsis.scan.reporting.edition.view.TimeField;
import com.ipsis.scan.reporting.edition.view.ViewField;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

/**
 * Created by pobouteau on 10/7/16.
 */

public class MissionInterpellation extends MissionReport {

    private static final long serialVersionUID = 100003L;

    private transient TimeField mTimeField;

    private transient PlaceField mLocationField;

    @Override
    public View buildView(Context context) {
        // return buildView(context, true);
        return buildView(context, false);
    }

    @Override
    protected View buildField(Context context, MissionField missionField) {
        View view = super.buildField(context, missionField);

        for (ViewField viewField : mViewFields) {
            String header = viewField.getHeader();
            if (header != null) {
                if (header.equals("hour")) {
                    mTimeField = (TimeField) viewField;
                } else if (header.equals("location")) {
                    mLocationField = (PlaceField) viewField;
                }
            }

            if (mTimeField != null && mLocationField != null) {
                break;
            }
        }

        return view;
    }

    public TimeField getTimeField() {
        return mTimeField;
    }

    public PlaceField getLocationField() {
        return mLocationField;
    }

    @Override
    public void send(final Context context, final NetworkConfiguration networkConfiguration, final SyncCallback syncCallback) {
        if (!isSync()) {
            sendInterpellationObservable(context, networkConfiguration, syncCallback);
        } else {
            if (syncCallback != null) {
                syncCallback.onSyncSucceed();
            }
        }
    }

    private void sendInterpellationObservable(final Context context, final NetworkConfiguration networkConfiguration, final SyncCallback syncCallback) {
        Observable.create(new Observable.OnSubscribe<MissionInterpellation>() {
            @Override
            public void call(final Subscriber<? super MissionInterpellation> subscriber) {
                        sendInterpellation(context, networkConfiguration, new SyncCallback() {
                            @Override
                            public void onSyncSucceed() {
                                subscriber.onNext(MissionInterpellation.this);
                                subscriber.onCompleted();
                            }

                            @Override
                            public void onSyncFailure(Exception e) {
                                subscriber.onError(e);
                            }
                        });
                    }
                })
                .retry(10)
                .subscribe(new Action1<MissionInterpellation>() {
                    @Override
                    public void call(MissionInterpellation missionInterpellation) {
                        syncCallback.onSyncSucceed();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        syncCallback.onSyncFailure(new Exception(throwable));
                    }
                });
    }

    private void sendInterpellation(final Context context, final NetworkConfiguration networkConfiguration, final SyncCallback syncCallback) {
        mSyncing = true;

        if (mSyncObserver != null) {
            mSyncObserver.onSyncUpdate(this);
        }

        SynchronisationRequest synchronisationRequest = new SynchronisationRequest(networkConfiguration);
        synchronisationRequest.sendInterpellation(this, new SynchronisationRequest.Callback() {
            @Override
            public void onSyncSucceed() {
                mSyncing = false;
                mSync = true;

                if (mSyncObserver != null) {
                    mSyncObserver.onSyncUpdate(MissionInterpellation.this);
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
                Log.e("SendInterpellation", "onSyncFailure: ", e);

                /*mSyncing = false;

                if (mSyncObserver != null) {
                    mSyncObserver.onSyncUpdate(MissionInterpellation.this);
                }*/

                if (syncCallback != null) {
                    syncCallback.onSyncFailure(e);
                }
            }
        });
    }
}
