package com.ipsis.scan.security.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.ipsis.scan.BuildConfig;
import com.ipsis.scan.R;
import com.ipsis.scan.communication.sending.NetworkManager;
import com.ipsis.scan.encryption.EncryptionManager;
import com.ipsis.scan.reporting.activities.SummaryActivity;
import com.ipsis.scan.reporting.data.CacheManager;
import com.ipsis.scan.security.locking.LockManager;
import com.ipsis.scan.utils.AppUtils;
import com.ipsis.scan.utils.SecurityUtils;
import org.w3c.dom.Text;

import java.security.NoSuchAlgorithmException;

public class LockActivity extends AppCompatActivity {

    /**
     * Connection progress dialog
     */
    private ProgressDialog mProgress;
    private AppCompatEditText mPasswordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("activitest", "activité " + this.getLocalClassName()  );


        setContentView(R.layout.activity_lock);

        Typeface ubuntuBoldTypeface = Typeface.createFromAsset(getAssets(), "fonts/Ubuntu-B.ttf");
        TextView titleTextView = (TextView) findViewById(R.id.titleTextView);
        titleTextView.setTypeface(ubuntuBoldTypeface);

        Typeface ubuntuTypeface = Typeface.createFromAsset(getAssets(), "fonts/Ubuntu-R.ttf");
        TextView title2TextView = (TextView) findViewById(R.id.title2TextView);
        title2TextView.setTypeface(ubuntuTypeface);

        TextView usernameTextView = (TextView) findViewById(R.id.usernameTextView);
        usernameTextView.setText(getString(R.string.lock_username, NetworkManager.getInstance().getNetworkConfiguration().getUsername()));

        mPasswordEditText = (AppCompatEditText) findViewById(R.id.passwordEditText);
        mPasswordEditText.setSupportBackgroundTintList(AppUtils.createEditTextColorStateList(this, Color.parseColor("#1E88E5")));
        //passwordEditText.setBackgroundColor(Color.TRANSPARENT);

        TextView informationsTextView = (TextView) findViewById(R.id.informationsTextView);
        informationsTextView.setText(getString(R.string.lock_informations_custom, CacheManager.getInstance().getData().getAddress()));

        Button disconnectButton = (Button) findViewById(R.id.disconnectButton);
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CacheManager.getInstance().saveCache(LockActivity.this, new CacheManager.CacheSavedCallback() {
                    @Override
                    public void onCacheSaved() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(LockActivity.this, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);

                                finish();
                            }
                        });
                    }
                });
            }
        });

        Button unlockButton = (Button) findViewById(R.id.unlockButton);
        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unlock();
            }
        });

        if (BuildConfig.DEBUG) {
            mPasswordEditText.setText("");

            // unlock();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mProgress != null) {
            mProgress.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
    }

    public void unlock() {
        mProgress = ProgressDialog.show(LockActivity.this, getString(R.string.lock_progress_title), getString(R.string.lock_progress_text), true);

        EncryptionManager encryptionManager = EncryptionManager.getInstance(LockActivity.this);
        try {
            String encodedPassphrase = SecurityUtils.sha256(NetworkManager.getInstance().getNetworkConfiguration().getUsername() + mPasswordEditText.getText().toString());

            if (encryptionManager.getEncodedPassphrase() != null && encryptionManager.getEncodedPassphrase().equals(encodedPassphrase)) {
                mProgress.dismiss();

                LockManager.getInstance().init(LockActivity.this);

                finish();
            } else {
                mProgress.dismiss();

                AppUtils.showErrorDialog(LockActivity.this, R.string.lock_error_connection);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();

            mProgress.dismiss();

            AppUtils.showErrorDialog(LockActivity.this, R.string.lock_error_connection);
        }
    }
}
