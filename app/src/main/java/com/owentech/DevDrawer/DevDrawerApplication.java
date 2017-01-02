package com.owentech.DevDrawer;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.facebook.stetho.Stetho;
import com.owentech.DevDrawer.data.OpenHelper;

public class DevDrawerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        OpenHelper openHelper = new OpenHelper(this);
        SQLiteDatabase sqLiteDatabase = openHelper.getWritableDatabase();
        openHelper.onCreate(sqLiteDatabase);
    }
}
