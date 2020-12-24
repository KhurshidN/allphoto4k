package com.app.allpaper4k.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.app.allpaper4k.Config;
import com.app.allpaper4k.R;
import com.app.allpaper4k.fragments.FragmentClassic;
import com.app.allpaper4k.fragments.FragmentExplore;
import com.app.allpaper4k.fragments.FragmentFavorite;
import com.app.allpaper4k.utilities.DBHelper;
import com.app.allpaper4k.utilities.GDPR;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdView;
import com.google.android.material.navigation.NavigationView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.IOException;
import java.util.List;

/**
 * Created by khurshidnormurodov on 12/24/20
 * */

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private final static String COLLAPSING_TOOLBAR_FRAGMENT_TAG = "collapsing_toolbar";
    private final static String CATEGORY_FRAGMENT_TAG = "category";
    private final static String FAVORITE_FRAGMENT_TAG = "favorite";
    private final static String RATE_FRAGMENT_TAG = "rate";
    private final static String MORE_FRAGMENT_TAG = "more";
    private final static String ABOUT_FRAGMENT_TAG = "about";
    private final static String SELECTED_TAG = "selected_index";
    private final static int COLLAPSING_TOOLBAR = 0;
    private final static int CATEGORY = 1;
    private final static int FAVORITE = 2;
    private final static int RATE = 3;
    private final static int MORE = 4;
    private final static int SHARE = 5;
    private final static int ABOUT = 6;
    private static int selectedIndex;
    static final String TAG = "MainActivity";
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private AdView adView;
    private static final int REQUEST = 112;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Config.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }

        dbHelper = new DBHelper(MainActivity.this);
        try {
            dbHelper.createDataBase();
            Log.d("Database", "Database created");
        } catch (IOException e) {
            e.printStackTrace();
        }

        loadBannerAd();

        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        drawerLayout = findViewById(R.id.drawer_layout);


        View header = navigationView.getHeaderView(0);
        LinearLayout linearLayout = header.findViewById(R.id.lyt_drawer_info);
        if (Config.ENABLE_NAVIGATION_DRAWER_HEADER_INFO) {
            linearLayout.setVisibility(View.VISIBLE);
        } else {
            linearLayout.setVisibility(View.GONE);
        }

        if (savedInstanceState != null) {
            navigationView.getMenu().getItem(savedInstanceState.getInt(SELECTED_TAG)).setChecked(true);
            return;
        }

        selectedIndex = COLLAPSING_TOOLBAR;

        if (Config.ENABLE_CLASSIC_MODE) {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
                    new FragmentClassic(), COLLAPSING_TOOLBAR_FRAGMENT_TAG).commit();
        } else {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
                    new FragmentExplore(), COLLAPSING_TOOLBAR_FRAGMENT_TAG).commit();
        }

        //requestStoragePermission();
        GDPR.updateConsentStatus(this);

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_TAG, selectedIndex);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.drawer_explore:
                if (!menuItem.isChecked()) {
                    selectedIndex = COLLAPSING_TOOLBAR;
                    menuItem.setChecked(true);
                    if (Config.ENABLE_CLASSIC_MODE) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                new FragmentClassic(), COLLAPSING_TOOLBAR_FRAGMENT_TAG).commit();
                    } else {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                new FragmentExplore(), COLLAPSING_TOOLBAR_FRAGMENT_TAG).commit();
                    }
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;

            case R.id.drawer_favorite:
                if (!menuItem.isChecked()) {
                    selectedIndex = FAVORITE;
                    menuItem.setChecked(true);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new FragmentFavorite(), FAVORITE_FRAGMENT_TAG).commit();
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;

            case R.id.drawer_rate:
                if (!menuItem.isChecked()) {
                    selectedIndex = RATE;
                    menuItem.setChecked(true);

                    final String appName = getPackageName();
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appName)));
                    }
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;

            case R.id.drawer_more:
                if (!menuItem.isChecked()) {
                    selectedIndex = MORE;
                    menuItem.setChecked(true);

                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.play_more_apps))));

                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;

            case R.id.drawer_share:
                if (!menuItem.isChecked()) {
                    selectedIndex = SHARE;
                    menuItem.setChecked(true);

                    Intent sendInt = new Intent(Intent.ACTION_SEND);
                    sendInt.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                    sendInt.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + "\nhttps://play.google.com/store/apps/details?id=" + getPackageName());
                    sendInt.setType("text/plain");
                    startActivity(Intent.createChooser(sendInt, "Share"));

                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;

            case R.id.drawer_settings:
                startActivity(new Intent(getApplicationContext(), ActivitySettings.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;

        }
        return false;
    }


    public void setupNavigationDrawer(Toolbar toolbar) {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

            if (Config.ENABLE_EXIT_DIALOG) {

                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setIcon(R.mipmap.ic_launcher);
                dialog.setTitle(R.string.app_name);
                dialog.setMessage(R.string.dialog_close_msg);
                dialog.setPositiveButton(R.string.dialog_option_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MainActivity.this.finish();
                    }
                });

                dialog.setNegativeButton(R.string.dialog_option_rate_us, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final String appName = getPackageName();
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appName)));
                        }

                        MainActivity.this.finish();
                    }
                });

                dialog.setNeutralButton(R.string.dialog_option_more, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.play_more_apps))));
                        MainActivity.this.finish();
                    }
                });
                dialog.show();

            } else {
                super.onBackPressed();
            }
        }
    }

    private void loadBannerAd() {
        if (Config.ENABLE_ADMOB_BANNER_ADS_MAIN_PAGE) {
            adView = findViewById(R.id.adView);
            adView.loadAd(GDPR.getAdRequest(MainActivity.this));
            adView.setAdListener(new AdListener() {

                @Override
                public void onAdClosed() {
                }

                @Override
                public void onAdFailedToLoad(int error) {
                    adView.setVisibility(View.GONE);
                }

                @Override
                public void onAdLeftApplication() {
                }

                @Override
                public void onAdOpened() {
                }

                @Override
                public void onAdLoaded() {
                    adView.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @TargetApi(16)
    private void requestStoragePermission() {
        Dexter.withActivity(MainActivity.this)
                .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            Log.d("Log", "permission granted");
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Error occurred! " + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.permisson_title);
        builder.setMessage(R.string.permisson_message);
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

}
