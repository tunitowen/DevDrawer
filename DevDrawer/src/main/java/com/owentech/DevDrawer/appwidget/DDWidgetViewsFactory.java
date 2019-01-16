package com.owentech.DevDrawer.appwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.utils.Constants;
import com.owentech.DevDrawer.utils.Database;

import java.util.ArrayList;
import java.util.List;

public class DDWidgetViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context context = null;
    private int appWidgetId;
    Database database;

    PackageManager pm;
    List<ResolveInfo> list;

    public List<String> applicationNames;
    public List<String> packageNames;
    public List<Drawable> applicationIcons;

    public DDWidgetViewsFactory(Context context, Intent intent) {
        this.context = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                		AppWidgetManager.INVALID_APPWIDGET_ID);

		// Create the database tables
		database = new Database(context);
		database.createTables();

		onDataSetChanged();

    }

    @Override
    public void onCreate() {
        // Nothing yet
    }

    @Override
    public void onDestroy() {
        // Nothing yet
    }

    @Override
    public int getCount() {
        return applicationNames.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean rootClearCache = sp.getBoolean("rootClearCache", false);

		// Setup the list item and intents for on click
		RemoteViews row = new RemoteViews(context.getPackageName(),
                rootClearCache ?
                        R.layout.list_item_more :
                        R.layout.list_item);

		try
		{
			row.setTextViewText(R.id.packageNameTextView, packageNames.get(position));
			row.setTextViewText(R.id.appNameTextView, applicationNames.get(position));
			row.setImageViewBitmap(R.id.imageView, convertFromDrawable(applicationIcons.get(position)));

			if(sp.getString("theme", "Light").equals("Light"))
			{
				row.setTextColor(R.id.appNameTextView, context.getResources().getColor(R.color.app_name_light));
				row.setImageViewResource(R.id.appDetailsImageButton, R.drawable.settings_imageview);
				row.setImageViewResource(R.id.uninstallImageButton, R.drawable.delete_imageview);
                row.setImageViewResource(R.id.clearImageButton, R.drawable.clear_imageview);
                row.setImageViewResource(R.id.moreImageButton, R.drawable.more_imageview);
			}
			else
			{
				row.setTextColor(R.id.appNameTextView, context.getResources().getColor(R.color.app_name_dark));
				row.setImageViewResource(R.id.appDetailsImageButton, R.drawable.settings_imageview_dark);
				row.setImageViewResource(R.id.uninstallImageButton, R.drawable.delete_imageview_dark);
                row.setImageViewResource(R.id.clearImageButton, R.drawable.clear_imageview_dark);
                row.setImageViewResource(R.id.moreImageButton, R.drawable.more_imageview_dark);
			}

            row.setViewVisibility(R.id.clearImageButton, rootClearCache ? View.VISIBLE : View.GONE);

			Intent appDetailsClickIntent=new Intent();
			Bundle appDetailsClickExtras=new Bundle();
			//appDetailsClickExtras.putBoolean("appDetails", true);
			appDetailsClickExtras.putInt("launchType", Constants.LAUNCH_APP_DETAILS);
			appDetailsClickExtras.putString(DDWidgetProvider.PACKAGE_STRING, packageNames.get(position));
			appDetailsClickIntent.putExtras(appDetailsClickExtras);
			row.setOnClickFillInIntent(R.id.appDetailsImageButton, appDetailsClickIntent);

			Intent uninstallClickIntent=new Intent();
			Bundle uninstallClickExtras=new Bundle();
			//appDetailsClickExtras.putBoolean("appDetails", true);
			uninstallClickExtras.putInt("launchType", Constants.LAUNCH_UNINSTALL);
			uninstallClickExtras.putString(DDWidgetProvider.PACKAGE_STRING, packageNames.get(position));
			uninstallClickIntent.putExtras(uninstallClickExtras);
			row.setOnClickFillInIntent(R.id.uninstallImageButton, uninstallClickIntent);

            Intent clearClickIntent=new Intent();
            Bundle clearClickExtras=new Bundle();
            clearClickExtras.putInt("launchType", Constants.LAUNCH_CLEAR);
            clearClickExtras.putString(DDWidgetProvider.PACKAGE_STRING, packageNames.get(position));
            clearClickIntent.putExtras(clearClickExtras);
            row.setOnClickFillInIntent(R.id.clearImageButton, clearClickIntent);

            Intent moreClickIntent=new Intent();
            Bundle moreClickExtras=new Bundle();
            moreClickExtras.putInt("launchType", Constants.LAUNCH_MORE);
            moreClickExtras.putString(DDWidgetProvider.PACKAGE_STRING, packageNames.get(position));
            moreClickIntent.putExtras(moreClickExtras);
            row.setOnClickFillInIntent(R.id.moreImageButton, moreClickIntent);

			Intent rowClickIntent=new Intent();
			Bundle rowClickExtras=new Bundle();
			//rowClickExtras.putBoolean("appDetails", false);
			rowClickExtras.putInt("launchType", Constants.LAUNCH_APP);
			rowClickExtras.putString(DDWidgetProvider.PACKAGE_STRING, packageNames.get(position));
			rowClickIntent.putExtras(rowClickExtras);
			row.setOnClickFillInIntent(R.id.touchArea, rowClickIntent);

			return(row);
		}
		catch (IndexOutOfBoundsException e)
		{
			return null;
		}
    }

    @Override
    public RemoteViews getLoadingView() {
        return(null);
    }

    @Override
    public int getViewTypeCount() {
        return(1);
    }

    @Override
    public long getItemId(int position) {
        return(position);
    }

    @Override
    public boolean hasStableIds() {
        return(true);
    }

    @Override
    public void onDataSetChanged() {
        // Update the dataset
        getApps();
    }

	// Method to get all apps from the app database and add to the dataset
    public void getApps()
    {

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        // Get all apps from the app table
		String[] packages = database.getAllAppsInDatabase(sp.getString("widgetSorting", "added"));
        pm = context.getPackageManager();

		// Defensive code, was getting some strange behaviour and forcing the lists seems to fix
        applicationNames = null;
        packageNames = null;
        applicationIcons = null;

		// Setup the lists holding the data
		applicationNames = new ArrayList<String>();
		packageNames = new ArrayList<String>();
		applicationIcons = new ArrayList<Drawable>();

		// Loop though adding details from PackageManager to the lists
        for(String s : packages)
        {
            Log.d("DDWidgetViewsFactory", "String is: " + s);
            ApplicationInfo applicationInfo;

            try {
                applicationInfo = pm.getPackageInfo(s, PackageManager.GET_ACTIVITIES).applicationInfo;
                applicationNames.add(applicationInfo.loadLabel(pm).toString());
                packageNames.add(applicationInfo.packageName.toString());
                applicationIcons.add(applicationInfo.loadIcon(pm));

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
    }

	// Method to return a bitmap from drawable
    public Bitmap convertFromDrawable(Drawable d)
    {
        if (d instanceof BitmapDrawable) {
            return ((BitmapDrawable) d).getBitmap();
        } else {
            return getBitmapFromDrawable(d);
        }
    }
}
