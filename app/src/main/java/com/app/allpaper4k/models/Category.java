package com.app.allpaper4k.models;

/**
 * Created by khurshidnormurodov on 12/24/20
 * */

public class Category {

    private String category_id;
    private String category_name;
    private String category_image;
    private String total_wallpaper;

    public Category(String category_id, String category_name, String category_image, String total_wallpaper) {
        this.category_id = category_id;
        this.category_name = category_name;
        this.category_image = category_image;
        this.total_wallpaper = total_wallpaper;
    }

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public String getCategory_image() {
        return category_image;
    }

    public void setCategory_image(String category_image) {
        this.category_image = category_image;
    }

    public String getTotal_wallpaper() {
        return total_wallpaper;
    }

    public void setTotal_wallpaper(String total_wallpaper) {
        this.total_wallpaper = total_wallpaper;
    }
}
