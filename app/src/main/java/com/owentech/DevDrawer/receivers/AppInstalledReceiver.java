package com.owentech.DevDrawer.receivers;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.utils.AppConstants;
import com.owentech.DevDrawer.utils.AppWidgetUtil;
import com.owentech.DevDrawer.utils.Database;
import com.owentech.DevDrawer.utils.NotificationHelper;
import com.owentech.DevDrawer.utils.RxUtils;

/**
 * Created with IntelliJ IDEA.
 * User: owent
 * Date: 25/01/2013
 * Time: 19:33
 * To change this template use File | Settings | File Templates.
 */

public class AppInstalledReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // New app has been installed, check and add to the database / widget
        String newPackage = intent.getData().getSchemeSpecificPart();

        if (Database.getInstance(context).getFiltersCount() != 0) {
            int[] appWidgetIds = AppWidgetUtil.findAppWidgetIds(context);
            for (int appWidgetId : appWidgetIds) {
                long match = Database.getInstance(context).parseAndMatch(newPackage, appWidgetId);
                if (match != Database.NOT_FOUND) {
                    RxUtils.backgroundSingleFromCallable(Database.getInstance(context).addAppToDatabase(intent.getData().getSchemeSpecificPart(), match, appWidgetId))
                            .subscribe();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listView);
                    }
                }
            }

            long match = Database.getInstance(context).parseAndMatch(newPackage, AppConstants.NOTIFICATION);
            if (match != Database.NOT_FOUND) {
                RxUtils.backgroundSingleFromCallable(Database.getInstance(context).addAppToDatabase(intent.getData().getSchemeSpecificPart(), match, AppConstants.NOTIFICATION))
                        .subscribe();
                NotificationHelper.showNotification(context, newPackage, match);
            }
        }
    }
}
