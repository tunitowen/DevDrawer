package com.owentech.DevDrawer.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.adapters.FilterListAdapter;
import com.owentech.DevDrawer.utils.AddAllAppsAsync;
import com.owentech.DevDrawer.utils.Constants;
import com.owentech.DevDrawer.utils.Database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity implements TextWatcher
{

	Database database;

	ImageView addButton;
	AutoCompleteTextView addPackageAutoComplete;
	FilterListAdapter lviewAdapter;
	ListView listView;

	List<String> appPackages = new ArrayList<String>();

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
		addPackageAutoComplete = (AutoCompleteTextView) findViewById(R.id.addPackageEditText);
		listView = (ListView) findViewById(R.id.packagesListView);

		appPackages = getExistingPackages();
		addPackageAutoComplete.setAdapter(new ArrayAdapter<String>(this, R.layout.dropdown_list_item, appPackages));
		addPackageAutoComplete.addTextChangedListener(this);

		// Update the ListView from the database
		updateListView();

		addButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{

				if(addPackageAutoComplete.getText().length() != 0) // Check something entered
				{
					// Check filter doesn't exist
					if(!database.doesFilterExist(addPackageAutoComplete.getText().toString()))
					{
						// Add the filter to the database
						database.addFilterToDatabase(addPackageAutoComplete.getText().toString());

						// Check existing apps and add to installed apps table if they match new filter
                        new AddAllAppsAsync(getApplicationContext(), addPackageAutoComplete.getText().toString()).execute();

						addPackageAutoComplete.setText("");
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
				startActivity(new Intent(MainActivity.this, PrefActivity.class));
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

	// Method to get all apps installed and return as List
	List<String> getExistingPackages()
	{
		// get installed applications
		PackageManager pm = this.getPackageManager();
		Intent intent = new Intent(Intent.ACTION_MAIN, null);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> list= pm.queryIntentActivities(intent,
				PackageManager.PERMISSION_GRANTED);

		// sort the list alphabetically
		Collections.sort(list, new ResolveInfo.DisplayNameComparator(pm));

		appPackages.clear();

		for (ResolveInfo rInfo : list)
		{
			appPackages.add(rInfo.activityInfo.applicationInfo.packageName.toString());
		}

		return appPackages;
	}

	@Override
	public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
	{}

	@Override
	public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
	{}

	@Override
	public void afterTextChanged(Editable editable)
	{}
}
