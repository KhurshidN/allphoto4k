package com.app.allpaper4k;

/**
 * Created by khurshidnormurodov on 12/24/20
 * */

public class Config {
    //admin panel url
    public static final String ADMIN_PANEL_URL = "https://allphoto4k.camano.uz/";

    //number of grid column of wallpaper images
    public static final int NUM_OF_COLUMNS = 4;

    //set true to enable ads or set false to disable ads
    public static final boolean ENABLE_ADMOB_BANNER_ADS_MAIN_PAGE = false;
    public static final boolean ENABLE_ADMOB_BANNER_ADS_WALLPAPER_DETAIL = false;
    public static final boolean ENABLE_ADMOB_INTERSTITIAL_ADS = false;
    public static final int INTERSTITIAL_ADS_INTERVAL = 3;

    //custom layout views
    public static final boolean SHOW_VIEW_COUNT = false;
    public static final boolean SHOW_DOWNLOAD_COUNT = false;
    public static final boolean ENABLE_DISPLAY_WALLPAPER_IN_SQUARE = false;
    public static final boolean ENABLE_CENTER_CROP_IN_DETAIL_WALLPAPER = true;
    public static final boolean ENABLE_NAVIGATION_DRAWER_HEADER_INFO = true;

    //set true to enable exit dialog or set false to disable exit dialog
    public static final boolean ENABLE_EXIT_DIALOG = true;

    //set true if you only want to display recent wallpaper and categories on the main screen
    public static final boolean ENABLE_CLASSIC_MODE = false;

    //if this option enabled, the way to set wallpaper will be switch to other apps inside your phone system
    //and set wallpaper inside this application with wallpaper manager will be disabled
    public static final boolean ENABLE_SET_WALLPAPER_WITH_OTHER_APPS = false;

    //set true if you want to enable RTL (Right To Left) mode, e.g : Arabic Language
    public static final boolean ENABLE_RTL_MODE = false;

}