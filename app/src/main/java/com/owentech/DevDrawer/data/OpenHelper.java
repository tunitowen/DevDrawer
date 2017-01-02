package com.owentech.DevDrawer.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.owentech.DevDrawer.data.model.AppModel;
import com.owentech.DevDrawer.data.model.FilterModel;
import com.owentech.DevDrawer.data.model.WidgetModel;

import javax.inject.Inject;

public class OpenHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "DevDrawer.db";
    public static final int DB_VERSION = 1;

    @Inject
    public OpenHelper(Context context) {
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