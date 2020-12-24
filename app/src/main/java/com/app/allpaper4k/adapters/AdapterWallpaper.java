package com.app.allpaper4k.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.app.allpaper4k.Config;
import com.app.allpaper4k.R;
import com.app.allpaper4k.models.Wallpaper;
import com.app.allpaper4k.utilities.Tools;
import com.balysv.materialripple.MaterialRippleLayout;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by khurshidnormurodov on 12/24/20
 * */

public class AdapterWallpaper extends RecyclerView.Adapter<AdapterWallpaper.MyViewHolder> {

    private ArrayList<Wallpaper> wallpapers;
    private OnItemClickListener mOnItemClickListener;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(View view, Wallpaper obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemOverflowClickListener) {
        this.mOnItemClickListener = mItemOverflowClickListener;
    }

    public AdapterWallpaper(Context context, ArrayList<Wallpaper> wallpapers) {
        this.context = context;
        this.wallpapers = wallpapers;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView img_wallpaper;
        TextView txt_view_count, txt_download_count;
        private MaterialRippleLayout lyt_parent;
        private RelativeLayout lyt_view_download;
        private LinearLayout lyt_view_count, lyt_download_count;

        MyViewHolder(View view) {
            super(view);
            img_wallpaper = view.findViewById(R.id.img_wallpaper);
            txt_view_count = view.findViewById(R.id.txt_view_count);
            txt_download_count = view.findViewById(R.id.txt_download_count);
            lyt_parent = view.findViewById(R.id.lyt_parent);
            lyt_view_download = view.findViewById(R.id.lyt_view_download);
            lyt_view_count = view.findViewById(R.id.lyt_view_count);
            lyt_download_count = view.findViewById(R.id.lyt_download_count);

        }
    }

    @Override
    public AdapterWallpaper.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (Config.ENABLE_DISPLAY_WALLPAPER_IN_SQUARE) {
            return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.lsv_item_square_wallpaper, parent, false));
        } else {
            return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.lsv_item_rectangle_wallpaper, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(AdapterWallpaper.MyViewHolder holder, final int position) {

        if (wallpapers.get(position).getType().equals("url")) {
            Picasso.with(context)
                    .load(wallpapers.get(position).getImage_url().replace(" ", "%20"))
                    .placeholder(R.drawable.ic_transparent)
                    .resizeDimen(R.dimen.list_wallpaper_width, R.dimen.list_wallpaper_height)
                    .centerCrop()
                    .into(holder.img_wallpaper);
        } else {
            Picasso.with(context)
                    .load(Config.ADMIN_PANEL_URL + "/upload/" + wallpapers.get(position).getImage_upload().replace(" ", "%20"))
                    .placeholder(R.drawable.ic_transparent)
                    .resizeDimen(R.dimen.list_wallpaper_width, R.dimen.list_wallpaper_height)
                    .centerCrop()
                    .into(holder.img_wallpaper);
        }

        holder.txt_view_count.setText("" + Tools.withSuffix(wallpapers.get(position).getView_count()));
        holder.txt_download_count.setText("" + Tools.withSuffix(wallpapers.get(position).getDownload_count()));

        if (Config.SHOW_VIEW_COUNT) {
            holder.lyt_view_count.setVisibility(View.VISIBLE);
        } else {
            holder.lyt_view_count.setVisibility(View.GONE);
        }

        if (Config.SHOW_DOWNLOAD_COUNT) {
            holder.lyt_download_count.setVisibility(View.VISIBLE);
        } else {
            holder.lyt_download_count.setVisibility(View.GONE);
        }

        if (!Config.SHOW_VIEW_COUNT && !Config.SHOW_DOWNLOAD_COUNT) {
            holder.lyt_view_download.setVisibility(View.GONE);
        } else {
            holder.lyt_view_download.setVisibility(View.VISIBLE);
        }

        holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, wallpapers.get(position), position);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return wallpapers.size();
    }

}