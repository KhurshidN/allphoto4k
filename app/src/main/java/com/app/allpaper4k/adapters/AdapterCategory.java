package com.app.allpaper4k.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.allpaper4k.Config;
import com.app.allpaper4k.R;
import com.app.allpaper4k.models.Category;
import com.balysv.materialripple.MaterialRippleLayout;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by khurshidnormurodov on 12/24/20
 * */

public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.MyViewHolder> implements Filterable {

    private ArrayList<Category> categories;
    private ArrayList<Category> categoriesFiltered;
    private OnItemClickListener mOnItemClickListener;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(View view, Category obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemOverflowClickListener) {
        this.mOnItemClickListener = mItemOverflowClickListener;
    }

    public AdapterCategory(Context context, ArrayList<Category> categories) {
        this.context = context;
        this.categories = categories;
        this.categoriesFiltered = categories;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView img_category;
        TextView txt_category_name, txt_total_wallpaper;
        private MaterialRippleLayout lyt_parent;

        MyViewHolder(View view) {
            super(view);
            img_category = view.findViewById(R.id.category_image);
            txt_category_name = view.findViewById(R.id.category_name);
            txt_total_wallpaper = view.findViewById(R.id.total_wallpaper);
            lyt_parent = view.findViewById(R.id.lyt_parent);

        }
    }

    @Override
    public AdapterCategory.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.lsv_item_category, parent, false));
    }

    @Override
    public void onBindViewHolder(AdapterCategory.MyViewHolder holder, final int position) {

        Picasso.with(context)
                .load(Config.ADMIN_PANEL_URL + "/upload/category/" + categoriesFiltered.get(position).getCategory_image().replace(" ", "%20"))
                .placeholder(R.drawable.ic_transparent)
                .resizeDimen(R.dimen.category_width, R.dimen.category_height)
                .centerCrop()
                .into(holder.img_category);

        holder.txt_category_name.setText("" + categoriesFiltered.get(position).getCategory_name());
        holder.txt_total_wallpaper.setText("" + categoriesFiltered.get(position).getTotal_wallpaper() + " " + context.getResources().getString(R.string.wallpapers));

        holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, categoriesFiltered.get(position), position);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return categoriesFiltered.size();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    categoriesFiltered = categories;
                } else {
                    ArrayList<Category> filteredList = new ArrayList<>();
                    for (Category row : categories) {
                        if (row.getCategory_name().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }
                    categoriesFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = categoriesFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                categoriesFiltered = (ArrayList<Category>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

}