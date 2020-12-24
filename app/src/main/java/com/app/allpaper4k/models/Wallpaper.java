package com.app.allpaper4k.models;

/**
 * Created by khurshidnormurodov on 12/24/20
 * */

public class Wallpaper {

    private String image_id;
    private String image_upload;
    private String image_url;
    private String type;
    private int view_count;
    private int download_count;
    private String featured;
    private String tags;
    private String category_id;
    private String category_name;

    public Wallpaper(String image_id, String image_upload, String image_url, String type, int view_count, int download_count, String featured, String tags, String category_id, String category_name) {
        this.image_id = image_id;
        this.image_upload = image_upload;
        this.image_url = image_url;
        this.type = type;
        this.view_count = view_count;
        this.download_count = download_count;
        this.featured = featured;
        this.tags = tags;
        this.category_id = category_id;
        this.category_name = category_name;
    }

    public String getImage_id() {
        return image_id;
    }

    public void setImage_id(String image_id) {
        this.image_id = image_id;
    }

    public String getImage_upload() {
        return image_upload;
    }

    public void setImage_upload(String image_upload) {
        this.image_upload = image_upload;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getView_count() {
        return view_count;
    }

    public void setView_count(int view_count) {
        this.view_count = view_count;
    }

    public int getDownload_count() {
        return download_count;
    }

    public void setDownload_count(int download_count) {
        this.download_count = download_count;
    }

    public String getFeatured() {
        return featured;
    }

    public void setFeatured(String featured) {
        this.featured = featured;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
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
}
