package com.ipsis.scan.security.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.*;
import com.ipsis.scan.BuildConfig;
import com.ipsis.scan.R;
import com.ipsis.scan.communication.sending.NetworkManager;
import com.ipsis.scan.encryption.EncryptionManager;
import com.ipsis.scan.reporting.activities.InitializationActivity;
import com.ipsis.scan.reporting.activities.SummaryActivity;
import com.ipsis.scan.reporting.communication.FormUpdateRequest;
import com.ipsis.scan.reporting.communication.LocationUpdateRequest;
import com.ipsis.scan.reporting.communication.service.SynchronisationService;
import com.ipsis.scan.reporting.data.CacheManager;
import com.ipsis.scan.security.communication.AuthenticationException;
import com.ipsis.scan.security.communication.AuthenticationRequest;
import com.ipsis.scan.security.locking.LockManager;
import com.ipsis.scan.utils.AppUtils;
import com.ipsis.scan.utils.PhoneUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity implements AuthenticationRequest.ConnectionCallback {

    private static final int PERMISSIONS_REQUEST = 500;

    public static final String LOGIN_PREFERENCES = "LoginPreferences";

    public static final String LAST_USERNAME_PREFERENCE = "lastUsername";

    /**
     * Connection progress dialog
     */
    private ProgressDialog mProgress;

    /**
     * Current username
     */
    private String mUsername;

    /**
     * Current password
     */
    private String mPassword;

    private SharedPreferences mPreferences;

    private boolean isKeyboardOpened = false;

    private AuthenticationRequest mAuthenticationRequest;
    private LocationUpdateRequest mLocationUpdateRequest;
    private AppCompatEditText mLoginEditText;
    private AppCompatEditText mPasswordEditText;
    private ScrollView mLoginScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mPreferences = getSharedPreferences(LOGIN_PREFERENCES, Context.MODE_PRIVATE);

        Typeface ubuntuBoldTypeface = Typeface.createFromAsset(getAssets(), "fonts/Ubuntu-B.ttf");
        TextView titleTextView = (TextView) findViewById(R.id.titleTextView);
        titleTextView.setTypeface(ubuntuBoldTypeface);

        Typeface ubuntuTypeface = Typeface.createFromAsset(getAssets(), "fonts/Ubuntu-R.ttf");
        TextView title2TextView = (TextView) findViewById(R.id.title2TextView);
        title2TextView.setTypeface(ubuntuTypeface);

        mLoginEditText = (AppCompatEditText) findViewById(R.id.loginEditText);
        mLoginEditText.setSupportBackgroundTintList(AppUtils.createEditTextColorStateList(this, Color.parseColor("#1E88E5")));
        mLoginEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mLoginScrollView.fullScroll(View.FOCUS_DOWN);

                mLoginEditText.requestFocus();

                return false;
            }
        });
        mLoginEditText.setText(mPreferences.getString(LAST_USERNAME_PREFERENCE, ""));
        //loginEditText.setBackgroundColor(Color.TRANSPARENT);

        mPasswordEditText = (AppCompatEditText) findViewById(R.id.passwordEditText);
        mPasswordEditText.setSupportBackgroundTintList(AppUtils.createEditTextColorStateList(this, Color.parseColor("#1E88E5")));
        mPasswordEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mLoginScrollView.fullScroll(View.FOCUS_DOWN);

                mPasswordEditText.requestFocus();

                return false;
            }
        });
        //passwordEditText.setBackgroundColor(Color.TRANSPARENT);

        registerKeyboardEvents();
        mLoginScrollView = (ScrollView) findViewById(R.id.loginScrollView);
        mLoginScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mLoginScrollView.fullScroll(View.FOCUS_DOWN);
            }
        }, 500);

        Button connectionButton = (Button) findViewById(R.id.connectionButton);
        connectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUsername = mLoginEditText.getText().toString();
                mPassword = mPasswordEditText.getText().toString();

                connect();
            }
        });

        askPermissions();

        EncryptionManager.getInstance(LoginActivity.this).clear();

        String usernames = "";
        JSONObject usersSynced = SynchronisationService.getUsersSynced(this);
        try {
            Iterator<String> keys = usersSynced.keys();
            while(keys.hasNext())  {
                String username = keys.next();
                JSONObject user = usersSynced.getJSONObject(username);
                boolean synced = true;
                String date = "";
                try {
                    synced = user.getBoolean("synced");
                    date = user.getString("date");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (!synced) {
                    usernames += " - " + username + " (" + date + ")\n";
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ImageView unsentImageView = (ImageView) findViewById(R.id.unsentReportImageView);
        TextView unsentTextView = (TextView) findViewById(R.id.unsentReportTextView);
        if (usernames.isEmpty()) {
            unsentImageView.setVisibility(View.GONE);
            unsentTextView.setVisibility(View.GONE);
        } else {
            final String finalUsernames = usernames;
            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setMessage(getString(R.string.login_unsent_report, finalUsernames))
                            .setPositiveButton(R.string.button_close, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                    builder.create().show();
                }
            };
            unsentImageView.setOnClickListener(clickListener);
            unsentTextView.setOnClickListener(clickListener);
        }

        if (BuildConfig.DEBUG) {
            // mLoginEditText.setText("");
            mPasswordEditText.setText("");

            // connect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mProgress != null) {
            mProgress.dismiss();
        }
    }

    public void registerKeyboardEvents(){
        final View activityRootView = getWindow().getDecorView().findViewById(android.R.id.content);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > 100 ) {
                    if(!isKeyboardOpened){
                        mLoginScrollView.fullScroll(View.FOCUS_DOWN);
                    }
                    isKeyboardOpened = true;
                } else if(isKeyboardOpened) {
                    isKeyboardOpened = false;
                }
            }
        });
    }

    public void connect() {
        mProgress = ProgressDialog.show(LoginActivity.this, getString(R.string.login_progress_title), getString(R.string.login_progress_text), true);
        mProgress.setCancelable(false);

        mAuthenticationRequest = new AuthenticationRequest(mUsername, mPassword, PhoneUtils.getImei(LoginActivity.this), "0");
        mAuthenticationRequest.login(LoginActivity.this);
    }

    @Override
    public void onConnectionSucceed() {
        EncryptionManager encryptionManager = EncryptionManager.getInstance(LoginActivity.this);
        encryptionManager.initialization(mUsername, mUsername + mPassword, new EncryptionManager.InitializationCallback() {
            @Override
            public void onInitialized() {
                startNextActivity();
            }

            @Override
            public void onError() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgress.dismiss();

                        AppUtils.showErrorDialog(LoginActivity.this, R.string.login_error_encryption);
                    }
                });
            }
        });
    }

    @Override
    public void onChangePassword() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgress.dismiss();

                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle(R.string.login_change_password_title)
                        .setMessage(R.string.login_change_password_message)
                        .setPositiveButton(R.string.button_continue, null)
                        .setNegativeButton(R.string.button_cancel, null)
                        .setView(R.layout.dialog_change_password);
                final AlertDialog dialog = builder.create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                EditText password1 = (EditText) dialog.findViewById(R.id.password1EditText);
                                EditText password2 = (EditText) dialog.findViewById(R.id.password2EditText);

                                if (password1 != null && password2 != null) {
                                    if (password1.getText().toString().length() >= 8) {
                                        if (password1.getText().toString().equals(password2.getText().toString())) {
                                            mPassword = password1.getText().toString();

                                            mProgress = ProgressDialog.show(LoginActivity.this, getString(R.string.login_progress_title), getString(R.string.login_progress_text), true);
                                            mProgress.setCancelable(false);

                                            mAuthenticationRequest.password(mPassword, new AuthenticationRequest.PasswordChangedCallback() {
                                                @Override
                                                public void onPasswordChanged() {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            dialog.dismiss();
                                                            mProgress.dismiss();

                                                            connect();
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onFailure(Exception e) {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            AppUtils.showErrorDialog(LoginActivity.this, R.string.login_change_password_connection);
                                                        }
                                                    });
                                                }
                                            });
                                        } else {
                                            AppUtils.showErrorDialog(LoginActivity.this, R.string.login_change_password_confirmation);
                                        }
                                    } else {
                                        AppUtils.showErrorDialog(LoginActivity.this, R.string.login_change_password_length);
                                    }
                                }
                            }
                        });
                    }
                });
                dialog.show();
            }
        });
    }

    @Override
    public void onConnectionFailure(Exception e) {
        if (e instanceof AuthenticationException) {
            final AuthenticationException exception = (AuthenticationException) e;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgress.dismiss();

                    if (exception.getCode() == 102) {
                        AppUtils.showErrorDialog(LoginActivity.this, R.string.login_error_device);
                    } else {
                        AppUtils.showErrorDialog(LoginActivity.this, R.string.login_error_connection);
                    }
                }
            });
        } else {
            final EncryptionManager encryptionManager = EncryptionManager.getInstance(LoginActivity.this);
            encryptionManager.checkKeys(mUsername, mUsername + mPassword, new EncryptionManager.KeysLoaderCallback() {
                @Override
                public void onKeysLoaded() {
                    CacheManager.getInstance().setOfflineMode(true);
                    NetworkManager.getInstance().getNetworkConfiguration().setUsername(mUsername);

                    mAuthenticationRequest.retryLogin();

                    startNextActivity();
                }

                @Override
                public void onError() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgress.dismiss();

                            if (encryptionManager.isKeysExist(mUsername)) {
                                AppUtils.showErrorDialog(LoginActivity.this, R.string.login_error_connection);
                            } else {
                                AppUtils.showErrorDialog(LoginActivity.this, R.string.login_error_first_time);
                            }
                        }
                    });
                }
            });

            e.printStackTrace();
        }
    }

    /**
     * Start next activity (runOnUiThread)
     */
    public void startNextActivity() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(LAST_USERNAME_PREFERENCE, NetworkManager.getInstance().getUsername());
        editor.apply();

        CacheManager.getInstance().loadCache(this, new CacheManager.CacheLoaderCallback() {
            @Override
            public void onCacheLoaded() {
                Log.e("cache", "onCacheLoaded");

                boolean synced = CacheManager.getInstance().getData().isSynced();
                if (!synced) {
                    startSummaryActivity();
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setTitle(R.string.login_error_cache_sent_title)
                            .setMessage(getString(R.string.login_error_cache_sent_message))
                            .setPositiveButton(R.string.login_error_cache_sent_open, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startSummaryActivity();
                                }
                            }).setNeutralButton(R.string.login_error_cache_sent_unlock, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                    builder.setTitle(R.string.login_error_erase_cache_title)
                                            .setMessage(R.string.login_error_cache_sent_confirm_message)
                                            .setPositiveButton(R.string.login_error_erase_cache_erase, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    CacheManager.getInstance().getData().resetSync();
                                                    CacheManager.getInstance().saveCache(LoginActivity.this, new CacheManager.CacheSavedCallback() {
                                                        @Override
                                                        public void onCacheSaved() {
                                                            startSummaryActivity();
                                                        }
                                                    });
                                                }
                                            }).setNeutralButton(R.string.login_error_erase_cache_cancel, null);
                                    builder.create().show();
                                }
                            });

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mProgress != null) {
                                mProgress.dismiss();
                            }

                            builder.create().show();
                        }
                    });
                }
            }

            @Override
            public void onCacheInvalidated() {
                Log.e("cache", "onCacheInvalidated");

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String dateFormatted = dateFormat.format(CacheManager.getInstance().getData().getDate().getTime());

                boolean synced = CacheManager.getInstance().getData().isSynced();
                if (!synced) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setTitle(R.string.login_error_cache_invalidated_title)
                            .setMessage(getString(R.string.login_error_cache_invalidated_message, dateFormatted))
                            .setPositiveButton(R.string.button_open, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startSummaryActivity();
                                }
                            }).setNeutralButton(R.string.login_error_cache_invalidated_new, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                            builder.setTitle(R.string.login_error_erase_cache_title)
                                    .setMessage(R.string.login_error_erase_cache_message)
                                    .setPositiveButton(R.string.login_error_erase_cache_erase, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            updateFormsDatabase();
                                        }
                                    }).setNeutralButton(R.string.login_error_erase_cache_cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                            builder.create().show();
                        }
                    });

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mProgress != null) {
                                mProgress.dismiss();
                            }

                            builder.create().show();
                        }
                    });
                } else {
                    updateFormsDatabase();
                }
            }

            @Override
            public void onCacheError() {
                Log.e("cache", "onCacheError");

                updateFormsDatabase();
            }
        });
    }

    public void updateFormsDatabase() {
        CacheManager.getInstance().getData().initDay();

        CacheManager.getInstance().saveCache(this);

        // Update forms or use local data if no internet
        FormUpdateRequest updateRequest = new FormUpdateRequest();
        updateRequest.update(new FormUpdateRequest.Callback() {
            @Override
            public void onUpdateSucceed() {
                CacheManager.getInstance().saveCache(LoginActivity.this);

                checkLocationsDatabase();
            }

            @Override
            public void onUpdateFailure(Exception e) {
                e.printStackTrace();

                if (CacheManager.getInstance().getData().getMissionForms() != null) {
                    checkLocationsDatabase();
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgress.dismiss();

                            AppUtils.showErrorDialog(LoginActivity.this, R.string.login_error_first_time);
                        }
                    });
                }
            }
        });
    }

    public void checkLocationsDatabase() {
        mLocationUpdateRequest = new LocationUpdateRequest();
        mLocationUpdateRequest.checkForUpdate(this, new LocationUpdateRequest.CheckForUpdateCallback() {
            @Override
            public void onUpdateAvailable(boolean localData) {
                if (localData) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setTitle(R.string.login_location_update_title)
                            .setMessage(R.string.login_location_update_message)
                            .setPositiveButton(R.string.login_location_update_continue, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startInitializationActivity();
                                }
                            }).setNeutralButton(R.string.login_location_update_update, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    downloadLocationsDatabase();
                                }
                            });

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mProgress != null) {
                                mProgress.dismiss();
                            }

                            builder.create().show();
                        }
                    });
                } else {
                    downloadLocationsDatabase();
                }
            }

            @Override
            public void onNoUpdateAvailable() {
                startInitializationActivity();
            }

            @Override
            public void onUpdateFailure(boolean localData, Exception e) {
                e.printStackTrace();

                if (localData) {
                    startInitializationActivity();
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgress.dismiss();

                            AppUtils.showErrorDialog(LoginActivity.this, R.string.login_error_location_download_message);
                        }
                    });
                }
            }
        });
    }

    public void downloadLocationsDatabase() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgress != null) {
                    mProgress.dismiss();
                }

                mProgress = new ProgressDialog(LoginActivity.this);
                mProgress.setMessage(getString(R.string.login_location_update_progress_message));
                mProgress.setCancelable(false);
                mProgress.setIndeterminate(false);
                mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgress.setMax(100);
                mProgress.show();

                mLocationUpdateRequest.update(mProgress, new LocationUpdateRequest.UpdateCallback() {
                    @Override
                    public void onUpdateSucceed() {
                        startInitializationActivity();
                    }

                    @Override
                    public void onUpdateFailure(Exception e) {
                        e.printStackTrace();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgress.dismiss();

                                AppUtils.showErrorDialog(LoginActivity.this, R.string.login_error_location_download_message);
                            }
                        });
                    }
                });
            }
        });


    }

    public void startSummaryActivity() {
        LockManager.getInstance().init(this);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(LoginActivity.this, SummaryActivity.class));

                finish();
            }
        });
    }

    public void startInitializationActivity() {
        LockManager.getInstance().init(LoginActivity.this);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(LoginActivity.this, InitializationActivity.class));

                finish();
            }
        });
    }

    public void askPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                if (grantResults.length >= 3 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED) {


                } else {
                    askPermissions();
                }

                break;
            }
        }
    }
}
