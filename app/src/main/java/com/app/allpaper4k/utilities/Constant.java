package com.app.allpaper4k.utilities;

import com.app.allpaper4k.Config;
import com.app.allpaper4k.models.Wallpaper;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by khurshidnormurodov on 12/24/20
 * */

public class Constant implements Serializable {

    public static final String URL_CATEGORY = Config.ADMIN_PANEL_URL + "/api/api.php?action=get_category";
    public static final String URL_CATEGORY_DETAIL = Config.ADMIN_PANEL_URL + "/api/api.php?action=get_category_detail";
    public static final String URL_RECENT_WALLPAPER = Config.ADMIN_PANEL_URL + "/api/api.php?action=get_recent&offset=";
    public static final String URL_POPULAR_WALLPAPER = Config.ADMIN_PANEL_URL + "/api/api.php?action=get_popular&offset=";
    public static final String URL_RANDOM_WALLPAPER = Config.ADMIN_PANEL_URL + "/api/api.php?action=get_random&offset=";
    public static final String URL_FEATURED_WALLPAPER = Config.ADMIN_PANEL_URL + "/api/api.php?action=get_featured&offset=";
    public static final String URL_SEARCH_WALLPAPER = Config.ADMIN_PANEL_URL + "/api/api.php?action=get_search";
    public static final String URL_PRIVACY_POLICY = Config.ADMIN_PANEL_URL + "/api/api.php?action=get_privacy_policy";
    public static final String URL_VIEW_COUNT = Config.ADMIN_PANEL_URL + "/api/api.php?action=view_count&id=";
    public static final String URL_DOWNLOAD_COUNT = Config.ADMIN_PANEL_URL + "/api/api.php?action=download_count&id=";

    public static final String TABLE_CATEGORY = "tbl_category";
    public static final String TABLE_CATEGORY_DETAIL = "tbl_category_detail";
    public static final String TABLE_FAVORITE = "tbl_favorite";
    public static final String TABLE_RECENT = "tbl_recent";
    public static final String TABLE_POPULAR = "tbl_popular";
    public static final String TABLE_RANDOM = "tbl_random";
    public static final String TABLE_FEATURED = "tbl_featured";

    public static final String NO = "no";
    public static final String IMAGE_ID = "image_id";
    public static final String IMAGE_UPLOAD = "image_upload";
    public static final String IMAGE_URL = "image_url";
    public static final String TYPE = "type";
    public static final String VIEW_COUNT = "view_count";
    public static final String DOWNLOAD_COUNT = "download_count";
    public static final String FEATURED = "featured";
    public static final String TAGS = "tags";
    public static final String CATEGORY_ID = "category_id";
    public static final String CATEGORY_NAME = "category_name";
    public static final String CATEGORY_IMAGE = "category_image";
    public static final String TOTAL_WALLPAPER = "total_wallpaper";

    public static ArrayList<Wallpaper> arrayList = new ArrayList<Wallpaper>();
    public static final int DELAY_PROGRESS = 200;
    public static final int DELAY_REFRESH = 1000;
    public static final int DELAY_LOAD_MORE = 1500;
    public static final int DELAY_SET_WALLPAPER = 2000;

}
