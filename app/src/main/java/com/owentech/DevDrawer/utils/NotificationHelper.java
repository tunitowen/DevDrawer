package com.owentech.DevDrawer.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.activities.ClickHandlingActivity;
import com.owentech.DevDrawer.appwidget.DDWidgetProvider;


/**
 * Created by tonyowen on 15/07/2014.
 */
public class NotificationHelper {

    public static String applicationName;
    public static String packageName;
    public static Drawable applicationIcon;
    public static int applicationUid;

    public static void showNotification(Context context, String packageName, long id){

        getAppInformation(context, packageName);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setContentTitle(applicationName);
        notificationBuilder.setContentText(packageName);
        notificationBuilder.setSmallIcon(R.drawable.notifcationicon);
        notificationBuilder.setLargeIcon(convertFromDrawable(applicationIcon));
        notificationBuilder.addAction(R.drawable.trashwhite, "Uninstall", uninstallPendingIntent(context, packageName, (int)id));
        notificationBuilder.addAction(R.drawable.settingswhite, "App Settings", appDetailsPendingIntent(context, packageName, (int)id));
        notificationBuilder.setPriority(Notification.PRIORITY_LOW);
        notificationBuilder.setContentIntent(contentPendingIntent(context, packageName, (int)id));

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify((int)id,
                notificationBuilder.build());
    }

    public static void removeNotification(Context context, int id){
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(id);
    }

    // Method to get all apps from the app database and add to the dataset
    public static void getAppInformation(Context context, String packageName) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        PackageManager pm = context.getPackageManager();

        ApplicationInfo applicationInfo;

        try {
            applicationInfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES).applicationInfo;
            applicationUid = applicationInfo.uid;
            applicationName = applicationInfo.loadLabel(pm).toString();
            packageName = applicationInfo.packageName;
            applicationIcon = applicationInfo.loadIcon(pm);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Method to return a bitmap from drawable
    public static Bitmap convertFromDrawable(Drawable d) {
        return ((BitmapDrawable) d).getBitmap();
    }

    private static PendingIntent uninstallPendingIntent(Context context, String pName, int id){
        Intent uninstallIntent = new Intent(context, ClickHandlingActivity.class);
        uninstallIntent.setAction(Long.toString(System.currentTimeMillis()));
        Bundle uninstallExtras = new Bundle();
        uninstallExtras.putInt("launchType", AppConstants.LAUNCH_UNINSTALL);
        uninstallExtras.putString(DDWidgetProvider.PACKAGE_STRING, pName);
        uninstallIntent.putExtras(uninstallExtras);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, uninstallIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        pendingIntent.cancel();
        pendingIntent = PendingIntent.getActivity(context, id, uninstallIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        return pendingIntent;
    }

    private static PendingIntent appDetailsPendingIntent(Context context, String pName, int id){
        Intent appDetailsIntent = new Intent(context, ClickHandlingActivity.class);
        appDetailsIntent.setAction(Long.toString(System.currentTimeMillis()));
        Bundle appDetailsExtras = new Bundle();
        appDetailsExtras.putInt("launchType", AppConstants.LAUNCH_APP_DETAILS);
        appDetailsExtras.putString(DDWidgetProvider.PACKAGE_STRING, pName);
        appDetailsIntent.putExtras(appDetailsExtras);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, appDetailsIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        pendingIntent.cancel();
        pendingIntent = PendingIntent.getActivity(context, id, appDetailsIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        return pendingIntent;
    }

    private static PendingIntent contentPendingIntent(Context context, String pName, int id){
        Intent openAppIntent = new Intent(context, ClickHandlingActivity.class);
        openAppIntent.setAction(Long.toString(System.currentTimeMillis()));
        Bundle openAppExtras = new Bundle();
        openAppExtras.putInt("launchType", AppConstants.LAUNCH_APP);
        openAppExtras.putString(DDWidgetProvider.PACKAGE_STRING, packageName);
        openAppIntent.putExtras(openAppExtras);
        PendingIntent openAppPendingIntent = PendingIntent.getActivity(context, 0, openAppIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        openAppPendingIntent.cancel();
        openAppPendingIntent = PendingIntent.getActivity(context, 0, openAppIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        return openAppPendingIntent;
    }

}
