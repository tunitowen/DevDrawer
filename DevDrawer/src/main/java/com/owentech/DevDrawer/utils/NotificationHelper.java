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

    public static void showNotification(Context context, String packageName){

        getAppInformation(context, packageName);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setContentTitle(applicationName);
        notificationBuilder.setContentText(packageName);
        notificationBuilder.setSmallIcon(R.drawable.notifcationicon);

        Intent uninstallIntent = new Intent(context, ClickHandlingActivity.class);
        Bundle uninstallExtras = new Bundle();
        uninstallExtras.putInt("launchType", AppConstants.LAUNCH_UNINSTALL);
        uninstallExtras.putString(DDWidgetProvider.PACKAGE_STRING, packageName);
        uninstallIntent.putExtras(uninstallExtras);
        PendingIntent uninstallPendingIntent = PendingIntent.getActivity(context, 0, uninstallIntent, PendingIntent.FLAG_ONE_SHOT);

        Intent appDetailsIntent = new Intent(context, ClickHandlingActivity.class);
        Bundle appDetailsExtras = new Bundle();
        appDetailsExtras.putInt("launchType", AppConstants.LAUNCH_APP_DETAILS);
        appDetailsExtras.putString(DDWidgetProvider.PACKAGE_STRING, packageName);
        appDetailsIntent.putExtras(appDetailsExtras);
        PendingIntent appDetailsPendingIntent = PendingIntent.getActivity(context, 0, appDetailsIntent, PendingIntent.FLAG_ONE_SHOT);

        notificationBuilder.addAction(R.drawable.ic_action_trash_white, "Uninstall", uninstallPendingIntent);
        notificationBuilder.addAction(R.drawable.ic_action_settings_white, "App Settings", appDetailsPendingIntent);
        notificationBuilder.setPriority(Notification.PRIORITY_LOW);
        notificationBuilder.setAutoCancel(true);

        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);;
        mNotificationManager.notify(applicationUid,
                notificationBuilder.build());
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
    public Bitmap convertFromDrawable(Drawable d) {
        return ((BitmapDrawable) d).getBitmap();
    }

}
