package com.app.allpaper4k.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import com.app.allpaper4k.BuildConfig;
import com.app.allpaper4k.models.Category;
import com.app.allpaper4k.models.Wallpaper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by khurshidnormurodov on 12/24/20
 * */
public class DBHelper extends SQLiteOpenHelper {

    private static String DB_NAME = "allphoto4k.db";
    private SQLiteDatabase db;
    private final Context context;
    private String DB_PATH;
    String outFileName = "";
    SharedPreferences.Editor spEdit;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, 1);
        this.context = context;
        DB_PATH = "/data/data/" + BuildConfig.APPLICATION_ID + "/databases/";
    }


    public void createDataBase() throws IOException {

        boolean dbExist = checkDataBase();
        //------------------------------------------------------------
        PackageInfo pinfo = null;
        if (!dbExist) {
            getReadableDatabase();
            copyDataBase();
        }

    }

    private boolean checkDataBase() {
        File dbFile = new File(DB_PATH + DB_NAME);
        return dbFile.exists();
    }

    private void copyDataBase() throws IOException {

        InputStream myInput = context.getAssets().open(DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        // Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    public Cursor getData(String Query) {
        String myPath = DB_PATH + DB_NAME;
        Cursor c = null;
        try {
            db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
            c = db.rawQuery(Query, null);
        } catch (Exception e) {
            Log.e("Err", e.toString());
        }
        return c;
    }

    //UPDATE temp_dquot SET age='20',name1='--',rdt='11/08/2014',basic_sa='100000',plno='814',pterm='20',mterm='20',mat_date='11/08/2034',mode='YLY',dab_sa='100000',tr_sa='0',cir_sa='',bonus_rate='42',prem='5276',basic_prem='5118',dab_prem='100.0',step_rate='for Life',loyal_rate='0',bonus_rate='42',act_mat='1,88,000',mly_b_pr='448',qly_b_pr='1345',hly_b_pr='2664',yly_b_pr='5276'  WHERE uniqid=1
    public void dml(String Query) {
        String myPath = DB_PATH + DB_NAME;
        if (db == null)
            db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
        try {
            db.execSQL(Query);
        } catch (Exception e) {
            Log.e("Error", e.toString());
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            db.disableWriteAheadLogging();
        }
    }

    public ArrayList<Wallpaper> getAllData(String table) {
        ArrayList<Wallpaper> arrayList = new ArrayList<Wallpaper>();

        //Cursor cursor = getData("select * from '" + table + "'" + " ORDER BY RANDOM()");
        Cursor cursor = getData("select * from '" + table + "'");
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                String image_id = cursor.getString(cursor.getColumnIndex("image_id"));
                String image_upload = cursor.getString(cursor.getColumnIndex("image_upload"));
                String image_url = cursor.getString(cursor.getColumnIndex("image_url"));
                String type = cursor.getString(cursor.getColumnIndex("type"));
                int view_count = cursor.getInt(cursor.getColumnIndex("view_count"));
                int download_count = cursor.getInt(cursor.getColumnIndex("download_count"));
                String featured = cursor.getString(cursor.getColumnIndex("featured"));
                String tags = cursor.getString(cursor.getColumnIndex("tags"));
                String category_id = cursor.getString(cursor.getColumnIndex("category_id"));
                String category_name = cursor.getString(cursor.getColumnIndex("category_name"));

                Wallpaper wallpaper = new Wallpaper(image_id, image_upload, image_url, type, view_count, download_count, featured, tags, category_id, category_name);
                arrayList.add(wallpaper);

                cursor.moveToNext();
            }
            cursor.close();
        }

        return arrayList;
    }

    public ArrayList<Category> getAllDataCategory(String table) {
        ArrayList<Category> arrayList = new ArrayList<Category>();

        Cursor cursor = getData("select * from '" + table + "'");
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                String category_id = cursor.getString(cursor.getColumnIndex("category_id"));
                String category_name = cursor.getString(cursor.getColumnIndex("category_name"));
                String category_image = cursor.getString(cursor.getColumnIndex("category_image"));
                String total_wallpaper = cursor.getString(cursor.getColumnIndex("total_wallpaper"));

                Category itemCategory = new Category(category_id, category_name, category_image, total_wallpaper);
                arrayList.add(itemCategory);

                cursor.moveToNext();
            }
            cursor.close();
        }

        return arrayList;
    }

    public ArrayList<Wallpaper> getRandom(String table) {
        ArrayList<Wallpaper> arrayList = new ArrayList<Wallpaper>();

        Cursor cursor = getData("select * from '" + table + "'" + " ORDER BY RANDOM()");
        //Cursor cursor = getData("select * from '" + table + "'");
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                String image_id = cursor.getString(cursor.getColumnIndex("image_id"));
                String image_upload = cursor.getString(cursor.getColumnIndex("image_upload"));
                String image_url = cursor.getString(cursor.getColumnIndex("image_url"));
                String type = cursor.getString(cursor.getColumnIndex("type"));
                int view_count = cursor.getInt(cursor.getColumnIndex("view_count"));
                int download_count = cursor.getInt(cursor.getColumnIndex("download_count"));
                String featured = cursor.getString(cursor.getColumnIndex("featured"));
                String tags = cursor.getString(cursor.getColumnIndex("tags"));
                String category_id = cursor.getString(cursor.getColumnIndex("category_id"));
                String category_name = cursor.getString(cursor.getColumnIndex("category_name"));

                Wallpaper wallpaper = new Wallpaper(image_id, image_upload, image_url, type, view_count, download_count, featured, tags, category_id, category_name);
                arrayList.add(wallpaper);

                cursor.moveToNext();
            }
            cursor.close();
        }

        return arrayList;
    }

    public ArrayList<Wallpaper> getPopular(String table) {
        ArrayList<Wallpaper> arrayList = new ArrayList<Wallpaper>();

        Cursor cursor = getData("select * from '" + table + "'" + " ORDER BY view_count DESC");
        //Cursor cursor = getData("select * from '" + table + "'");
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                String image_id = cursor.getString(cursor.getColumnIndex("image_id"));
                String image_upload = cursor.getString(cursor.getColumnIndex("image_upload"));
                String image_url = cursor.getString(cursor.getColumnIndex("image_url"));
                String type = cursor.getString(cursor.getColumnIndex("type"));
                int view_count = cursor.getInt(cursor.getColumnIndex("view_count"));
                int download_count = cursor.getInt(cursor.getColumnIndex("download_count"));
                String featured = cursor.getString(cursor.getColumnIndex("featured"));
                String tags = cursor.getString(cursor.getColumnIndex("tags"));
                String category_id = cursor.getString(cursor.getColumnIndex("category_id"));
                String category_name = cursor.getString(cursor.getColumnIndex("category_name"));

                Wallpaper wallpaper = new Wallpaper(image_id, image_upload, image_url, type, view_count, download_count, featured, tags, category_id, category_name);
                arrayList.add(wallpaper);

                cursor.moveToNext();
            }
            cursor.close();
        }

        return arrayList;
    }

    public ArrayList<Wallpaper> getFeatured(String table) {
        ArrayList<Wallpaper> arrayList = new ArrayList<Wallpaper>();

        Cursor cursor = getData("select * from '" + table + "'" + " WHERE featured = 'yes' ORDER BY id DESC");
        //Cursor cursor = getData("select * from '" + table + "'");
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                String image_id = cursor.getString(cursor.getColumnIndex("image_id"));
                String image_upload = cursor.getString(cursor.getColumnIndex("image_upload"));
                String image_url = cursor.getString(cursor.getColumnIndex("image_url"));
                String type = cursor.getString(cursor.getColumnIndex("type"));
                int view_count = cursor.getInt(cursor.getColumnIndex("view_count"));
                int download_count = cursor.getInt(cursor.getColumnIndex("download_count"));
                String featured = cursor.getString(cursor.getColumnIndex("featured"));
                String tags = cursor.getString(cursor.getColumnIndex("tags"));
                String category_id = cursor.getString(cursor.getColumnIndex("category_id"));
                String category_name = cursor.getString(cursor.getColumnIndex("category_name"));

                Wallpaper wallpaper = new Wallpaper(image_id, image_upload, image_url, type, view_count, download_count, featured, tags, category_id, category_name);
                arrayList.add(wallpaper);

                cursor.moveToNext();
            }
            cursor.close();
        }

        return arrayList;
    }

    public ArrayList<Wallpaper> getAllFavorite(String table) {
        ArrayList<Wallpaper> arrayList = new ArrayList<Wallpaper>();

        Cursor cursor = getData("select * from '" + table + "'" + " ORDER BY id DESC");
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                String image_id = cursor.getString(cursor.getColumnIndex("image_id"));
                String image_upload = cursor.getString(cursor.getColumnIndex("image_upload"));
                String image_url = cursor.getString(cursor.getColumnIndex("image_url"));
                String type = cursor.getString(cursor.getColumnIndex("type"));
                int view_count = cursor.getInt(cursor.getColumnIndex("view_count"));
                int download_count = cursor.getInt(cursor.getColumnIndex("download_count"));
                String featured = cursor.getString(cursor.getColumnIndex("featured"));
                String tags = cursor.getString(cursor.getColumnIndex("tags"));
                String category_id = cursor.getString(cursor.getColumnIndex("category_id"));
                String category_name = cursor.getString(cursor.getColumnIndex("category_name"));

                Wallpaper wallpaper = new Wallpaper(image_id, image_upload, image_url, type, view_count, download_count, featured, tags, category_id, category_name);
                arrayList.add(wallpaper);

                cursor.moveToNext();
            }
            cursor.close();
        }

        return arrayList;
    }

    public ArrayList<Wallpaper> getFavRow(String id, String table) {
        ArrayList<Wallpaper> arrayList = new ArrayList<Wallpaper>();
        // Select All Query
        String selectQuery = "SELECT  * FROM '" + table + "' WHERE image_id=" + "'" + id + "'";

        Cursor cursor = getData(selectQuery);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                String image_id = cursor.getString(cursor.getColumnIndex("image_id"));
                String image_upload = cursor.getString(cursor.getColumnIndex("image_upload"));
                String image_url = cursor.getString(cursor.getColumnIndex("image_url"));
                String type = cursor.getString(cursor.getColumnIndex("type"));
                int view_count = cursor.getInt(cursor.getColumnIndex("view_count"));
                int download_count = cursor.getInt(cursor.getColumnIndex("download_count"));
                String featured = cursor.getString(cursor.getColumnIndex("featured"));
                String tags = cursor.getString(cursor.getColumnIndex("tags"));
                String category_id = cursor.getString(cursor.getColumnIndex("category_id"));
                String category_name = cursor.getString(cursor.getColumnIndex("category_name"));

                Wallpaper wallpaper = new Wallpaper(image_id, image_upload, image_url, type, view_count, download_count, featured, tags, category_id, category_name);
                arrayList.add(wallpaper);

                cursor.moveToNext();
            }
            cursor.close();
        }

        // return contact list
        return arrayList;
    }


    public ArrayList<Wallpaper> getCategoryDetail(String id, String table) {
        ArrayList<Wallpaper> arrayList = new ArrayList<Wallpaper>();
        // Select All Query
        String selectQuery = "SELECT  * FROM '" + table + "' WHERE category_id=" + "'" + id + "'";

        Cursor cursor = getData(selectQuery);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                String image_id = cursor.getString(cursor.getColumnIndex("image_id"));
                String image_upload = cursor.getString(cursor.getColumnIndex("image_upload"));
                String image_url = cursor.getString(cursor.getColumnIndex("image_url"));
                String type = cursor.getString(cursor.getColumnIndex("type"));
                int view_count = cursor.getInt(cursor.getColumnIndex("view_count"));
                int download_count = cursor.getInt(cursor.getColumnIndex("download_count"));
                String featured = cursor.getString(cursor.getColumnIndex("featured"));
                String tags = cursor.getString(cursor.getColumnIndex("tags"));
                String category_id = cursor.getString(cursor.getColumnIndex("category_id"));
                String category_name = cursor.getString(cursor.getColumnIndex("category_name"));

                Wallpaper wallpaper = new Wallpaper(image_id, image_upload, image_url, type, view_count, download_count, featured, tags, category_id, category_name);
                arrayList.add(wallpaper);

                cursor.moveToNext();
            }
            cursor.close();
        }

        // return contact list
        return arrayList;
    }

    public void saveWallpaper(Wallpaper wallpaper, String table) {
        dml("insert into '" + table + "' (image_id, image_upload, image_url, type, view_count, download_count, featured, tags, category_id, category_name) values ('" + wallpaper.getImage_id() + "','" + wallpaper.getImage_upload() + "','" + wallpaper.getImage_url() + "','" + wallpaper.getType() + "','" + wallpaper.getView_count() + "','" + wallpaper.getDownload_count() + "','" + wallpaper.getFeatured() + "','" + wallpaper.getTags() + "','" + wallpaper.getCategory_id() + "','" + wallpaper.getCategory_name() + "')");
    }

    public void addtoCategory(Category category, String table) {
        dml("insert into '" + table + "' (category_id, category_name, category_image, total_wallpaper) values ('" + category.getCategory_id() + "','" + category.getCategory_name() + "','" + category.getCategory_image() + "','" + category.getTotal_wallpaper() + "')");
    }

    public void removeFav(String id) {
        dml("delete from tbl_favorite where image_id = '" + id + "'");
    }

    public void deleteAllCategory() {
        dml("delete from tbl_category");
        dml("delete from tbl_category_detail");
        dml("delete from tbl_favorite");
        dml("delete from tbl_featured");
        dml("delete from tbl_popular");
        dml("delete from tbl_random");
        dml("delete from tbl_recent");
    }

    public void deleteData(String table) {
        dml("delete from '" + table + "'");
    }

    public void resetCategoryDetail(String table, String id) {
        dml("delete from '" + table + "' where category_id = '" + id + "'");
    }

    public void updateView(String id, String total_view) {
        int view_count = Integer.parseInt(total_view) + 1;
        dml("update tbl_category_detail set view_count = '" + String.valueOf(view_count) + "' where image_id = '" + id + "'");
        dml("update tbl_favorite set view_count = '" + String.valueOf(view_count) + "' where image_id = '" + id + "'");
        dml("update tbl_featured set view_count = '" + String.valueOf(view_count) + "' where image_id = '" + id + "'");
        dml("update tbl_popular set view_count = '" + String.valueOf(view_count) + "' where image_id = '" + id + "'");
        dml("update tbl_random set view_count = '" + String.valueOf(view_count) + "' where image_id = '" + id + "'");
        dml("update tbl_recent set view_count = '" + String.valueOf(view_count) + "' where image_id = '" + id + "'");
    }

    public void updateDownload(String id, String total_download) {
        int view_count = Integer.parseInt(total_download) + 1;
        dml("update tbl_category_detail set download_count = '" + String.valueOf(view_count) + "' where image_id = '" + id + "'");
        dml("update tbl_favorite set download_count = '" + String.valueOf(view_count) + "' where image_id = '" + id + "'");
        dml("update tbl_featured set download_count = '" + String.valueOf(view_count) + "' where image_id = '" + id + "'");
        dml("update tbl_popular set download_count = '" + String.valueOf(view_count) + "' where image_id = '" + id + "'");
        dml("update tbl_random set download_count = '" + String.valueOf(view_count) + "' where image_id = '" + id + "'");
        dml("update tbl_recent set download_count = '" + String.valueOf(view_count) + "' where image_id = '" + id + "'");
    }

}  