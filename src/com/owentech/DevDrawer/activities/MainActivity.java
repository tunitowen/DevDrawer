package com.owentech.DevDrawer.activities;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.adapters.FilterListAdapter;
import com.owentech.DevDrawer.appwidget.DDWidgetProvider;
import com.owentech.DevDrawer.adapters.PartialMatchAdapter;
import com.owentech.DevDrawer.utils.AddAllAppsAsync;
import com.owentech.DevDrawer.utils.Constants;
import com.owentech.DevDrawer.utils.Database;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity implements TextWatcher
{

	Database database;

	ImageView addButton;
	AutoCompleteTextView addPackageAutoComplete;
	FilterListAdapter lviewAdapter;
	ListView listView;
	PartialMatchAdapter partialMatchAdapter;

	List<String> appPackages;

    @Override
	public void onCreate(Bundle state)
	{
		super.onCreate(state);

		setContentView(R.layout.main);

		// Set up ActionBar to use custom view (Robot Light font)
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB)
		{
			getActionBar().setDisplayShowTitleEnabled(false);
			LayoutInflater inflater = LayoutInflater.from(this);
			View customView = inflater.inflate(R.layout.custom_ab_title, null);
			getActionBar().setCustomView(customView);
			getActionBar().setDisplayShowCustomEnabled(true);
		}

		// Create the database tables
		database = new Database(this);
		database.createTables();

		// Setup view components
		addButton = (ImageView) findViewById(R.id.addButton);
		addPackageAutoComplete = (AutoCompleteTextView) findViewById(R.id.addPackageEditText);
		listView = (ListView) findViewById(R.id.packagesListView);

		appPackages = getExistingPackages();

		partialMatchAdapter = new PartialMatchAdapter(this, appPackages);
		addPackageAutoComplete.setAdapter(partialMatchAdapter);
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

    @Override
    public void onBackPressed() {

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            RemoteViews widget = DDWidgetProvider.getRemoteViews(this,appWidgetId);
            appWidgetManager.updateAppWidget(appWidgetId, widget);
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }

        super.onBackPressed();
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
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			menu.add(0, Constants.MENU_SHORTCUT, 0, "Create Legacy Shortcut").setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
			menu.add(0, Constants.MENU_SETTINGS, 0, "Settings").setIcon(R.drawable.ic_action_settings_white).setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
			//menu.add(0, Constants.MENU_LOCALE_SWITCHER, 0, "Locale Switcher").setIcon(R.drawable.ic_action_globe).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
		else
		{
			menu.add(0, Constants.MENU_SHORTCUT, 0, "Create Shortcut");
			menu.add(0, Constants.MENU_SETTINGS, 0, "Settings");
			//menu.add(0, Constants.MENU_LOCALE_SWITCHER, 0, "Locale Switcher");
		}
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item)
	{
		switch(item.getItemId())
		{
			case Constants.MENU_SHORTCUT:
			{
				addShortcut(this);
				break;
			}
			case Constants.MENU_SETTINGS:
			{
				startActivity(new Intent(MainActivity.this, PrefActivity.class));
				break;
			}
			case Constants.MENU_LOCALE_SWITCHER:
			{

				startActivity(new Intent(this, LocaleSwitcher.class));
				break;
			}
		}
		return false;
	}

	public void addShortcut(Context context) {
		Intent shortcutIntent = new Intent(this, LegacyDialog.class);
		shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		Intent intent = new Intent();
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "^DevDrawer");
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(context, R.drawable.shortcut_icon));
		intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		String shortcutUri = intent.toUri(MODE_WORLD_WRITEABLE);
		context.sendBroadcast(intent);
	}

	@Override
	protected void onStop()
	{
		super.onStop();
        //TODO is this really needed? It makes the prefActivity to close the app on backpress
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

        Set<String> appSet = new HashSet<String>();

		for (ResolveInfo rInfo : list) {
            String appName = rInfo.activityInfo.applicationInfo.packageName.toString();
            appSet.add(appName);
            while (appName.length() > 0) {
                int lastIndex = appName.lastIndexOf(".");
                if (lastIndex > 0) {
                    appName = appName.substring(0,lastIndex);
                    appSet.add(appName+".*");
                } else {
                    appName = "";
                }
            }
		}

        Collator collator = Collator.getInstance();
        ArrayList<String> appList = new ArrayList<String>(appSet);
        Collections.sort(appList, collator);
		return appList;
	}

	@Override
	public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
	{}

	@Override
	public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
	{}

	@Override
	public void afterTextChanged(Editable editable)
	{
		partialMatchAdapter.getFilter().filter(editable.toString());
	}

}
