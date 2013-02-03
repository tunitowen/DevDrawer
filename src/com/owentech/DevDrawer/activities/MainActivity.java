package com.owentech.DevDrawer.activities;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.owentech.DevDrawer.*;
import com.owentech.DevDrawer.appwidget.DDWidgetProvider;
import com.owentech.DevDrawer.utils.Constants;
import com.owentech.DevDrawer.utils.Database;
import com.owentech.DevDrawer.adapters.FilterListAdapter;
import com.owentech.DevDrawer.utils.PackageCollection;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity
{

	Database database;

	ImageView addButton;
	EditText addPackageEditText;
	FilterListAdapter lviewAdapter;
	ListView listView;

	@Override
	public void onCreate(Bundle state)
	{
		super.onCreate(state);

		setContentView(R.layout.main);

		// Set up ActionBar to use custom view (Robot Light font)
		getActionBar().setDisplayShowTitleEnabled(false);
		LayoutInflater inflater = LayoutInflater.from(this);
		View customView = inflater.inflate(R.layout.custom_ab_title, null);
		getActionBar().setCustomView(customView);
		getActionBar().setDisplayShowCustomEnabled(true);

		// Create the database tables
		database = new Database(this);
		database.createTables();

		// Setup view components
		addButton = (ImageView) findViewById(R.id.addButton);
		addPackageEditText = (EditText) findViewById(R.id.addPackageEditText);
		listView = (ListView) findViewById(R.id.packagesListView);

		// Update the ListView from the database
		updateListView();

		addButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{

				if(addPackageEditText.getText().length() != 0) // Check something entered
				{
					// Check filter doesn't exist
					if(!database.doesFilterExist(addPackageEditText.getText().toString()))
					{
						// Add the filter to the database
						database.addFilterToDatabase(addPackageEditText.getText().toString());

						// Check existing apps and add to installed apps table if they match new filter
						getAllAppsInstalledAndAdd(addPackageEditText.getText().toString());

						addPackageEditText.setText("");
						updateListView();

					}
					else
					{
						Toast.makeText(getApplicationContext(), "Filter already exists", Toast.LENGTH_SHORT).show();
					}
				}

			}
		});


	}

	// Method to re-populate the ListView
	public void updateListView()
	{
		lviewAdapter = null;
		lviewAdapter = new FilterListAdapter(this, database.getAllFiltersInDatabase());
		listView.setAdapter(lviewAdapter);
		lviewAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		// Catch the return from the EditDialog
		if(resultCode == Constants.EDIT_DIALOG_CHANGE)
		{
			Bundle bundle = data.getExtras();

			Database database = new Database(this);
			database.amendFilterEntryTo(bundle.getString("id"), bundle.getString("newText"));
			updateListView();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(0, 0, 0, "Settings").setIcon(R.drawable.ic_action_settings_white).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item)
	{
		switch(item.getItemId())
		{
			case 0:
			{
				Toast.makeText(this, "Import/Export options coming soon..", Toast.LENGTH_SHORT).show();
			}
		}
		return false;
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		// this is called to prevent a new app, back pressed, opening this activity
		finish();
	}

	// Method to check existing installed apps and add to apps table if they match the filter
	// TODO: Move to AsyncTask, something like "com.*" can hang the UI
	public void getAllAppsInstalledAndAdd(String newFilter)
	{

		List<String> appPackages = new ArrayList<String>();
		PackageManager pm;
		List<ResolveInfo> list;

		// get installed applications
		pm = this.getPackageManager();
		Intent intent = new Intent(Intent.ACTION_MAIN, null);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		list = pm.queryIntentActivities(intent,
				PackageManager.PERMISSION_GRANTED);

		// Loop through the installed apps and check if they match the new filter
		for (ResolveInfo rInfo : list)
		{

			String currentPackage = rInfo.activityInfo.applicationInfo.packageName.toLowerCase();

			if (newFilter.contains("*"))
			{
				if (currentPackage.toLowerCase().startsWith(newFilter.toLowerCase().substring(0, newFilter.indexOf("*"))))
					appPackages.add(currentPackage);

			}
			else
			{
				if (currentPackage.toLowerCase().equals(newFilter.toLowerCase()))
					appPackages.add(currentPackage);

			}

		}

		// If the list is > 0 add the packages to the database
		if(appPackages.size() != 0)
		{
			for (String s : appPackages)
			{
				List<PackageCollection> packageCollections = database.getAllFiltersInDatabase();

				database.addAppToDatabase(s, packageCollections.get(packageCollections.size()-1).mId);

				AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getBaseContext());
				int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(getBaseContext(), DDWidgetProvider.class));
				appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listView);
			}
		}
	}
}
