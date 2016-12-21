package com.owentech.DevDrawer.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.owentech.DevDrawer.data.model.AppModel;
import com.owentech.DevDrawer.data.model.FilterModel;
import com.owentech.DevDrawer.data.model.WidgetModel;

public class OpenHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "sqldelight.db";
    public static final int DB_VERSION = 1;

    private static OpenHelper instance;

    public static OpenHelper getInstance(Context context) {
        if (null == instance) {
            instance = new OpenHelper(context);
        }
        return instance;
    }

    private OpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(WidgetModel.CREATE_TABLE);
        db.execSQL(FilterModel.CREATE_TABLE);
        db.execSQL(AppModel.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // upgrade logic
    }
}