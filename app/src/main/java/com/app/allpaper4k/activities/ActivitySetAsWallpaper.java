package com.app.allpaper4k.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.app.allpaper4k.Config;
import com.app.allpaper4k.R;
import com.app.allpaper4k.utilities.Constant;
import com.app.allpaper4k.utilities.GDPR;
import com.balysv.materialripple.MaterialRippleLayout;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;

/**
 * Created by khurshidnormurodov on 12/24/20
 * */

public class ActivitySetAsWallpaper extends AppCompatActivity {

    private CropImageView mCropImageView;
    String str_image;
    Toolbar toolbar;
    Bitmap bitmap = null;
    private InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_as);

        if (Config.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }

        initAds();
        loadInterstitialAd();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            toolbar.setPadding(0, getStatusBarHeight(), 0, 0);
        }

        Intent i = getIntent();
        str_image = i.getStringExtra("WALLPAPER_IMAGE_URL");

        mCropImageView = (CropImageView) findViewById(R.id.CropImageView);

        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getApplicationContext()));
        ImageLoader.getInstance().loadImage(str_image, new ImageLoadingListener() {

            @Override
            public void onLoadingStarted(String arg0, View arg1) {
            }

            @Override
            public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
            }

            @Override
            public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
                mCropImageView.setImageBitmap(arg2);
            }

            @Override
            public void onLoadingCancelled(String arg0, View arg1) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.menu_set:
                if (Build.VERSION.SDK_INT >= 24) {
                    dialogSetWallpaperOption();
                } else {
                    dialogSetWallpaper();
                }
                break;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }


    public void dialogSetWallpaperOption() {

        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(ActivitySetAsWallpaper.this);
        View view = layoutInflaterAndroid.inflate(R.layout.dialog_set_wallpaper, null);
        final AlertDialog.Builder alert = new AlertDialog.Builder(ActivitySetAsWallpaper.this);
        alert.setView(view);

        final MaterialRippleLayout btn_set_home_screen = view.findViewById(R.id.menu_set_home_screen);
        final MaterialRippleLayout btn_set_lock_screen = view.findViewById(R.id.menu_set_lock_screen);
        final MaterialRippleLayout btn_set_both = view.findViewById(R.id.menu_set_both);
        final LinearLayout lyt_option = view.findViewById(R.id.lyt_option);
        final LinearLayout lyt_progress = view.findViewById(R.id.lyt_progress);

        final AlertDialog alertDialog = alert.create();

        btn_set_home_screen.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {

                alertDialog.setCancelable(false);
                lyt_option.setVisibility(View.GONE);
                lyt_progress.setVisibility(View.VISIBLE);

                bitmap = mCropImageView.getCroppedImage();
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        alertDialog.dismiss();
                        lyt_progress.setVisibility(View.GONE);
                        showInterstitialAd();
                    }
                }, Constant.DELAY_SET_WALLPAPER);

                try {
                    wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM);
                } catch (IOException e) {
                    e.printStackTrace();
                    alertDialog.dismiss();
                    lyt_progress.setVisibility(View.GONE);
                    Toast.makeText(ActivitySetAsWallpaper.this, R.string.msg_failed, Toast.LENGTH_SHORT).show();
                }

            }
        });

        btn_set_lock_screen.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {

                alertDialog.setCancelable(false);
                lyt_option.setVisibility(View.GONE);
                lyt_progress.setVisibility(View.VISIBLE);

                bitmap = mCropImageView.getCroppedImage();
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        alertDialog.dismiss();
                        lyt_progress.setVisibility(View.GONE);
                        showInterstitialAd();
                    }
                }, Constant.DELAY_SET_WALLPAPER);

                try {
                    wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);
                } catch (IOException e) {
                    e.printStackTrace();
                    alertDialog.dismiss();
                    lyt_progress.setVisibility(View.GONE);
                    Toast.makeText(ActivitySetAsWallpaper.this, R.string.msg_failed, Toast.LENGTH_SHORT).show();
                }

            }
        });

        btn_set_both.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.setCancelable(false);
                lyt_option.setVisibility(View.GONE);
                lyt_progress.setVisibility(View.VISIBLE);

                bitmap = mCropImageView.getCroppedImage();
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        alertDialog.dismiss();
                        lyt_progress.setVisibility(View.GONE);
                        showInterstitialAd();
                    }
                }, Constant.DELAY_SET_WALLPAPER);

                try {
                    wallpaperManager.setBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    alertDialog.dismiss();
                    lyt_progress.setVisibility(View.GONE);
                    Toast.makeText(ActivitySetAsWallpaper.this, R.string.msg_failed, Toast.LENGTH_SHORT).show();
                }

            }
        });

        alertDialog.show();
    }

    public void dialogSetWallpaper() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivitySetAsWallpaper.this);
        builder.setMessage(R.string.msg_title_set_wallpaper)
                .setCancelable(true)
                .setPositiveButton(getResources().getString(R.string.option_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            bitmap = mCropImageView.getCroppedImage();
                            WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
                            wallpaperManager.setBitmap(bitmap);

                            final ProgressDialog progressDialog = new ProgressDialog(ActivitySetAsWallpaper.this);
                            progressDialog.setMessage(getResources().getString(R.string.msg_apply_wallpaper));
                            progressDialog.show();

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.dismiss();
                                    showInterstitialAd();
                                }
                            }, Constant.DELAY_SET_WALLPAPER);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(ActivitySetAsWallpaper.this, R.string.msg_failed, Toast.LENGTH_SHORT).show();
                            Log.v("ERROR", "Wallpaper not set");
                        }
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void initAds() {
        if (Config.ENABLE_ADMOB_INTERSTITIAL_ADS) {
            MobileAds.initialize(ActivitySetAsWallpaper.this, getResources().getString(R.string.admob_app_id));
        }
    }

    private void loadInterstitialAd() {
        if (Config.ENABLE_ADMOB_INTERSTITIAL_ADS) {
            interstitialAd = new InterstitialAd(getApplicationContext());
            interstitialAd.setAdUnitId(getResources().getString(R.string.admob_interstitial_unit_id));
            interstitialAd.loadAd(GDPR.getAdRequest(ActivitySetAsWallpaper.this));
            interstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    closeApp();
                }
            });
        }
    }

    private void showInterstitialAd() {
        if (Config.ENABLE_ADMOB_INTERSTITIAL_ADS) {
            if (interstitialAd != null && interstitialAd.isLoaded()) {
                interstitialAd.show();
            } else {
                closeApp();
            }
        } else {
            closeApp();
        }
    }

    private void closeApp() {
        Intent intent = new Intent(ActivitySetAsWallpaper.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        Toast.makeText(ActivitySetAsWallpaper.this, R.string.msg_success, Toast.LENGTH_SHORT).show();
    }

}