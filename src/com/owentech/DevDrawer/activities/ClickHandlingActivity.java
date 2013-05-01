package com.owentech.DevDrawer.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;
import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.appwidget.DDWidgetProvider;
import com.owentech.DevDrawer.utils.Constants;
import com.owentech.DevDrawer.utils.Database;
import com.owentech.DevDrawer.utils.RootFeatures;

/**
 * Created with IntelliJ IDEA.
 * User: owent
 * Date: 29/01/2013
 * Time: 06:31
 * To change this template use File | Settings | File Templates.
 */
public class ClickHandlingActivity extends Activity
{

	SharedPreferences sp;
	String[] Packageitems = null;

	@Override
	public void onCreate(Bundle state)
	{
		super.onCreate(state);

		final String packageName = getIntent().getStringExtra(DDWidgetProvider.PACKAGE_STRING);
		int launchType = getIntent().getIntExtra("launchType", 0);

		sp = PreferenceManager.getDefaultSharedPreferences(this);

		if(packageName != null && isAppInstalled(packageName))
		{

			switch(launchType)
			{
				case Constants.LAUNCH_APP:
				{
					startApp(this, packageName);
					break;
				}
				case Constants.LAUNCH_APP_DETAILS:
				{
					startAppDetails(this, packageName);
					break;
				}
				case Constants.LAUNCH_UNINSTALL:
				{
					startUninstall(this, packageName);
					break;
				}
                case Constants.LAUNCH_CLEAR:
                {
                    startClearCache(this, packageName);
                    break;
                }
			}
		}
		else
		{
			Toast.makeText(getApplicationContext(), "Package is not installed", Toast.LENGTH_SHORT).show();

			AlertDialog.Builder builder = new AlertDialog.Builder(ClickHandlingActivity.this);
			builder.setTitle(getResources().getString(R.string.uninstalled));
			builder.setMessage(getResources().getString(R.string.package_does_not_exist));
			builder.setPositiveButton(getResources().getString(R.string.remove), new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialogInterface, int i)
				{
					new Database(getApplicationContext()).deleteAppFromDb(packageName);

					AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
					int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(getApplicationContext(), DDWidgetProvider.class));
					appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listView);

					finish();
				}
			});
			builder.setCancelable(true);
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				@Override
				public void onCancel(DialogInterface dialogInterface)
				{
					finish();
				}
			});

			AlertDialog alert = builder.create();
			alert.show();
		}
	}

	// Method to check whether the Facebook App is installed
	private boolean isAppInstalled(String uri)
	{
		PackageManager pm = getPackageManager();
		boolean app_installed;
		try
		{
			pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
			app_installed = true;
		}
		catch(PackageManager.NameNotFoundException e)
		{
			app_installed = false;
		}
		return app_installed;
	}

	public static void startApp(Activity activity, String packageName)
	{
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
		if(sp.getBoolean("showActivityChoice", false))
		{
			// Show the activity choice dialog
			Intent intent = new Intent(activity, ChooseActivityDialog.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
			intent.putExtra("packageName", packageName);
			activity.startActivity(intent);
			activity.finish();


		}else
		{
			// Launch the app
			try
			{
				Intent LaunchIntent = activity.getPackageManager()
						.getLaunchIntentForPackage(packageName);
				LaunchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
				LaunchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
						Intent.FLAG_ACTIVITY_CLEAR_TOP);
				activity.startActivity(LaunchIntent);
			}
			catch(NullPointerException e)
			{
				Toast.makeText(activity, activity.getString(
						R.string.no_main_activity_could_be_found), Toast.LENGTH_SHORT)
						.show();
				Intent intent = new Intent(activity, PrefActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
						Intent.FLAG_ACTIVITY_CLEAR_TOP);
				activity.startActivity(intent);
			}
			activity.finish();
		}
	}

	public static void startAppDetails(Activity activity, String packageName)
	{
		// Launch the app details settings screen for the app
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9)
		{
            intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + packageName));
		}
		else
		{
            // on FroYo and Eclair it's just horrible...
            final String APP_PKG_NAME_21 = "com.android.settings.ApplicationPkgName";
            final String APP_PKG_NAME_22 = "pkg";
            final String APP_DETAILS_PACKAGE_NAME = "com.android.settings";
            final String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";
            final String appPkgName = (Build.VERSION.SDK_INT == 8 ? APP_PKG_NAME_22 : APP_PKG_NAME_21);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName(APP_DETAILS_PACKAGE_NAME, APP_DETAILS_CLASS_NAME);
            intent.putExtra(appPkgName, packageName);
		}
        activity.startActivity(intent);

        if (Build.VERSION.SDK_INT >= 11) {
            activity.finish();
        }
	}
    public static void startClearCache(Activity activity, String packageName)
    {
        final Context context = activity.getApplicationContext();
        activity.finish();
        RootFeatures.clearCache(packageName, new RootFeatures.Listener() {

            @Override
            public void onFinished(boolean result) {
                if (result == false) {
                    Toast.makeText(context, "No root access available", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

	public static void startUninstall(Activity activity, String packageName)
	{
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        if(sp.getBoolean("rootQuickUninstall", true)) {
            final Context context = activity.getApplicationContext();
            activity.finish();
            RootFeatures.uninstall(packageName, new RootFeatures.Listener() {

                @Override
                public void onFinished(boolean result) {
                    if (result == false) {
                        Toast.makeText(context, "No root access available", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else {
            if (Build.VERSION.SDK_INT > 10)
            {
                try
                {
                    Uri packageUri = Uri.parse("package:" + packageName);
                    Intent uninstallIntent =
                            new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
                    uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    activity.startActivity(uninstallIntent);
                    activity.finish();
                }
                catch (ActivityNotFoundException e)
                {
                    Toast.makeText(activity, "Application cannot be uninstalled / possibly system app", Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Intent intent = new Intent(Intent.ACTION_DELETE);
                intent.setData(Uri.parse("package:" + packageName));
                activity.startActivity(intent);
            }
        }
	}



}
