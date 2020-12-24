package com.app.allpaper4k.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.app.allpaper4k.Config;
import com.app.allpaper4k.R;
import com.app.allpaper4k.adapters.AdapterWallpaper;
import com.app.allpaper4k.models.Wallpaper;
import com.app.allpaper4k.utilities.Constant;
import com.app.allpaper4k.utilities.DBHelper;
import com.app.allpaper4k.utilities.GDPR;
import com.app.allpaper4k.utilities.ItemOffsetDecoration;
import com.app.allpaper4k.utilities.Tools;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdView;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by khurshidnormurodov on 12/24/20
 * */

public class ActivityCategoryDetail extends AppCompatActivity {

    SwipeRefreshLayout swipeRefreshLayout = null;
    RecyclerView recyclerView;
    RelativeLayout lyt_parent;
    DBHelper dbHelper;
    private String lastId = "0";
    private boolean itShouldLoadMore = true;
    private AdapterWallpaper mAdapter;
    private ArrayList<Wallpaper> arrayList;
    ProgressBar progressBar;
    View lyt_no_item;
    Tools tools;
    String category_id, category_name;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_detail);

        if (Config.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }

        lyt_parent = findViewById(R.id.lyt_parent);
        lyt_no_item = findViewById(R.id.lyt_no_item);

        dbHelper = new DBHelper(ActivityCategoryDetail.this);
        tools = new Tools(ActivityCategoryDetail.this);

        Intent intent = getIntent();
        category_id = intent.getStringExtra("category_id");
        category_name = intent.getStringExtra("category_name");

        setupToolbar();

        loadBannerAd();

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue, R.color.red);
        showRefresh(true);

        progressBar = findViewById(R.id.progressBar);

        arrayList = new ArrayList<>();
        mAdapter = new AdapterWallpaper(ActivityCategoryDetail.this, arrayList);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(ActivityCategoryDetail.this, Config.NUM_OF_COLUMNS));
        recyclerView.setHasFixedSize(true);
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(ActivityCategoryDetail.this, R.dimen.grid_space_wallpaper);
        recyclerView.addItemDecoration(itemDecoration);

        recyclerView.setAdapter(mAdapter);

        firstLoadData();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    if (!recyclerView.canScrollVertically(RecyclerView.FOCUS_DOWN)) {
                        if (itShouldLoadMore) {
                            loadMore();
                        }
                    }
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });

        mAdapter.setOnItemClickListener(new AdapterWallpaper.OnItemClickListener() {
            @Override
            public void onItemClick(View v, final Wallpaper obj, int position) {
                Intent intent = new Intent(getApplicationContext(), ActivityImageSlider.class);
                intent.putExtra("POSITION_ID", position);
                Constant.arrayList.clear();
                Constant.arrayList.addAll(arrayList);
                startActivity(intent);
            }
        });

    }

    private void firstLoadData() {

        if (tools.isNetworkAvailable()) {

            itShouldLoadMore = false;
            dbHelper.resetCategoryDetail(Constant.TABLE_CATEGORY_DETAIL, category_id);

            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, Constant.URL_CATEGORY_DETAIL + "&id=" + category_id + "&offset=0", null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {

                    showRefresh(false);
                    itShouldLoadMore = true;

                    if (response.length() <= 0) {
                        lyt_no_item.setVisibility(View.VISIBLE);
                        return;
                    }

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject jsonObject = response.getJSONObject(i);

                            lastId = jsonObject.getString(Constant.NO);
                            String image_id = jsonObject.getString(Constant.IMAGE_ID);
                            String image_upload = jsonObject.getString(Constant.IMAGE_UPLOAD);
                            String image_url = jsonObject.getString(Constant.IMAGE_URL);
                            String type = jsonObject.getString(Constant.TYPE);
                            int view_count = jsonObject.getInt(Constant.VIEW_COUNT);
                            int download_count = jsonObject.getInt(Constant.DOWNLOAD_COUNT);
                            String featured = jsonObject.getString(Constant.FEATURED);
                            String tags = jsonObject.getString(Constant.TAGS);
                            String category_id = jsonObject.getString(Constant.CATEGORY_ID);
                            String category_name = jsonObject.getString(Constant.CATEGORY_NAME);

                            arrayList.add(new Wallpaper(image_id, image_upload, image_url, type, view_count, download_count, featured, tags, category_id, category_name));
                            dbHelper.saveWallpaper(arrayList.get(i), Constant.TABLE_CATEGORY_DETAIL);
                            mAdapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    itShouldLoadMore = true;
                    showRefresh(false);
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.msg_network_error), Toast.LENGTH_SHORT).show();
                }
            });

            MyApplication.getInstance().addToRequestQueue(jsonArrayRequest);

        } else {
            showRefresh(false);
            arrayList = dbHelper.getCategoryDetail(category_id, Constant.TABLE_CATEGORY_DETAIL);
            mAdapter = new AdapterWallpaper(ActivityCategoryDetail.this, arrayList);
            recyclerView.setAdapter(mAdapter);
        }

    }

    private void loadMore() {

        itShouldLoadMore = false;
        progressBar.setVisibility(View.VISIBLE);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, Constant.URL_CATEGORY_DETAIL + "&id=" + category_id + "&offset=" + lastId, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(final JSONArray response) {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        showRefresh(false);
                        progressBar.setVisibility(View.GONE);
                        itShouldLoadMore = true;

                        if (response.length() <= 0) {
                            return;
                        }

                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject jsonObject = response.getJSONObject(i);

                                lastId = jsonObject.getString(Constant.NO);
                                String image_id = jsonObject.getString(Constant.IMAGE_ID);
                                String image_upload = jsonObject.getString(Constant.IMAGE_UPLOAD);
                                String image_url = jsonObject.getString(Constant.IMAGE_URL);
                                String type = jsonObject.getString(Constant.TYPE);
                                int view_count = jsonObject.getInt(Constant.VIEW_COUNT);
                                int download_count = jsonObject.getInt(Constant.DOWNLOAD_COUNT);
                                String featured = jsonObject.getString(Constant.FEATURED);
                                String tags = jsonObject.getString(Constant.TAGS);
                                String category_id = jsonObject.getString(Constant.CATEGORY_ID);
                                String category_name = jsonObject.getString(Constant.CATEGORY_NAME);

                                arrayList.add(new Wallpaper(image_id, image_upload, image_url, type, view_count, download_count, featured, tags, category_id, category_name));
                                mAdapter.notifyDataSetChanged();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }, Constant.DELAY_LOAD_MORE);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                showRefresh(false);
                itShouldLoadMore = true;
                isOffline();
            }
        });

        MyApplication.getInstance().addToRequestQueue(jsonArrayRequest);

    }

    private void refreshData() {
        if (tools.isNetworkAvailable()) {
            lyt_no_item.setVisibility(View.GONE);
            arrayList.clear();
            mAdapter.notifyDataSetChanged();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    firstLoadData();
                }
            }, Constant.DELAY_REFRESH);
        } else {
            showRefresh(false);
            isOffline();
        }
    }

    private void isOffline() {
        Snackbar snackBar = Snackbar.make(lyt_parent, getResources().getString(R.string.msg_offline), Snackbar.LENGTH_LONG);
        snackBar.setAction(getResources().getString(R.string.option_retry), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRefresh(true);
                refreshData();
            }
        });
        snackBar.show();
    }

    private void showRefresh(boolean show) {
        if (show) {
            swipeRefreshLayout.setRefreshing(true);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }, Constant.DELAY_PROGRESS);
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(category_name);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search_wallpaper, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.search:
                Intent intent = new Intent(getApplicationContext(), ActivitySearch.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadBannerAd() {
        if (Config.ENABLE_ADMOB_BANNER_ADS_MAIN_PAGE) {
            adView = findViewById(R.id.adView);
            adView.loadAd(GDPR.getAdRequest(ActivityCategoryDetail.this));
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
