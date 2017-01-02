package com.owentech.DevDrawer.receivers;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.di.DaggerDatabaseComponent;
import com.owentech.DevDrawer.di.DatabaseModule;
import com.owentech.DevDrawer.utils.AppConstants;
import com.owentech.DevDrawer.utils.AppWidgetUtil;
import com.owentech.DevDrawer.utils.Database;
import com.owentech.DevDrawer.utils.NotificationHelper;
import com.owentech.DevDrawer.utils.RxUtils;

import javax.inject.Inject;

public class AppInstalledReceiver extends BroadcastReceiver {

    @Inject
    Database database;

    @Override
    public void onReceive(Context context, Intent intent) {
        DaggerDatabaseComponent.builder()
                .databaseModule(new DatabaseModule(context))
                .build().inject(this);

        // New app has been installed, check and add to the database / widget
        String newPackage = intent.getData().getSchemeSpecificPart();

        if (database.getFiltersCount() != 0) {
            int[] appWidgetIds = AppWidgetUtil.findAppWidgetIds(context);
            for (int appWidgetId : appWidgetIds) {
                long match = database.parseAndMatch(newPackage, appWidgetId);
                if (match != Database.NOT_FOUND) {
                    RxUtils.fromCallable(database.addAppToDatabase(intent.getData().getSchemeSpecificPart(), match, appWidgetId))
                            .subscribe();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listView);
                    }
                }
            }

            long match = database.parseAndMatch(newPackage, AppConstants.NOTIFICATION);
            if (match != Database.NOT_FOUND) {
                RxUtils.fromCallable(database.addAppToDatabase(intent.getData().getSchemeSpecificPart(), match, AppConstants.NOTIFICATION))
                        .subscribe();
                NotificationHelper.showNotification(context, newPackage, match);
            }
        }
    }
}
