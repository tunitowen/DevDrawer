package com.owentech.DevDrawer.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: owent
 * Date: 29/01/2013
 * Time: 07:08
 * To change this template use File | Settings | File Templates.
 */
public class Database {

    SQLiteDatabase db;
    Context ctx;

	public static int NOT_FOUND = 1000000;

    private static final String TAG = "DevDrawer-Database";

    public Database(Context ctx)
    {
        this.ctx = ctx;
    }

    ///////////////////////////////////
	// Method to connect to database
    ///////////////////////////////////
    public void connectDB()
    {
        db = ctx.openOrCreateDatabase("DevDrawer.db",
                SQLiteDatabase.CREATE_IF_NECESSARY, null);
    }

    ////////////////////////////////////////
	// Method to close database connection
    ////////////////////////////////////////
    public void closeDB()
    {
        db.close();
    }

    /////////////////////////////////////////
	// Method to create tables in database
    /////////////////////////////////////////
    public void createTables()
    {
        connectDB();

        // create tables
        String CREATE_TABLE_FILTER = "CREATE TABLE IF NOT EXISTS devdrawer_filter ("
                + "id INTEGER PRIMARY KEY, package TEXT);";
        db.execSQL(CREATE_TABLE_FILTER);

        String CREATE_TABLE_APPS = "CREATE TABLE IF NOT EXISTS devdrawer_app ("
                + "id INTEGER PRIMARY KEY, package TEXT, filterid INTEGER);";
        db.execSQL(CREATE_TABLE_APPS);

		String CREATE_TABLE_LOCALES = "CREATE TABLE IF NOT EXISTS devdrawer_locales ("
				+ "name TEXT);";
		db.execSQL(CREATE_TABLE_LOCALES);

        closeDB();

    }

    // ////////////////////////////////////////////////
    // Method to add an entry into the filter table
    // ///////////////////////////////////////////////
    public void addFilterToDatabase(String p)
    {
        // connect
        connectDB();

        // add accident entry
        String packageInsertQuery = "INSERT INTO 'devdrawer_filter' "
                + "(package)" + "VALUES" + "('" + p + "');";

        db.execSQL(packageInsertQuery);



        // close
        closeDB();
    }

    // ///////////////////////////////////////////////
    // Method to add package to installed apps table
    // ///////////////////////////////////////////////
    public void addAppToDatabase(String p, String filterId)
    {
        // connect
        connectDB();

        // add accident entry
        String packageInsertQuery = "INSERT INTO 'devdrawer_app' "
                + "(package, filterid)" + "VALUES" + "('" + p + "', " + filterId + ");";

        db.execSQL(packageInsertQuery);

        // close
        closeDB();
    }

