package com.owentech.DevDrawer.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;
import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.appwidget.DDWidgetProvider;
import com.owentech.DevDrawer.utils.Constants;
import com.owentech.DevDrawer.utils.Database;

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
					if(sp.getBoolean("showActivityChoice", false))
					{
						// Show the activity choice dialog
						Intent intent = new Intent(this, ChooseActivityDialog.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
						intent.putExtra("packageName", packageName);
						startActivity(intent);


					}else
					{
						// Launch the app
						try
						{
							Intent LaunchIntent = getApplicationContext().getPackageManager()
									.getLaunchIntentForPackage(packageName);
							LaunchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
							LaunchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
									Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(LaunchIntent);
						}
						catch(NullPointerException e)
						{


							Toast.makeText(this, this.getString(
									R.string.no_main_activity_could_be_found), Toast.LENGTH_SHORT)
									.show();
							Intent intent = new Intent(this, PrefActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
									Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
						}
						finish();
					}
					break;
				}
				case Constants.LAUNCH_APP_DETAILS:
				{
					// Launch the app details settings screen for the app
					Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
					i.addCategory(Intent.CATEGORY_DEFAULT);
					i.setData(Uri.parse("package:" + packageName));
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(i);
					finish();
					break;
				}
				case Constants.LAUNCH_UNINSTALL:
				{

					Uri packageUri = Uri.parse("package:" + packageName);
					Intent uninstallIntent =
							new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
					uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(uninstallIntent);
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
		boolean app_installed = false;
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


}
