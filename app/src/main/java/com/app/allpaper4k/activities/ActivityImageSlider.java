package com.app.allpaper4k.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;

import com.app.allpaper4k.Config;
import com.app.allpaper4k.R;
import com.app.allpaper4k.adapters.AdapterTags;
import com.app.allpaper4k.models.Wallpaper;
import com.app.allpaper4k.utilities.Constant;
import com.app.allpaper4k.utilities.DBHelper;
import com.app.allpaper4k.utilities.GDPR;
import com.app.allpaper4k.utilities.Tools;
import com.balysv.materialripple.MaterialRippleLayout;
import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager;
import com.beloo.widget.chipslayoutmanager.SpacingItemDecoration;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by khurshidnormurodov on 12/24/20
 * */

public class ActivityImageSlider extends AppCompatActivity implements SensorEventListener {

    DBHelper dbHelper;
    int position;
    ViewPager viewpager;
    int TOTAL_IMAGE;
    private SensorManager sensorManager;
    private boolean checkImage = false;
    private long lastUpdate;
    Handler handler;
    Runnable Update;
    String image_id;
    private BottomSheetBehavior bottomSheetBehavior;
    TextView txt_category_name, txt_resolution, txt_view_count, txt_download_count;
    ImageView img_thumb;
    ImageButton btn_favorite, btn_download, btn_share, btn_set;
    Toolbar toolbar;
    ProgressDialog progressDialog;
    private InterstitialAd interstitialAd;
    private AdView adView;
    LinearLayout lyt_action;
    RecyclerView recyclerView_tags;
    AdapterTags adapterTags;
    ArrayList<String> arrayListTags;
    LinearLayout lyt_tags;
    Tools tools;

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_slider);

        if (Config.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }

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

        lyt_action = findViewById(R.id.lyt_action);

        dbHelper = new DBHelper(ActivityImageSlider.this);
        tools = new Tools(ActivityImageSlider.this);

        initAds();
        loadBannerAd();
        loadInterstitialAd();

        btn_favorite = findViewById(R.id.btn_favorite);
        btn_download = findViewById(R.id.btn_download);
        btn_share = findViewById(R.id.btn_share);
        btn_set = findViewById(R.id.btn_set);

        txt_category_name = findViewById(R.id.category_name);
        txt_resolution = findViewById(R.id.txt_resolution);
        txt_view_count = findViewById(R.id.txt_view_count);
        txt_download_count = findViewById(R.id.txt_download_count);
        img_thumb = findViewById(R.id.img_thumb);

        Intent i = getIntent();
        position = i.getIntExtra("POSITION_ID", 0);

        recyclerView_tags = findViewById(R.id.recyclerViewTags);
        lyt_tags = findViewById(R.id.lyt_tags);

        ChipsLayoutManager spanLayoutManager = ChipsLayoutManager.newBuilder(getApplicationContext()).setOrientation(ChipsLayoutManager.HORIZONTAL).build();
        recyclerView_tags.addItemDecoration(new SpacingItemDecoration(getResources().getDimensionPixelOffset(R.dimen.chips_space), getResources().getDimensionPixelOffset(R.dimen.chips_space)));
        recyclerView_tags.setLayoutManager(spanLayoutManager);

        loadViewed(position);

        TOTAL_IMAGE = Constant.arrayList.size() - 1;
        viewpager = (ViewPager) findViewById(R.id.image_slider);
        handler = new Handler();

        ImagePagerAdapter adapter = new ImagePagerAdapter();
        viewpager.setAdapter(adapter);
        viewpager.setCurrentItem(position);

        if (tools.isNetworkAvailable()) {
            new MyTask().execute(Constant.URL_VIEW_COUNT + Constant.arrayList.get(position).getImage_id());
        }

        arrayListTags = new ArrayList(Arrays.asList(((Wallpaper) Constant.arrayList.get(position)).getTags().split(",")));

        adapterTags = new AdapterTags(ActivityImageSlider.this, arrayListTags);
        recyclerView_tags.setAdapter(adapterTags);

        if (Constant.arrayList.get(position).getTags().equals("")) {
            lyt_tags.setVisibility(View.GONE);
        } else {
            lyt_tags.setVisibility(View.VISIBLE);
        }

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lastUpdate = System.currentTimeMillis();

        FirstFav();

        viewpager.addOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {

                position = viewpager.getCurrentItem();
                ArrayList<Wallpaper> itemPhotos = dbHelper.getFavRow(Constant.arrayList.get(position).getImage_id(), Constant.TABLE_FAVORITE);
                if (itemPhotos.size() == 0) {
                    btn_favorite.setImageResource(R.drawable.ic_star_outline);
                } else {
                    btn_favorite.setImageResource(R.drawable.ic_star_white);
                }
                loadViewed(position);

                if (tools.isNetworkAvailable()) {
                    new MyTask().execute(Constant.URL_VIEW_COUNT + Constant.arrayList.get(position).getImage_id());
                }

                arrayListTags = new ArrayList(Arrays.asList(((Wallpaper) Constant.arrayList.get(position)).getTags().split(",")));
                adapterTags = new AdapterTags(ActivityImageSlider.this, arrayListTags);
                recyclerView_tags.setAdapter(adapterTags);

                if (Constant.arrayList.get(position).getTags().equals("")) {
                    lyt_tags.setVisibility(View.GONE);
                } else {
                    lyt_tags.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int position) {

            }

            @Override
            public void onPageScrollStateChanged(int position) {

            }
        });

        initBottomSheet();

    }

    private void initBottomSheet() {

        RelativeLayout relativeLayout = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(relativeLayout);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {

                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {

                } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {

                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        ((RelativeLayout) findViewById(R.id.lyt_expand)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

    }

    public void AddtoFav(int position) {
        dbHelper.saveWallpaper(Constant.arrayList.get(position), Constant.TABLE_FAVORITE);
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.favorite_added), Toast.LENGTH_SHORT).show();
        btn_favorite.setImageResource(R.drawable.ic_star_white);
    }

    public void RemoveFav(int position) {
        dbHelper.removeFav(Constant.arrayList.get(position).getImage_id());
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.favorite_removed), Toast.LENGTH_SHORT).show();
        btn_favorite.setImageResource(R.drawable.ic_star_outline);
    }

    public void FirstFav() {
        int first = viewpager.getCurrentItem();
        //String Image_id = Constant.arrayList.get(first).getWallpaper_image();

        ArrayList<Wallpaper> itemPhotos = dbHelper.getFavRow(Constant.arrayList.get(first).getImage_id(), Constant.TABLE_FAVORITE);
        if (itemPhotos.size() == 0) {
            btn_favorite.setImageResource(R.drawable.ic_star_outline);
        } else {
            btn_favorite.setImageResource(R.drawable.ic_star_white);
        }
    }

    private class ImagePagerAdapter extends PagerAdapter {

        private LayoutInflater inflater;

        public ImagePagerAdapter() {
            inflater = getLayoutInflater();
        }

        @Override
        public int getCount() {
            return Constant.arrayList.size();

        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {

            if (Config.ENABLE_CENTER_CROP_IN_DETAIL_WALLPAPER) {

                View imageLayout = inflater.inflate(R.layout.view_pager_item_crop, container, false);
                assert imageLayout != null;
                final PhotoView imageView = imageLayout.findViewById(R.id.image);
                final ProgressBar spinner = imageLayout.findViewById(R.id.loading);

                if (Constant.arrayList.get(position).getType().equals("url")) {
                    Picasso.with(ActivityImageSlider.this)
                            .load(Constant.arrayList.get(position).getImage_url().replace(" ", "%20"))
                            .placeholder(R.drawable.ic_transparent)
                            .into(imageView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    spinner.setVisibility(View.GONE);
                                }

                                @Override
                                public void onError() {
                                    spinner.setVisibility(View.GONE);
                                }
                            });
                } else {
                    Picasso.with(ActivityImageSlider.this)
                            .load(Config.ADMIN_PANEL_URL + "/upload/" + Constant.arrayList.get(position).getImage_upload().replace(" ", "%20"))
                            .placeholder(R.drawable.ic_transparent)
                            .into(imageView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    spinner.setVisibility(View.GONE);
                                }

                                @Override
                                public void onError() {
                                    spinner.setVisibility(View.GONE);
                                }
                            });
                }

                container.addView(imageLayout, 0);
                return imageLayout;

            } else {

                View imageLayout = inflater.inflate(R.layout.view_pager_item, container, false);
                assert imageLayout != null;
                final PhotoView imageView = imageLayout.findViewById(R.id.image);
                final ProgressBar spinner = imageLayout.findViewById(R.id.loading);

                if (Constant.arrayList.get(position).getType().equals("url")) {
                    Picasso.with(ActivityImageSlider.this)
                            .load(Constant.arrayList.get(position).getImage_url().replace(" ", "%20"))
                            .placeholder(R.drawable.ic_transparent)
                            .into(imageView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    spinner.setVisibility(View.GONE);
                                }

                                @Override
                                public void onError() {
                                    spinner.setVisibility(View.GONE);
                                }
                            });
                } else {
                    Picasso.with(ActivityImageSlider.this)
                            .load(Config.ADMIN_PANEL_URL + "/upload/" + Constant.arrayList.get(position).getImage_upload().replace(" ", "%20"))
                            .placeholder(R.drawable.ic_transparent)
                            .into(imageView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    spinner.setVisibility(View.GONE);
                                }

                                @Override
                                public void onError() {
                                    spinner.setVisibility(View.GONE);
                                }
                            });
                }

                container.addView(imageLayout, 0);
                return imageLayout;

            }

        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((View) object);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event);
        }
    }

    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;

        float x = values[0];
        float y = values[1];
        float z = values[2];

        float accelerationSquareRoot = (x * x + y * y + z * z)
                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        long actualTime = System.currentTimeMillis();
        if (accelerationSquareRoot >= 2) {
            if (actualTime - lastUpdate < 200) {
                return;
            }
            lastUpdate = actualTime;
            if (checkImage) {
                position = viewpager.getCurrentItem();
                viewpager.setCurrentItem(position);
            } else {
                position = viewpager.getCurrentItem();
                position++;
                if (position == TOTAL_IMAGE) {
                    position = TOTAL_IMAGE;
                }
                viewpager.setCurrentItem(position);
            }
            checkImage = !checkImage;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadViewed(position);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(Update);
        sensorManager.unregisterListener(this);
    }

    public class ShareTask extends AsyncTask<String, String, String> {
        private Context context;
        private ProgressDialog pDialog;
        URL myFileUrl;
        Bitmap bmImg = null;
        File file;

        public ShareTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(context);
            pDialog.setMessage(getResources().getString(R.string.msg_please_wait));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected String doInBackground(String... args) {

            try {

                myFileUrl = new URL(args[0]);
                HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
                conn.setDoInput(true);
                conn.connect();
                InputStream is = conn.getInputStream();
                bmImg = BitmapFactory.decodeStream(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {

                String path = myFileUrl.getPath();
                String idStr = path.substring(path.lastIndexOf('/') + 1);
                File filepath = Environment.getExternalStorageDirectory();
                File dir = new File(filepath.getAbsolutePath() + "/" + getResources().getString(R.string.saved_folder_name) + "/");
                dir.mkdirs();
                String fileName = idStr;
                file = new File(dir, fileName);
                FileOutputStream fos = new FileOutputStream(file);
                bmImg.compress(CompressFormat.JPEG, 75, fos);
                fos.flush();
                fos.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String args) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                    StrictMode.setVmPolicy(builder.build());
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("image/*");
                    share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file.getAbsolutePath()));
                    startActivity(Intent.createChooser(share, "Share Image"));
                    pDialog.dismiss();
                }
            }, Constant.DELAY_SET_WALLPAPER);

        }
    }

    public class SetWallpaperFromOtherApp extends AsyncTask<String, String, String> {

        private Context context;
        private ProgressDialog pDialog;
        URL myFileUrl;
        Bitmap bmImg = null;
        File file;

        public SetWallpaperFromOtherApp(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(context);
            pDialog.setMessage(getResources().getString(R.string.msg_please_wait));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected String doInBackground(String... args) {

            try {
                myFileUrl = new URL(args[0]);
                HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
                conn.setDoInput(true);
                conn.connect();
                InputStream is = conn.getInputStream();
                bmImg = BitmapFactory.decodeStream(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                String path = myFileUrl.getPath();
                String idStr = path.substring(path.lastIndexOf('/') + 1);
                File filepath = Environment.getExternalStorageDirectory();
                File dir = new File(filepath.getAbsolutePath() + "/" + getResources().getString(R.string.saved_folder_name) + "/");
                dir.mkdirs();
                String fileName = idStr;
                file = new File(dir, fileName);
                FileOutputStream fos = new FileOutputStream(file);
                bmImg.compress(CompressFormat.JPEG, 99, fos);
                fos.flush();
                fos.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String args) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                    StrictMode.setVmPolicy(builder.build());
                    Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setDataAndType(Uri.parse("file://" + file.getAbsolutePath()), "image/jpeg");
                    intent.putExtra("mimeType", "image/jpeg");
                    startActivity(Intent.createChooser(intent, "Set as:"));
                    pDialog.dismiss();
                }
            }, Constant.DELAY_SET_WALLPAPER);
        }
    }

    private void loadViewed(final int position) {

        btn_favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityImageSlider.this.position = viewpager.getCurrentItem();
                image_id = Constant.arrayList.get(ActivityImageSlider.this.position).getImage_id();
                ArrayList<Wallpaper> itemPhotos = dbHelper.getFavRow(image_id, Constant.TABLE_FAVORITE);
                if (itemPhotos.size() == 0) {
                    AddtoFav(ActivityImageSlider.this.position);
                } else {
                    if (itemPhotos.get(0).getImage_id().equals(image_id)) {
                        RemoveFav(ActivityImageSlider.this.position);
                    }
                }
            }
        });

        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tools.isNetworkAvailable()) {
                    sharePermission(position);
                } else {
                    Toast.makeText(ActivityImageSlider.this, R.string.alert_share, Toast.LENGTH_SHORT).show();
                }
            }
        });

        txt_category_name.setText(Constant.arrayList.get(position).getCategory_name());
        txt_view_count.setText(Tools.withSuffix(Constant.arrayList.get(position).getView_count()) + "");
        txt_download_count.setText(Tools.withSuffix(Constant.arrayList.get(position).getDownload_count()) + "");

        if (Constant.arrayList.get(position).getType().equals("url")) {
            Picasso.with(ActivityImageSlider.this)
                    .load(Constant.arrayList.get(position).getImage_url().replace(" ", "%20"))
                    .placeholder(R.drawable.ic_transparent)
                    .into(img_thumb, new Callback() {
                        @Override
                        public void onSuccess() {

                            Bitmap resolution = ((BitmapDrawable) img_thumb.getDrawable()).getBitmap();
                            int width = resolution.getWidth();
                            int height = resolution.getHeight();
                            txt_resolution.setText(width + " x " + height);

                            btn_set.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    if (Config.ENABLE_SET_WALLPAPER_WITH_OTHER_APPS) {
                                        setWallpaperPermission(position);
                                    } else {
                                        PopupMenu popup = new PopupMenu(v.getContext(), v);
                                        popup.inflate(R.menu.menu_popup);
                                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                            public boolean onMenuItemClick(MenuItem item) {
                                                final int itemId = item.getItemId();
                                                switch (itemId) {
                                                    case R.id.option_apply_now:
                                                        if (Build.VERSION.SDK_INT >= 24) {
                                                            dialogSetWallpaperOption(position);
                                                        } else {
                                                            dialogSetWallpaper();
                                                        }
                                                        return true;
                                                    case R.id.option_crop_wallpaper:
                                                        Intent intent = new Intent(getApplicationContext(), ActivitySetAsWallpaper.class);
                                                        intent.putExtra("WALLPAPER_IMAGE_URL", Constant.arrayList.get(position).getImage_url());
                                                        startActivity(intent);
                                                        return true;
                                                    default:
                                                        return false;
                                                }
                                            }
                                        });
                                        Object menuHelper;
                                        Class[] argTypes;
                                        try {
                                            Field fMenuHelper = PopupMenu.class.getDeclaredField("mPopup");
                                            fMenuHelper.setAccessible(true);
                                            menuHelper = fMenuHelper.get(popup);
                                            argTypes = new Class[]{boolean.class};
                                            menuHelper.getClass().getDeclaredMethod("setForceShowIcon", argTypes).invoke(menuHelper, true);
                                        } catch (Exception e) {

                                        }
                                        popup.show();
                                    }

                                }
                            });

                            btn_download.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (tools.isNetworkAvailable()) {

                                        AlertDialog.Builder dialog = new AlertDialog.Builder(ActivityImageSlider.this);
                                        dialog.setMessage(R.string.msg_confirm_download);
                                        dialog.setPositiveButton(R.string.dialog_option_yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Tools.download(ActivityImageSlider.this, Constant.arrayList.get(position).getImage_url());
                                                new MyTask().execute(Constant.URL_DOWNLOAD_COUNT + Constant.arrayList.get(position).getImage_id());
                                            }
                                        });
                                        dialog.setNegativeButton(R.string.dialog_option_cancel, null);
                                        dialog.show();

                                    } else {
                                        Toast.makeText(ActivityImageSlider.this, R.string.alert_download, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }

                        @Override
                        public void onError() {
                        }
                    });
        } else {
            Picasso.with(ActivityImageSlider.this)
                    .load(Config.ADMIN_PANEL_URL + "/upload/" + Constant.arrayList.get(position).getImage_upload().replace(" ", "%20"))
                    .placeholder(R.drawable.ic_transparent)
                    .into(img_thumb, new Callback() {
                        @Override
                        public void onSuccess() {

                            Bitmap resolution = ((BitmapDrawable) img_thumb.getDrawable()).getBitmap();
                            int width = resolution.getWidth();
                            int height = resolution.getHeight();
                            txt_resolution.setText(width + " x " + height);

                            btn_set.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    if (Config.ENABLE_SET_WALLPAPER_WITH_OTHER_APPS) {
                                        setWallpaperPermission(position);
                                    } else {
                                        PopupMenu popup = new PopupMenu(v.getContext(), v);
                                        popup.inflate(R.menu.menu_popup);
                                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                            public boolean onMenuItemClick(MenuItem item) {
                                                final int itemId = item.getItemId();
                                                switch (itemId) {
                                                    case R.id.option_apply_now:
                                                        if (Build.VERSION.SDK_INT >= 24) {
                                                            dialogSetWallpaperOption(position);
                                                        } else {
                                                            dialogSetWallpaper();
                                                        }
                                                        return true;
                                                    case R.id.option_crop_wallpaper:
                                                        Intent intent = new Intent(getApplicationContext(), ActivitySetAsWallpaper.class);
                                                        intent.putExtra("WALLPAPER_IMAGE_URL", Config.ADMIN_PANEL_URL + "/upload/" + Constant.arrayList.get(position).getImage_upload());
                                                        startActivity(intent);
                                                        return true;
                                                    default:
                                                        return false;
                                                }
                                            }
                                        });
                                        Object menuHelper;
                                        Class[] argTypes;
                                        try {
                                            Field fMenuHelper = PopupMenu.class.getDeclaredField("mPopup");
                                            fMenuHelper.setAccessible(true);
                                            menuHelper = fMenuHelper.get(popup);
                                            argTypes = new Class[]{boolean.class};
                                            menuHelper.getClass().getDeclaredMethod("setForceShowIcon", argTypes).invoke(menuHelper, true);
                                        } catch (Exception e) {

                                        }
                                        popup.show();
                                    }

                                }
                            });

                            btn_download.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (tools.isNetworkAvailable()) {

                                        AlertDialog.Builder dialog = new AlertDialog.Builder(ActivityImageSlider.this);
                                        dialog.setMessage(R.string.msg_confirm_download);
                                        dialog.setPositiveButton(R.string.dialog_option_yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Tools.download(ActivityImageSlider.this, Config.ADMIN_PANEL_URL + "/upload/" + Constant.arrayList.get(position).getImage_upload());
                                                new MyTask().execute(Constant.URL_DOWNLOAD_COUNT + Constant.arrayList.get(position).getImage_id());
                                            }
                                        });
                                        dialog.setNegativeButton(R.string.dialog_option_cancel, null);
                                        dialog.show();

                                    } else {
                                        Toast.makeText(ActivityImageSlider.this, R.string.alert_download, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }

                        @Override
                        public void onError() {
                        }
                    });
        }

    }

    private static class MyTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            return Tools.getJSONString(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (null == result || result.length() == 0) {
                Log.d("TAG", "no data found!");
            } else {

                try {

                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray("result");
                    JSONObject objJson = null;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
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

    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            super.onBackPressed();
        }
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

    public void dialogSetWallpaperOption(final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(ActivityImageSlider.this);
        View view = layoutInflaterAndroid.inflate(R.layout.dialog_set_wallpaper, null);
        final AlertDialog.Builder alert = new AlertDialog.Builder(ActivityImageSlider.this);
        alert.setView(view);
        final MaterialRippleLayout btn_set_home_screen = view.findViewById(R.id.menu_set_home_screen);
        final MaterialRippleLayout btn_set_lock_screen = view.findViewById(R.id.menu_set_lock_screen);
        final MaterialRippleLayout btn_set_both = view.findViewById(R.id.menu_set_both);
        final LinearLayout lyt_root = view.findViewById(R.id.custom_dialog_layout_design_user_input);
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

                try {
                    Bitmap bitmap = ((BitmapDrawable) img_thumb.getDrawable()).getBitmap();
                    WallpaperManager wallpaperManager = WallpaperManager.getInstance(ActivityImageSlider.this);
                    wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            alertDialog.dismiss();
                            lyt_progress.setVisibility(View.GONE);
                            showInterstitialAd();
                        }
                    }, Constant.DELAY_SET_WALLPAPER);

                } catch (IOException e) {
                    Tools.printStackTrace(e);
                    alertDialog.dismiss();
                    lyt_progress.setVisibility(View.GONE);
                    Toast.makeText(ActivityImageSlider.this, R.string.msg_failed, Toast.LENGTH_SHORT).show();
                    Log.v("ERROR", "Wallpaper not set");
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

                try {
                    Bitmap bitmap = ((BitmapDrawable) img_thumb.getDrawable()).getBitmap();

                    WallpaperManager wallpaperManager = WallpaperManager.getInstance(ActivityImageSlider.this);
                    wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            alertDialog.dismiss();
                            lyt_progress.setVisibility(View.GONE);
                            showInterstitialAd();
                        }
                    }, Constant.DELAY_SET_WALLPAPER);

                } catch (IOException e) {
                    Tools.printStackTrace(e);
                    alertDialog.dismiss();
                    lyt_progress.setVisibility(View.GONE);
                    Toast.makeText(ActivityImageSlider.this, R.string.msg_failed, Toast.LENGTH_SHORT).show();
                    Log.v("ERROR", "Wallpaper not set");
                }

            }
        });

        btn_set_both.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.setCancelable(false);
                lyt_option.setVisibility(View.GONE);
                lyt_progress.setVisibility(View.VISIBLE);

                try {
                    Bitmap bitmap = ((BitmapDrawable) img_thumb.getDrawable()).getBitmap();
                    WallpaperManager wallpaperManager = WallpaperManager.getInstance(ActivityImageSlider.this);
                    wallpaperManager.setBitmap(bitmap);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            alertDialog.dismiss();
                            lyt_progress.setVisibility(View.GONE);
                            showInterstitialAd();
                        }
                    }, Constant.DELAY_SET_WALLPAPER);

                } catch (IOException e) {
                    Tools.printStackTrace(e);
                    alertDialog.dismiss();
                    lyt_progress.setVisibility(View.GONE);
                    Toast.makeText(ActivityImageSlider.this, R.string.msg_failed, Toast.LENGTH_SHORT).show();
                    Log.v("ERROR", "Wallpaper not set");
                }

            }
        });

        alertDialog.show();
    }

    public void dialogSetWallpaper() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityImageSlider.this);
        builder.setMessage(R.string.msg_title_set_wallpaper)
                .setCancelable(true)
                .setPositiveButton(getResources().getString(R.string.option_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            Bitmap bitmap = ((BitmapDrawable) img_thumb.getDrawable()).getBitmap();
                            WallpaperManager wallpaperManager = WallpaperManager.getInstance(ActivityImageSlider.this);
                            wallpaperManager.setBitmap(bitmap);

                            final ProgressDialog progressDialog = new ProgressDialog(ActivityImageSlider.this);
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
                            Tools.printStackTrace(e);
                            progressDialog.dismiss();
                            Toast.makeText(ActivityImageSlider.this, R.string.msg_failed, Toast.LENGTH_SHORT).show();
                            Log.v("ERROR", "Wallpaper not set");
                        }
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private void sharePermission(final int position) {
        Dexter.withActivity(ActivityImageSlider.this)
                .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            if (Constant.arrayList.get(position).getType().equals("url")) {
                                (new ShareTask(ActivityImageSlider.this)).execute(Constant.arrayList.get(position).getImage_url());
                            } else {
                                (new ShareTask(ActivityImageSlider.this)).execute(Config.ADMIN_PANEL_URL + "/upload/" + Constant.arrayList.get(position).getImage_upload());
                            }
                        }
                        if (report.isAnyPermissionPermanentlyDenied()) {
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

    private void setWallpaperPermission(final int position) {
        Dexter.withActivity(ActivityImageSlider.this)
                .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            if (Constant.arrayList.get(position).getType().equals("url")) {
                                (new SetWallpaperFromOtherApp(ActivityImageSlider.this)).execute(Constant.arrayList.get(position).getImage_url());
                            } else {
                                (new SetWallpaperFromOtherApp(ActivityImageSlider.this)).execute(Config.ADMIN_PANEL_URL + "/upload/" + Constant.arrayList.get(position).getImage_upload());
                            }
                        }
                        if (report.isAnyPermissionPermanentlyDenied()) {
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
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityImageSlider.this);
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

    private void initAds() {
        if (Config.ENABLE_ADMOB_INTERSTITIAL_ADS) {
            MobileAds.initialize(ActivityImageSlider.this, getResources().getString(R.string.admob_app_id));
        }
    }

    private void loadInterstitialAd() {
        if (Config.ENABLE_ADMOB_INTERSTITIAL_ADS) {
            interstitialAd = new InterstitialAd(ActivityImageSlider.this);
            interstitialAd.setAdUnitId(getResources().getString(R.string.admob_interstitial_unit_id));
            interstitialAd.loadAd(GDPR.getAdRequest(ActivityImageSlider.this));
            interstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    ActivityImageSlider.this.finish();
                    Toast.makeText(ActivityImageSlider.this, R.string.msg_success, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showInterstitialAd() {
        if (Config.ENABLE_ADMOB_INTERSTITIAL_ADS) {
            if (interstitialAd != null && interstitialAd.isLoaded()) {
                interstitialAd.show();
            } else {
                Toast.makeText(ActivityImageSlider.this, R.string.msg_success, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadBannerAd() {
        if (Config.ENABLE_ADMOB_BANNER_ADS_WALLPAPER_DETAIL) {
            adView = findViewById(R.id.adView);
            adView.loadAd(GDPR.getAdRequest(ActivityImageSlider.this));
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

}
