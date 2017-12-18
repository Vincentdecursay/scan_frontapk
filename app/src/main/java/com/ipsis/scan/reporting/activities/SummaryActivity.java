package com.ipsis.scan.reporting.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ipsis.scan.R;
import com.ipsis.scan.communication.sending.NetworkManager;
import com.ipsis.scan.encryption.EncryptionManager;
import com.ipsis.scan.reporting.data.CacheManager;
import com.ipsis.scan.reporting.edition.model.MissionReport;
import com.ipsis.scan.security.activities.LoginActivity;
import com.ipsis.scan.settings.AboutActivity;
import com.ipsis.scan.settings.SettingsActivity;
import com.ipsis.scan.utils.Constants;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

public class SummaryActivity extends ReportingActivity {

    /**
     * Tag pour les logs.
     */
    private static final String TAG = SummaryActivity.class.getSimpleName();

    /**
     * Request Id pour la vérification de Google Play Services
     */
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Menu drawer
     */
    private DrawerLayout mDrawerLayout;

    private Drawer mDrawer;

    private String mLastDateTimeEnd;

    private MissionReport mLastMissionReport;

    public SummaryActivity() {
        super();

        mLastDateTimeEnd = "";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        /*if (checkPlayServices()) {
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }*/

        if (EncryptionManager.getInstance(this).getEncodedPassphrase() == null || CacheManager.getInstance().getData() == null) {
            startActivity(new Intent(this, LoginActivity.class));

            finish();

            return;
        }

        String token = FirebaseInstanceId.getInstance().getToken();
        if (token != null) {
            Log.e(TAG, "Token: " + token);
        } else {
            Log.e(TAG, "Waiting for firebase token...");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle(R.string.summary_title);

        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.login_background)
                .addProfiles(
                        new ProfileDrawerItem().withName(getString(R.string.summary_menu_agent)).withEmail(NetworkManager.getInstance().getNetworkConfiguration().getUsername()).withIcon(ContextCompat.getDrawable(SummaryActivity.this, R.drawable.ic_person_white_48dp))
                )
                .withSelectionListEnabled(false)
                .withAlternativeProfileHeaderSwitching(false)
                .build();

        final PrimaryDrawerItem reportsItem = new PrimaryDrawerItem().withName(getString(R.string.summary_title)).withIcon(R.drawable.ic_description_grey_48dp);
        final PrimaryDrawerItem disconnectItem = new PrimaryDrawerItem().withName(getString(R.string.summary_menu_disconnect)).withIcon(R.drawable.ic_exit_to_app_grey_48dp);

        final SecondaryDrawerItem settingsItem = new SecondaryDrawerItem().withName(getString(R.string.settings_title)).withIcon(R.drawable.ic_settings_grey_48dp);
        final SecondaryDrawerItem commentItem = new SecondaryDrawerItem().withName(getString(R.string.summary_menu_comment)).withIcon(R.drawable.ic_comment_grey_48dp);
        final SecondaryDrawerItem aboutItem = new SecondaryDrawerItem().withName(getString(R.string.summary_menu_about)).withIcon(R.drawable.ic_help_grey_48dp);

        DrawerBuilder drawerBuilder = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        reportsItem,
                        disconnectItem,
                        new DividerDrawerItem(),
                        settingsItem,
                        commentItem,
                        aboutItem
                ).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem == reportsItem) {
                            mDrawer.closeDrawer();
                        } else if (drawerItem == disconnectItem) {
                            if (!CacheManager.getInstance().getData().isSynced()) {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(SummaryActivity.this);
                                builder.setMessage(R.string.summary_disconnect_title)
                                        .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                CacheManager.getInstance().saveCache(SummaryActivity.this, new CacheManager.CacheSavedCallback() {
                                                    @Override
                                                    public void onCacheSaved() {
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                startActivity(new Intent(SummaryActivity.this, LoginActivity.class));

                                                                finish();
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        }).setNeutralButton(R.string.button_no, null);
                                builder.create().show();
                            } else {
                                CacheManager.getInstance().saveCache(SummaryActivity.this, new CacheManager.CacheSavedCallback() {
                                    @Override
                                    public void onCacheSaved() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                startActivity(new Intent(SummaryActivity.this, LoginActivity.class));

                                                finish();
                                            }
                                        });
                                    }
                                });
                            }

                            mDrawer.closeDrawer();
                        } else if (drawerItem == settingsItem) {
                            Intent intent = new Intent(SummaryActivity.this, SettingsActivity.class);
                            intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.GeneralPreferenceFragment.class.getName());
                            intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
                            startActivity(intent);
                        } else if (drawerItem == commentItem) {
                            Intent intent = new Intent(Intent.ACTION_SENDTO);
                            intent.setType("text/plain");
                            intent.setData(Uri.parse("mailto:" + CacheManager.getInstance().getData().getCommentEmail()));
                            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.summary_email_subject));
                            startActivity(intent);
                        } else if (drawerItem == aboutItem) {
                            Intent intent = new Intent(SummaryActivity.this, AboutActivity.class);
                            intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, AboutActivity.GeneralPreferenceFragment.class.getName());
                            intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
                            startActivity(intent);
                        }

                        return false;
                    }
                }).withCloseOnClick(false);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //drawerBuilder.withTranslucentStatusBar(true);
            /*Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#1565C0"));*/
        } else {
            drawerBuilder.withTranslucentStatusBar(false);
        }

        mDrawer = drawerBuilder.build();

        final FloatingActionsMenu fab = (FloatingActionsMenu) findViewById(R.id.fab);
        if (CacheManager.getInstance().getData().isSynced() || CacheManager.getInstance().getData().isSyncing()) {
            fab.setVisibility(View.GONE);
        }

        FloatingActionButton mission1Button = (FloatingActionButton) findViewById(R.id.mission1Button);
        mission1Button.setIcon(R.drawable.ic_hors_reseau);
        mission1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SummaryActivity.this, CreateActivity.class);
                intent.putExtra(CreateActivity.EXTRA_FORM_TYPE, Constants.TYPE_HORS_RESEAU);
                intent.putExtra(CreateActivity.EXTRA_FORM_DATE_TIME_START, mLastDateTimeEnd);
                intent.putExtra(CreateActivity.EXTRA_FORM_LAST_REPORT, mLastMissionReport);
                startActivity(intent);

                fab.collapse();
            }
        });
        if (CacheManager.getInstance().getData().getMissionForms().getHorsReseauForms().size() == 0) {
            mission1Button.setVisibility(View.GONE);
        }

        FloatingActionButton mission2Button = (FloatingActionButton) findViewById(R.id.mission2Button);
        mission2Button.setIcon(R.drawable.ic_lieu_fixe);
        mission2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SummaryActivity.this, CreateActivity.class);
                intent.putExtra(CreateActivity.EXTRA_FORM_TYPE, Constants.TYPE_LIEU_FIXE);
                intent.putExtra(CreateActivity.EXTRA_FORM_DATE_TIME_START, mLastDateTimeEnd);
                intent.putExtra(CreateActivity.EXTRA_FORM_LAST_REPORT, mLastMissionReport);
                startActivity(intent);

                fab.collapse();
            }
        });
        if (CacheManager.getInstance().getData().getMissionForms().getLieuFixeForms().size() == 0) {
            mission2Button.setVisibility(View.GONE);
        }

        FloatingActionButton mission3Button = (FloatingActionButton) findViewById(R.id.mission3Button);
        mission3Button.setIcon(R.drawable.ic_sur_ligne);
        mission3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SummaryActivity.this, CreateActivity.class);
                intent.putExtra(CreateActivity.EXTRA_FORM_TYPE, Constants.TYPE_SUR_LIGNE);
                intent.putExtra(CreateActivity.EXTRA_FORM_DATE_TIME_START, mLastDateTimeEnd);
                intent.putExtra(CreateActivity.EXTRA_FORM_LAST_REPORT, mLastMissionReport);
                startActivity(intent);

                fab.collapse();
            }
        });
        if (CacheManager.getInstance().getData().getMissionForms().getSurLigneForms().size() == 0) {
            mission3Button.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen()) {
            mDrawer.closeDrawer();
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.summary_close_app_title)
                    .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SummaryActivity.super.onBackPressed();
                        }
                    }).setNeutralButton(R.string.button_no, null);
            builder.create().show();
        }
    }

    /**
     * Vérifie la présence des services Google
     *
     * @return true si supportés
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    public void setLastDateTimeEnd(String lastDateTimeEnd) {
        mLastDateTimeEnd = lastDateTimeEnd;
    }

    public MissionReport getLastMissionReport() {
        return mLastMissionReport;
    }

    public void setLastMissionReport(MissionReport lastMissionReport) {
        mLastMissionReport = lastMissionReport;
    }
}
