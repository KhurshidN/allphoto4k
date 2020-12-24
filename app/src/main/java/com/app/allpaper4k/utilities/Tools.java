package com.app.allpaper4k.utilities;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.app.allpaper4k.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by khurshidnormurodov on 12/24/20
 * */

public class Tools {

    public static final boolean LOG = true;
    public Context context;

    public Tools(Context context) {
        this.context = context;
    }

    public static String withSuffix(long count) {
        if (count < 1000) return "" + count;
        int exp = (int) (Math.log(count) / Math.log(1000));
        return String.format("%.1f%c", count / Math.pow(1000, exp), "KMGTPE".charAt(exp-1));
    }

    public static String getJSONString(String url) {
        String jsonString = null;
        HttpURLConnection linkConnection = null;
        try {
            URL linkurl = new URL(url);
            linkConnection = (HttpURLConnection) linkurl.openConnection();
            int responseCode = linkConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream linkinStream = linkConnection.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int j = 0;
                while ((j = linkinStream.read()) != -1) {
                    baos.write(j);
                }
                byte[] data = baos.toByteArray();
                jsonString = new String(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (linkConnection != null) {
                linkConnection.disconnect();
            }
        }
        return jsonString;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void printStackTrace(Exception e) {
        if (LOG) e.printStackTrace();
    }

    public static void download(Activity context, String url) {
        if (!hasPermissionToDownload(context))
            return;

        String source = url;

        if (source == null) {
            Toast.makeText(context, context.getResources().getString(R.string.download_unavailable), Toast.LENGTH_LONG).show();
            return;
        }

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri Download_Uri = Uri.parse(source);
        String name = url.substring(url.lastIndexOf("/"), url.length());

        DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
        final Long downloadReference = downloadManager.enqueue(request);

        //A broadcast receiver to open the file after it is downloaded
        String downloadCompleteIntentName = DownloadManager.ACTION_DOWNLOAD_COMPLETE;
        IntentFilter downloadCompleteIntentFilter = new IntentFilter(downloadCompleteIntentName);
        BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
                if (id != downloadReference) {
                    return;
                }

                //Unregister the broadcastreceiver
                context.unregisterReceiver(this);

                //A list of actions from here on to open the file
                DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(id);
                Cursor cursor = downloadManager.query(query);

                // it shouldn't be empty, but just in case
                if (!cursor.moveToFirst()) {
                    return;
                }

                int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                if (DownloadManager.STATUS_SUCCESSFUL != cursor.getInt(statusIndex)) {
                    Toast.makeText(context, context.getResources().getString(R.string.download_failed), Toast.LENGTH_LONG).show();
                    return;
                }

                int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                String downloadedPackageUriString = cursor.getString(uriIndex);
                Uri fileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", new File(Uri.parse(downloadedPackageUriString).getPath()));

                Intent open = new Intent(Intent.ACTION_VIEW);
                open.setData(fileUri);
                open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                open.setDataAndType(fileUri, downloadManager.getMimeTypeForDownloadedFile(id));

                PackageManager manager = context.getPackageManager();
                List<ResolveInfo> infos = manager.queryIntentActivities(open, 0);
                if (infos.size() > 0) {
                    //Then there is an Application(s) can handle the intent
                    context.startActivity(open);
                } else {
                    //No Application can handle your intent
                    Toast.makeText(context, context.getResources().getString(R.string.download_open_failed), Toast.LENGTH_LONG).show();
                }

            }
        };

        //Register the broadcastreceiver
        context.registerReceiver(downloadCompleteReceiver, downloadCompleteIntentFilter);

    }

    private static boolean hasPermissionToDownload(final Activity context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED)
            return true;

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setMessage(R.string.download_permission_explaination);
        builder.setPositiveButton(R.string.download_permission_grant, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Fire off an async request to actually get the permission
                // This will show the standard permission request dialog UI
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    context.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        });
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

        return false;
    }

}