    // ////////////////////////////////////////////////////
    // Method to remove an entry from the filters tables
    // ////////////////////////////////////////////////////
    public void removeFilterFromDatabase(String i)
    {
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
	public void removeAppFromDatabase(String i)
	{
		// connect
		connectDB();

		// add accident entry
		String packageDeleteQuery = "DELETE FROM 'devdrawer_app' WHERE filterid = '" + i + "'";

		db.execSQL(packageDeleteQuery);

		// close
		closeDB();
	}

    //////////////////////////////////////////////////////
    // Method to get all the entries in the filter table
    //////////////////////////////////////////////////////
    public List<PackageCollection> getAllFiltersInDatabase()
    {

        connectDB();

        Cursor getAllCursor = db.query("devdrawer_filter", null, null, null, null, null, null, null);

		List<PackageCollection> packageCollections = new ArrayList<PackageCollection>();

        getAllCursor.moveToFirst();

        while(!getAllCursor.isAfterLast())
        {

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
    public String[] getAllAppsInDatabase(String order)
    {
        String[] packages;

        connectDB();

        Cursor getAllCursor = db.query("devdrawer_app", null, null, null, null, null,
										(order.equals(Constants.ORDER_ORIGINAL)) ? null : "package ASC",
										null);

        Log.d("DATABASE", "getAllAppsInDatabase: " + Integer.toString(getAllCursor.getCount()) );

        getAllCursor.moveToFirst();

        packages = new String[getAllCursor.getCount()];

        int i=0;

        while(!getAllCursor.isAfterLast())
        {
            packages[i] = getAllCursor.getString(1);
            i++;
            getAllCursor.moveToNext();
        }

        getAllCursor.close();
        closeDB();

		if (order.equals(Constants.ORDER_ORIGINAL))
		{
			Collections.reverse(Arrays.asList(packages));
		}

        return packages;

    }

    // ////////////////////////////////////////////////////
    // Method to get a count of rows in the filter table
    // ////////////////////////////////////////////////////
    public int getFiltersCount()
    {
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
	public boolean doesFilterExist(String s)
	{
		// connect
		connectDB();

		Cursor countCursor = db.rawQuery("SELECT count(*) FROM devdrawer_filter WHERE package = '" + s + "'", null);

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
	public int getAppsCount()
	{
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
	public boolean doesAppExistInDb(String s)
	{
		connectDB();

		Cursor cursor = db.query("devdrawer_app", null, "package = '" + s + "'", null, null, null, null, null);
		int count = cursor.getCount();
		cursor.close();
		closeDB();

		if (cursor.getCount() == 0)
			return false;
		else
			return true;

	}

	////////////////////////////////////////////////////////////////
	// Method to delete a package from the installed package table
	////////////////////////////////////////////////////////////////
	public void deleteAppFromDb(String s)
	{
		connectDB();
		db.execSQL("DELETE FROM devdrawer_app WHERE package ='" + s + "'");
		closeDB();
	}

    /////////////////////////////////////////////////////////////////////
    // Method to parse each row and return if the new package matches
    /////////////////////////////////////////////////////////////////////
	// TODO: Make this work for exact package name
    public int parseAndMatch(String p)
    {

		int match = NOT_FOUND;

        connectDB();

        Cursor getAllCursor = db.query("devdrawer_filter", null, null, null, null, null, null, null);

        getAllCursor.moveToFirst();

        while (!getAllCursor.isAfterLast())
        {

            String packageFilter = getAllCursor.getString(1).toLowerCase();

            if (packageFilter.contains("*"))
            {
                if (p.toLowerCase().startsWith(packageFilter.toLowerCase().substring(0, packageFilter.indexOf("*"))))
                    match = Integer.valueOf(getAllCursor.getString(0));

            }
            else
            {
                if (p.toLowerCase().equals(packageFilter.toLowerCase()))
					match = Integer.valueOf(getAllCursor.getString(0));

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
	public void amendFilterEntryTo(String id, String newString)
	{
		connectDB();
		db.execSQL("UPDATE devdrawer_filter SET package='" + newString + "' WHERE id ='" + id + "'");
		closeDB();
	}

	///////////////////////////////
	// Method to add all locales
	///////////////////////////////
	public void addLocale(String localeDescriptor)
	{
		connectDB();

		db.execSQL("INSERT INTO devdrawer_locales (name) VALUES ('" + localeDescriptor + "');");

		closeDB();
	}

	public List<String> getLocales()
	{
		connectDB();

		List<String> list = new ArrayList<String>();

		Cursor getAllCursor = db.query("devdrawer_locales", null, null, null, null, null, "name ASC", null);

		if (getAllCursor.getCount() != 0)
		{
			getAllCursor.moveToFirst();

			while(!getAllCursor.isAfterLast())
			{
				list.add(getAllCursor.getString(0));
				getAllCursor.moveToNext();
			}
		}

		return list;
	}

	public void deleteLocale(String localeDescriptor)
	{
		connectDB();

		db.execSQL("DELETE FROM devdrawer_locales WHERE name = '" + localeDescriptor + "';");

		closeDB();
	}

}
