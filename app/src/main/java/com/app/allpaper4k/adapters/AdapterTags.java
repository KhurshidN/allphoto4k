package com.app.allpaper4k.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.app.allpaper4k.R;
import com.app.allpaper4k.activities.ActivitySearch;

import java.util.ArrayList;

/**
 * Created by khurshidnormurodov on 12/24/20
 * */

public class AdapterTags extends RecyclerView.Adapter<AdapterTags.ViewHolder> {

    private ArrayList<String> arrayList;
    Context context;

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txt_tags;

        public ViewHolder(View view) {
            super(view);
            txt_tags = view.findViewById(R.id.item_tags);
        }

    }

    public AdapterTags(Context context, ArrayList<String> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.lsv_item_tag, parent, false);
        return new AdapterTags.ViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        ((ViewHolder) holder).txt_tags.setText(arrayList.get(position).toLowerCase());

        ((ViewHolder) holder).txt_tags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ActivitySearch.class);
                intent.putExtra("tags", arrayList.get(position).toLowerCase());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

}