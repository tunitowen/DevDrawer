package com.owentech.DevDrawer;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.facebook.stetho.Stetho;
import com.owentech.DevDrawer.data.OpenHelper;
import com.owentech.DevDrawer.di.ApplicationComponent;
import com.owentech.DevDrawer.di.ApplicationModule;
import com.owentech.DevDrawer.di.DaggerApplicationComponent;

public class DevDrawerApplication extends Application {

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
    }

    public static DevDrawerApplication getInstance(){
        return instance;
    }

    public ApplicationComponent getApplicationComponent() {
        return this.applicationComponent;
    }
}
