package com.owentech.DevDrawer.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        String packageName = getIntent().getStringExtra(DDWidgetProvider.PACKAGE_STRING);
        int launchType = getIntent().getIntExtra("launchType", 0);

        if (packageName != null) {

            switch (launchType)
            {
                case Constants.LAUNCH_APP:
                {
                    // Launch the app
                    Intent LaunchIntent = getApplicationContext().getPackageManager()
                            .getLaunchIntentForPackage(packageName);
                    LaunchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    LaunchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(LaunchIntent);
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

        finish();
    }

}
