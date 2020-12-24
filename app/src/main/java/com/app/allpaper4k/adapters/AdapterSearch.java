package com.app.allpaper4k.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.app.allpaper4k.R;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by khurshidnormurodov on 12/24/20
 * */

public class AdapterSearch extends RecyclerView.Adapter<AdapterSearch.ViewHolder> {

    private static final String SEARCH_HISTORY_KEY = "_SEARCH_HISTORY_KEY";
    private static final int MAX_HISTORY_ITEMS = 0;

    private List<String> items = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    private SharedPreferences prefs;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public LinearLayout lyt_parent;

        public ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.title);
            lyt_parent = v.findViewById(R.id.lyt_parent);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public AdapterSearch(Context context) {
        prefs = context.getSharedPreferences("PREF_RECENT_SEARCH", Context.MODE_PRIVATE);
        this.items = getSearchHistory();
        Collections.reverse(this.items);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lsv_item_suggestion, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final String p = items.get(position);
        final int pos = position;
        holder.title.setText(p);
        holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                onItemClickListener.onItemClick(v, p, pos);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, String viewModel, int pos);
    }

    public void refreshItems() {
        this.items = getSearchHistory();
        Collections.reverse(this.items);
        notifyDataSetChanged();
    }

    private class SearchObject implements Serializable {
        public SearchObject(List<String> items) {
            this.items = items;
        }

        public List<String> items = new ArrayList<>();
    }

    /**
     * To save last state request
     */
    public void addSearchHistory(String s) {
        SearchObject searchObject = new SearchObject(getSearchHistory());
        if (searchObject.items.contains(s)) searchObject.items.remove(s);
        searchObject.items.add(s);
        if (searchObject.items.size() > MAX_HISTORY_ITEMS) searchObject.items.remove(0);
        String json = new Gson().toJson(searchObject, SearchObject.class);
        prefs.edit().putString(SEARCH_HISTORY_KEY, json).apply();
    }

    private List<String> getSearchHistory() {
        String json = prefs.getString(SEARCH_HISTORY_KEY, "");
        if (json.equals("")) return new ArrayList<>();
        SearchObject searchObject = new Gson().fromJson(json, SearchObject.class);
        return searchObject.items;
    }
}