package com.owentech.DevDrawer.utils;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.owentech.DevDrawer.appwidget.DDWidgetProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Database {

    SQLiteDatabase db;
    Context ctx;
    private static Database instance;

    public static Database getInstance(Context context){
        if (instance == null){
            instance = new Database(context);
        }
        return instance;
    }

    public static int NOT_FOUND = 1000000;

    private static final String TAG = "DevDrawer-Database";

    public Database(Context ctx) {
        this.ctx = ctx;

        int[] appWidgetIds = AppWidgetUtil.findAppWidgetIds(ctx);
        if (appWidgetIds.length > 0 && !PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean("HASMULTIPLEWIDGETS", false)) {
            Toast.makeText(ctx, "Please remove and re-add your widgets", Toast.LENGTH_LONG).show();
            dropTables();
            PreferenceManager.getDefaultSharedPreferences(ctx).edit().putBoolean("HASMULTIPLEWIDGETS", true).commit();
        }
        createTables();
    }

    ///////////////////////////////////
    // Method to connect to database
    ///////////////////////////////////
    @SuppressWarnings("Deprecation")
    public void connectDB() {
        db = ctx.openOrCreateDatabase("DevDrawer.db", 0, null);
    }

    ////////////////////////////////////////
    // Method to close database connection
    ////////////////////////////////////////
    public void closeDB() {
        db.close();
    }

    public void createTables() {
        connectDB();

        // create tables
        String CREATE_TABLE_FILTER = "CREATE TABLE IF NOT EXISTS devdrawer_filter ("
                + "id INTEGER PRIMARY KEY, package TEXT, widgetid INTEGER);";
        db.execSQL(CREATE_TABLE_FILTER);

        String CREATE_TABLE_APPS = "CREATE TABLE IF NOT EXISTS devdrawer_app ("
                + "id INTEGER PRIMARY KEY, package TEXT, filterid INTEGER, widgetid INTEGER);";
        db.execSQL(CREATE_TABLE_APPS);

        String CREATE_TABLE_WIDGETS = "CREATE TABLE IF NOT EXISTS devdrawer_widgets (id INTEGER PRIMARY KEY, name TEXT);";
        db.execSQL(CREATE_TABLE_WIDGETS);

        closeDB();
    }

    public void dropTables() {
        connectDB();

        db.execSQL("DROP TABLE IF EXISTS devdrawer_filter");
        db.execSQL("DROP TABLE IF EXISTS devdrawer_app");
        db.execSQL("DROP TABLE IF EXISTS devdrawer_locales");
        db.execSQL("DROP TABLE IF EXISTS devdrawer_widgets");

        closeDB();
    }


    public void addWidgetToDatabase(int widgetId, String name) {
        connectDB();

        String insertQuery = "INSERT INTO 'devdrawer_widgets' (id, name) VALUES (" + widgetId + ", '" + name + "')";
        db.execSQL(insertQuery);

        closeDB();
    }

    public void renameWidget(int widgetId, String name) {
        connectDB();
        db.execSQL("UPDATE devdrawer_widgets SET name='" + name.replace("'","''") + "' WHERE id ='" + widgetId + "'");
        closeDB();
    }

    public void removeWidgetFromDatabase(int widgetId) {
        connectDB();

        String query = "DELETE FROM 'devdrawer_widgets' WHERE id = " + widgetId;
        db.execSQL(query);

        query = "DELETE FROM 'devdrawer_filter' WHERE widgetId = " + widgetId;
        db.execSQL(query);

        query = "DELETE FROM 'devdrawer_app' WHERE widgetId = " + widgetId;
        db.execSQL(query);

        closeDB();
    }

    public SparseArray<String> getWidgetNames(Context context) {
        SparseArray<String> result = new SparseArray<String>();

        connectDB();
        Cursor cursor = db.query("devdrawer_widgets", null, null, null, null, null, null, null);
        cursor.moveToFirst();

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, DDWidgetProvider.class));

        while (!cursor.isAfterLast()) {

            String name = cursor.getString(1);
            if (name == null || name.length() == 0){
                name = AppConstants.UNNAMED;
            }

            boolean exists = false;
            for (int i : ids){
                if (cursor.getInt(0) == i){
                    exists = true;
                }
            }

            if (exists) {
                result.put(cursor.getInt(0), name);
            }
            else{
                removeWidgetFromDatabase(cursor.getInt(0));
            }
            cursor.moveToNext();
        }

        cursor.close();
        closeDB();
        return result;
    }

    // ////////////////////////////////////////////////
    // Method to add an entry into the filter table
    // ///////////////////////////////////////////////
    public void addFilterToDatabase(String p, int widgetId) {
        // connect
        connectDB();

        // add accident entry
        String packageInsertQuery = "INSERT INTO 'devdrawer_filter' (package, widgetid)" + "VALUES" + "('" + p + "', " + widgetId + ");";

        db.execSQL(packageInsertQuery);

        // close
        closeDB();
    }

    // ///////////////////////////////////////////////
    // Method to add package to installed apps table
    // ///////////////////////////////////////////////
    public void addAppToDatabase(String p, String filterId, int widgetId) {
        // connect
        connectDB();

        // add accident entry
        String packageInsertQuery = "INSERT INTO 'devdrawer_app' "
                + "(package, filterid, widgetid)" + "VALUES" + "('" + p + "', " + filterId + ", " + String.valueOf(widgetId) + ");";

        db.execSQL(packageInsertQuery);

        // close
        closeDB();
    }

    // ////////////////////////////////////////////////////
    // Method to remove an entry from the filters tables
    // ////////////////////////////////////////////////////
    public void removeFilterFromDatabase(String i) {
        // connect
        connectDB();

        // add accident entry
        String packageDeleteQuery = "DELETE FROM 'devdrawer_filter' WHERE id = '" + i + "'";

        db.execSQL(packageDeleteQuery);

        // close
        closeDB();
    }

    // ////////////////////////////////////////////////////
    // Method to remove an entry from the filters tables
    // ////////////////////////////////////////////////////
    public void removeAppFromDatabase(String filterId) {
        // connect
        connectDB();

        // add accident entry
        String packageDeleteQuery = "DELETE FROM 'devdrawer_app' WHERE filterid = '" + filterId + "'";

        db.execSQL(packageDeleteQuery);

        // close
        closeDB();
    }

    //////////////////////////////////////////////////////
    // Method to get all the entries in the filter table
    //////////////////////////////////////////////////////
    public List<PackageCollection> getAllFiltersInDatabase() {

        connectDB();

        Cursor getAllCursor = db.query("devdrawer_filter", null, null, null, null, null, null, null);

        List<PackageCollection> packageCollections = new ArrayList<PackageCollection>();

        getAllCursor.moveToFirst();

        while (!getAllCursor.isAfterLast()) {

            packageCollections.add(new PackageCollection(getAllCursor.getString(0), getAllCursor.getString(1)));
            getAllCursor.moveToNext();
        }

        getAllCursor.close();
        closeDB();

        return packageCollections;

    }

    //////////////////////////////////////////////////////
    // Method to get all the entries in the filter table for given widgetId
    //////////////////////////////////////////////////////
    public List<PackageCollection> getAllFiltersInDatabase(int widgetId) {

        connectDB();

        Cursor getAllCursor = db.query("devdrawer_filter", null, "widgetid = " + widgetId, null, null, null, null, null);

        List<PackageCollection> packageCollections = new ArrayList<PackageCollection>();

        getAllCursor.moveToFirst();

        while (!getAllCursor.isAfterLast()) {

            packageCollections.add(new PackageCollection(getAllCursor.getString(0), getAllCursor.getString(1)));
            getAllCursor.moveToNext();
        }

        getAllCursor.close();
        closeDB();

        return packageCollections;

    }

    ////////////////////////////////////////////////////////////////
    // Method to get all the packages in the installed apps table
    ////////////////////////////////////////////////////////////////
    public String[] getAllAppsInDatabase(String order) {
        String[] packages;

        connectDB();
        Cursor getAllCursor = db.query("devdrawer_app", null, null, null, null, null, (order.equals(AppConstants.ORDER_ORIGINAL)) ? null : "package ASC", null);
        getAllCursor.moveToFirst();
        packages = new String[getAllCursor.getCount()];

        int i = 0;
        while (!getAllCursor.isAfterLast()) {
            packages[i] = getAllCursor.getString(1);
            i++;
            getAllCursor.moveToNext();
        }

        getAllCursor.close();
        closeDB();

        if (order.equals(AppConstants.ORDER_ORIGINAL)) {
            Collections.reverse(Arrays.asList(packages));
        }

        return packages;
    }

    ////////////////////////////////////////////////////////////////
    // Method to get all the packages in the installed apps table for given widgetId
    ////////////////////////////////////////////////////////////////
    public String[] getAllAppsInDatabase(String order, int widgetId) {
        String[] packages;

        connectDB();
        Cursor getAllCursor = db.query("devdrawer_app", null, "widgetid = " + widgetId, null, null, null, (order.equals(AppConstants.ORDER_ORIGINAL)) ? null : "package ASC", null);
        getAllCursor.moveToFirst();
        packages = new String[getAllCursor.getCount()];

        int i = 0;
        while (!getAllCursor.isAfterLast()) {
            packages[i] = getAllCursor.getString(1);
            i++;
            getAllCursor.moveToNext();
        }

        getAllCursor.close();
        closeDB();

        if (order.equals(AppConstants.ORDER_ORIGINAL)) {
            Collections.reverse(Arrays.asList(packages));
        }

        return packages;
    }



    // ////////////////////////////////////////////////////
    // Method to get a count of rows in the filter table
    // ////////////////////////////////////////////////////
    public int getFiltersCount() {
        // connect
        connectDB();

        Cursor countCursor = db.rawQuery("SELECT count(*) FROM devdrawer_filter", null);

        // get number of rows
        countCursor.moveToFirst();
        int count = countCursor.getInt(0);
        countCursor.close();

        // close
        closeDB();

        return count;

    }

    ///////////////////////////////////////////////////////////////
    // Method to determine whether the new filter already exists
    ///////////////////////////////////////////////////////////////
    public boolean doesFilterExist(String s, int appWidgetId) {
        // connect
        connectDB();

        Cursor countCursor = db.rawQuery("SELECT count(*) FROM devdrawer_filter WHERE package = '" + s + "' AND widgetid = " + appWidgetId, null);

        // get number of rows
        countCursor.moveToFirst();
        int count = countCursor.getInt(0);
        countCursor.close();

        // close
        closeDB();

        if (count == 0)
            return false;
        else
            return true;
    }

    // ////////////////////////////////////////////////////////////
    // Method to get a count of rows in the installed apps tables
    // ////////////////////////////////////////////////////////////
    public int getAppsCount() {
        // connect
        connectDB();

        Cursor countCursor = db.rawQuery("SELECT count(*) FROM devdrawer_app", null);

        // get number of rows
        countCursor.moveToFirst();
        int count = countCursor.getInt(0);
        countCursor.close();

        // close
        closeDB();

        return count;

    }

    /////////////////////////////////////////////////////////////////////////////////////////
    // Method to check whether the app being install exists in the installed package table
    /////////////////////////////////////////////////////////////////////////////////////////
    public boolean doesAppExistInDb(String s) {
        connectDB();

        Cursor cursor = db.query("devdrawer_app", null, "package = '" + s + "'", null, null, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        closeDB();

        return count != 0;
    }

    ////////////////////////////////////////////////////////////////
    // Method to delete a package from the installed package table
    ////////////////////////////////////////////////////////////////
    public void deleteAppFromDb(String packageName) {
        connectDB();
        db.execSQL("DELETE FROM devdrawer_app WHERE package ='" + packageName + "'");
        closeDB();
    }

    /////////////////////////////////////////////////////////////////////
    // Method to parse each row and return if the new package matches
    /////////////////////////////////////////////////////////////////////
    public int parseAndMatch(String p) {

        int match = NOT_FOUND;

        connectDB();
        Cursor getAllCursor = db.query("devdrawer_filter", null, null, null, null, null, null, null);
        getAllCursor.moveToFirst();

        while (!getAllCursor.isAfterLast()) {
            String packageFilter = getAllCursor.getString(1).toLowerCase();

            if (packageFilter.contains("*")) {
                if (p.toLowerCase().startsWith(packageFilter.toLowerCase().substring(0, packageFilter.indexOf("*"))))
                    match = Integer.valueOf(getAllCursor.getString(0));
            } else {
                if (p.toLowerCase().equals(packageFilter.toLowerCase()))
                    match = Integer.valueOf(getAllCursor.getString(0));
            }
            getAllCursor.moveToNext();
        }

        getAllCursor.close();
        closeDB();
        return match;
    }

    /////////////////////////////////////////////////////////////////////
    // Method to parse each row and return if the new package matches
    /////////////////////////////////////////////////////////////////////
    public int parseAndMatch(String p, int widgetId) {

        int match = NOT_FOUND;

        connectDB();
        Cursor getAllCursor = db.query("devdrawer_filter", null, "widgetid = " + widgetId, null, null, null, null, null);
        getAllCursor.moveToFirst();

        while (!getAllCursor.isAfterLast()) {
            String packageFilter = getAllCursor.getString(1).toLowerCase();

            if (packageFilter.contains("*")) {
                if (p.toLowerCase().startsWith(packageFilter.toLowerCase().substring(0, packageFilter.indexOf("*")))) {
                    match = Integer.valueOf(getAllCursor.getString(0));
                }
            }
            else {
                if (p.toLowerCase().equals(packageFilter.toLowerCase())) {
                    match = Integer.valueOf(getAllCursor.getString(0));
                }
            }
            getAllCursor.moveToNext();
        }

        getAllCursor.close();
        closeDB();

        return match;
    }

    ///////////////////////////////////
    // Method to amend a filter entry
    ///////////////////////////////////
    public void amendFilterEntryTo(String id, String newString) {
        connectDB();
        db.execSQL("UPDATE devdrawer_filter SET package='" + newString + "' WHERE id ='" + id + "'");
        closeDB();
    }
}
