package com.app.allpaper4k.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.app.allpaper4k.Config;
import com.app.allpaper4k.R;
import com.app.allpaper4k.activities.ActivityImageSlider;
import com.app.allpaper4k.activities.ActivitySearch;
import com.app.allpaper4k.activities.MyApplication;
import com.app.allpaper4k.adapters.AdapterWallpaper;
import com.app.allpaper4k.models.Wallpaper;
import com.app.allpaper4k.utilities.Constant;
import com.app.allpaper4k.utilities.DBHelper;
import com.app.allpaper4k.utilities.GDPR;
import com.app.allpaper4k.utilities.ItemOffsetDecoration;
import com.app.allpaper4k.utilities.Tools;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by khurshidnormurodov on 12/24/20
 * */

public class FragmentRecent extends Fragment {

    SwipeRefreshLayout swipeRefreshLayout = null;
    RecyclerView recyclerView;
    RelativeLayout lyt_parent;
    DBHelper dbHelper;
    private String lastId = "0";
    private boolean itShouldLoadMore = true;
    private AdapterWallpaper mAdapter;
    private ArrayList<Wallpaper> arrayList;
    private InterstitialAd interstitialAd;
    int counter = 1;
    ProgressBar progressBar;
    View lyt_no_item, view;
    Tools tools;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_wallpaper, container, false);
        lyt_parent = view.findViewById(R.id.lyt_parent);
        lyt_no_item = view.findViewById(R.id.lyt_no_item);

        if (Config.ENABLE_RTL_MODE) {
            lyt_parent.setRotationY(180);
        }

        setHasOptionsMenu(true);

        loadInterstitialAd();

        dbHelper = new DBHelper(getActivity());
        tools = new Tools(getActivity());

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue, R.color.red);
        showRefresh(true);

        progressBar = view.findViewById(R.id.progressBar);

        arrayList = new ArrayList<>();
        mAdapter = new AdapterWallpaper(getActivity(), arrayList);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), Config.NUM_OF_COLUMNS));
        recyclerView.setHasFixedSize(true);
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(getActivity(), R.dimen.grid_space_wallpaper);
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
            public void onItemClick(View v, final Wallpaper obj, final int position) {

                Intent intent = new Intent(getActivity(), ActivityImageSlider.class);
                intent.putExtra("POSITION_ID", position);
                Constant.arrayList.clear();
                Constant.arrayList.addAll(arrayList);
                startActivity(intent);

                showInterstitialAd();
            }
        });

        return view;
    }

    private void firstLoadData() {

        if (tools.isNetworkAvailable()) {

            itShouldLoadMore = false;
            dbHelper.deleteData(Constant.TABLE_RECENT);

            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, Constant.URL_RECENT_WALLPAPER + 0, null, new Response.Listener<JSONArray>() {
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
                            dbHelper.saveWallpaper(arrayList.get(i), Constant.TABLE_RECENT);
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
                    Toast.makeText(getActivity(), getResources().getString(R.string.msg_network_error), Toast.LENGTH_SHORT).show();
                }
            });

            MyApplication.getInstance().addToRequestQueue(jsonArrayRequest);

        } else {
            showRefresh(false);
            arrayList = dbHelper.getAllData(Constant.TABLE_RECENT);
            mAdapter = new AdapterWallpaper(getActivity(), arrayList);
            recyclerView.setAdapter(mAdapter);
        }

    }

    private void loadMore() {

        itShouldLoadMore = false;
        progressBar.setVisibility(View.VISIBLE);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, Constant.URL_RECENT_WALLPAPER + lastId, null, new Response.Listener<JSONArray>() {
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search_wallpaper, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.search) {
            Intent intent = new Intent(getActivity(), ActivitySearch.class);
            startActivity(intent);
            return false;
        }
        return false;
    }

    private void loadInterstitialAd() {
        if (Config.ENABLE_ADMOB_INTERSTITIAL_ADS) {
            interstitialAd = new InterstitialAd(getActivity());
            interstitialAd.setAdUnitId(getResources().getString(R.string.admob_interstitial_unit_id));
            interstitialAd.loadAd(GDPR.getAdRequest(getActivity()));
            interstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    interstitialAd.loadAd(GDPR.getAdRequest(getActivity()));
                }
            });
        }
    }

    private void showInterstitialAd() {
        if (Config.ENABLE_ADMOB_INTERSTITIAL_ADS) {
            if (interstitialAd != null && interstitialAd.isLoaded()) {
                if (counter == Config.INTERSTITIAL_ADS_INTERVAL) {
                    interstitialAd.show();
                    counter = 1;
                } else {
                    counter++;
                }
            }
        }
    }

}
