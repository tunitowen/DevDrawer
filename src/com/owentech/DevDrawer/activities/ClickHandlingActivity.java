package com.owentech.DevDrawer.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.owentech.DevDrawer.appwidget.DDWidgetProvider;
import com.owentech.DevDrawer.utils.Constants;

/**
 * Created with IntelliJ IDEA.
 * User: owent
 * Date: 29/01/2013
 * Time: 06:31
 * To change this template use File | Settings | File Templates.
 */
public class ClickHandlingActivity extends Activity {

	SharedPreferences sp;
	CharSequence[] Packageitems = null;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        final String packageName = getIntent().getStringExtra(DDWidgetProvider.PACKAGE_STRING);
        int launchType = getIntent().getIntExtra("launchType", 0);

		sp = PreferenceManager.getDefaultSharedPreferences(this);

        if (packageName != null) {

            switch (launchType)
            {
                case Constants.LAUNCH_APP:
                {
					if (sp.getBoolean("showActivityChoice", false))
					{
						// Show the activity choice dialog
						try
						{
							List<String> adapter = getActivityList(packageName);
							Packageitems = adapter.toArray(new CharSequence[adapter
									.size()]);
						}
						catch (PackageManager.NameNotFoundException e)
						{
							e.printStackTrace();
						}

						AlertDialog.Builder alert = new AlertDialog.Builder(this);
						alert.setTitle("Choose:");
						alert.setCancelable(false);
						alert.setSingleChoiceItems(Packageitems, -1,
								new DialogInterface.OnClickListener()
								{
									@Override
                                    public void onClick(DialogInterface dialog, int item) {
										Intent intent = new Intent();
										intent.setComponent(new ComponentName(
												packageName, Packageitems[item]
												.toString()));
										intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
										intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
										startActivity(intent);
										dialog.dismiss();
										finish();
									}
								});

						alert.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener()
								{
									@Override
                                    public void onClick(DialogInterface dialog, int id) {
										dialog.cancel();
										finish();
									}
								});

						AlertDialog ad = alert.create();
						ad.show();

					}
					else
					{
						// Launch the app
						Intent LaunchIntent = getApplicationContext().getPackageManager()
								.getLaunchIntentForPackage(packageName);
						LaunchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
						LaunchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(LaunchIntent);
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

    }

	private List<String> getActivityList(String packageName)
			throws PackageManager.NameNotFoundException {

		PackageManager pm = this.getPackageManager();

		List<String> adapter = new ArrayList<String>();

		PackageInfo info = pm.getPackageInfo(packageName,
				PackageManager.GET_ACTIVITIES);
		ActivityInfo[] list = info.activities;

                for (ActivityInfo activity : list) {
                    if (activity.exported) {
                        adapter.add(activity.name.toString());
                    }
                }

		return adapter;
	}

}
