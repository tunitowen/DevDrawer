package com.owentech.DevDrawer.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import com.owentech.DevDrawer.appwidget.DDWidgetProvider;

/**
 * Created with IntelliJ IDEA. User: owent Date: 29/01/2013 Time: 06:31 To
 * change this template use File | Settings | File Templates.
 */
public class ClickHandlingActivity extends Activity {

	CharSequence[] Packageitems = null;

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);

		final String packageName = getIntent().getStringExtra(
				DDWidgetProvider.PACKAGE_STRING);
		boolean appDetails = getIntent().getBooleanExtra("appDetails", false);

		if (packageName != null) {

			if (appDetails) {
				// Launch the app details settings screen for the app
				Intent i = new Intent(
						android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
				i.addCategory(Intent.CATEGORY_DEFAULT);
				i.setData(Uri.parse("package:" + packageName));
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);
			} else {

				try {
					List<String> adapter = getActivityList(packageName);
					Packageitems = adapter.toArray(new CharSequence[adapter
							.size()]);
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				AlertDialog.Builder alert = new AlertDialog.Builder(this);
				alert.setTitle("Choose:");
				alert.setCancelable(false);
				alert.setSingleChoiceItems(Packageitems, -1,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int item) {
								Intent intent = new Intent();
								intent.setComponent(new ComponentName(
										packageName, Packageitems[item]
												.toString()));
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(intent);
								finish();
							}
						});

				alert.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								finish();
							}
						});

				AlertDialog ad = alert.create();
				ad.show();
			}
		}
	}

	private List<String> getActivityList(String packageName)
			throws NameNotFoundException {

		PackageManager pm = this.getPackageManager();

		List<String> adapter = new ArrayList<String>();

		PackageInfo info = pm.getPackageInfo(packageName,
				PackageManager.GET_ACTIVITIES);
		ActivityInfo[] list = info.activities;
		for (ActivityInfo string : list) {
			adapter.add(string.name.toString());
		}

		return adapter;
	}

}
