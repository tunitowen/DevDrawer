package com.owentech.DevDrawer;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.facebook.stetho.Stetho;
import com.owentech.DevDrawer.data.OpenHelper;
import com.owentech.DevDrawer.di.ApplicationComponent;
import com.owentech.DevDrawer.di.ApplicationModule;
import com.owentech.DevDrawer.di.DaggerApplicationComponent;
import com.owentech.DevDrawer.utils.AppConstants;
import com.owentech.DevDrawer.utils.AppWidgetUtil;
import com.owentech.DevDrawer.utils.Database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

public class DevDrawerApplication extends Application {

    @Inject Database database;
    ApplicationComponent applicationComponent;
    private static DevDrawerApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Stetho.initializeWithDefaults(this);
        OpenHelper openHelper = new OpenHelper(this);
        SQLiteDatabase sqLiteDatabase = openHelper.getWritableDatabase();
        openHelper.onCreate(sqLiteDatabase);

        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();

        applicationComponent.inject(this);

        cleanWidgetsTable();
    }

    private void cleanWidgetsTable(){
        List<Integer> widgets = new ArrayList<>();
        int[] activeWidgets = AppWidgetUtil.findAppWidgetIds(this);
        for (int activeWidget : activeWidgets) {
            widgets.add(activeWidget);
        }
        widgets.add(AppConstants.NOTIFICATION);
        widgets.add(AppConstants.SHORTCUT);

        database.cleanWidgets(widgets);

    }

    public static DevDrawerApplication getInstance(){
        return instance;
    }

    public ApplicationComponent getApplicationComponent() {
        return this.applicationComponent;
    }
}
