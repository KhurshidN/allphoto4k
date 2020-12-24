package com.app.allpaper4k.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
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
import com.app.allpaper4k.activities.ActivityCategoryDetail;
import com.app.allpaper4k.activities.MyApplication;
import com.app.allpaper4k.adapters.AdapterCategory;
import com.app.allpaper4k.models.Category;
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

public class FragmentCategory extends Fragment {

    SwipeRefreshLayout swipeRefreshLayout = null;
    RecyclerView recyclerView;
    RelativeLayout lyt_parent;
    DBHelper dbHelper;
    private AdapterCategory mAdapter;
    private ArrayList<Category> arrayList;
    ProgressBar progressBar;
    private SearchView searchView;
    private InterstitialAd interstitialAd;
    int counter = 1;
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

        recyclerView = view.findViewById(R.id.recyclerView);
        int padding = getResources().getDimensionPixelOffset(R.dimen.grid_space_wallpaper);
        recyclerView.setPadding(padding, padding, padding, padding);

        progressBar = view.findViewById(R.id.progressBar);

        arrayList = new ArrayList<>();
        mAdapter = new AdapterCategory(getActivity(), arrayList);

        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        recyclerView.setHasFixedSize(true);
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(getActivity(), R.dimen.grid_space_wallpaper);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setAdapter(mAdapter);

        recyclerView.setAdapter(mAdapter);

        firstLoadData();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });

        mAdapter.setOnItemClickListener(new AdapterCategory.OnItemClickListener() {
            @Override
            public void onItemClick(View v, final Category obj, int position) {
                Intent intent = new Intent(getActivity(), ActivityCategoryDetail.class);
                intent.putExtra("category_id", obj.getCategory_id());
                intent.putExtra("category_name", obj.getCategory_name());
                startActivity(intent);

                showInterstitialAd();
            }
        });

        return view;
    }

    private void firstLoadData() {

        if (tools.isNetworkAvailable()) {

            dbHelper.deleteData(Constant.TABLE_CATEGORY);

            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, Constant.URL_CATEGORY, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {

                    showRefresh(false);

                    if (response.length() <= 0) {
                        lyt_no_item.setVisibility(View.VISIBLE);
                        return;
                    }

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject jsonObject = response.getJSONObject(i);

                            String category_id = jsonObject.getString(Constant.CATEGORY_ID);
                            String category_name = jsonObject.getString(Constant.CATEGORY_NAME);
                            String category_image = jsonObject.getString(Constant.CATEGORY_IMAGE);
                            String total_wallpaper = jsonObject.getString(Constant.TOTAL_WALLPAPER);

                            arrayList.add(new Category(category_id, category_name, category_image, total_wallpaper));
                            dbHelper.addtoCategory(arrayList.get(i), Constant.TABLE_CATEGORY);
                            mAdapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    showRefresh(false);
                    Toast.makeText(getActivity(), getResources().getString(R.string.msg_network_error), Toast.LENGTH_SHORT).show();
                }
            });

            MyApplication.getInstance().addToRequestQueue(jsonArrayRequest);

        } else {
            showRefresh(false);
            arrayList = dbHelper.getAllDataCategory(Constant.TABLE_CATEGORY);
            mAdapter = new AdapterCategory(getActivity(), arrayList);
            recyclerView.setAdapter(mAdapter);
        }

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
        inflater.inflate(R.menu.menu_search_category, menu);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                mAdapter.getFilter().filter(query);
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
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
