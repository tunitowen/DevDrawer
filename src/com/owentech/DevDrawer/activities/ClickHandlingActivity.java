package com.owentech.DevDrawer.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.owentech.DevDrawer.appwidget.DDWidgetProvider;

/**
 * Created with IntelliJ IDEA.
 * User: owent
 * Date: 29/01/2013
 * Time: 06:31
 * To change this template use File | Settings | File Templates.
 */
public class ClickHandlingActivity extends Activity {

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        String packageName = getIntent().getStringExtra(DDWidgetProvider.PACKAGE_STRING);
        boolean appDetails = getIntent().getBooleanExtra("appDetails", false);

        if (packageName != null) {

            if (appDetails)
            {
				// Launch the app details settings screen for the app
                Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                i.addCategory(Intent.CATEGORY_DEFAULT);
                i.setData(Uri.parse("package:" + packageName));
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
            else
            {
				// Launch the app
                Intent LaunchIntent = getApplicationContext().getPackageManager()
                        .getLaunchIntentForPackage(packageName);
				LaunchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
				LaunchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(LaunchIntent);
            }
        }

        finish();
    }

}
