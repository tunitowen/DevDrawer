package com.owentech.DevDrawer.utils;

import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Build;
import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.appwidget.DDWidgetProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: tony
 * Date: 03/02/13
 * Time: 20:34
 * To change this template use File | Settings | File Templates.
 */
public class AddAllAppsAsync extends AsyncTask
{
    ProgressDialog progressDialog;
    Context context;
    Database database;
    String newFilter;

    public AddAllAppsAsync(Context context, String newFilter)
    {
        this.context = context;
        this.newFilter = newFilter;
        progressDialog = new ProgressDialog(context);
    }

    @Override
    protected Object doInBackground(Object... objects)
    {
        getAllAppsInstalledAndAdd(newFilter);
        return null;
    }

    // Method to check existing installed apps and add to apps table if they match the filter
    public void getAllAppsInstalledAndAdd(String newFilter)
    {

        List<String> appPackages = new ArrayList<String>();
        PackageManager pm;
        List<ResolveInfo> list;

        database = new Database(context);

        // get installed applications
        pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        list = pm.queryIntentActivities(intent,
                PackageManager.PERMISSION_GRANTED);

        // Loop through the installed apps and check if they match the new filter
        for (ResolveInfo rInfo : list)
        {

            String currentPackage = rInfo.activityInfo.applicationInfo.packageName.toLowerCase();

            if (newFilter.contains("*"))
            {
                if (currentPackage.toLowerCase().startsWith(newFilter.toLowerCase().substring(0, newFilter.indexOf("*"))))
                    appPackages.add(currentPackage);

            }
            else
            {
                if (currentPackage.toLowerCase().equals(newFilter.toLowerCase()))
                    appPackages.add(currentPackage);

            }

        }

        // If the list is > 0 add the packages to the database
        if(appPackages.size() != 0)
        {
            for (String s : appPackages)
            {
                List<PackageCollection> packageCollections = database.getAllFiltersInDatabase();

                database.addAppToDatabase(s, packageCollections.get(packageCollections.size()-1).mId);

				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				{
					AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
					int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, DDWidgetProvider.class));
					appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listView);
				}
            }
        }
    }
}
