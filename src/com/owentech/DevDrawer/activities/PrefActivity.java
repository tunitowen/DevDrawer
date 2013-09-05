package com.owentech.DevDrawer.activities;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;
import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.appwidget.DDWidgetProvider;

/**
 * Created with IntelliJ IDEA.
 * User: owent
 * Date: 04/02/2013
 * Time: 16:33
 * To change this template use File | Settings | File Templates.
 */
public class PrefActivity extends PreferenceActivity
{

	SharedPreferences sp;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		sp = getPreferenceManager().getSharedPreferences();

		ListPreference activityChoicePref = (ListPreference)findPreference("widgetSorting");
		ListPreference themePref = (ListPreference)findPreference("theme");

		activityChoicePref.setSummary(nameFromValue(sp.getString("widgetSorting", "order"), activityChoicePref));
		themePref.setSummary(sp.getString("theme", "Light"));

		activityChoicePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
		{
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				SharedPreferences.Editor editor = sp.edit();
				editor.putString(preference.getKey(), newValue.toString());
				editor.commit();

				preference.setSummary(nameFromValue(newValue.toString(), preference));

				AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
				int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(getApplicationContext(), DDWidgetProvider.class));
				appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listView);

				return false;
			}
		});

		themePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
		{
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				SharedPreferences.Editor editor = sp.edit();
				editor.putString(preference.getKey(), newValue.toString());
				editor.commit();

				preference.setSummary(newValue.toString());

				Toast.makeText(PrefActivity.this, "You may need to re-add the widget for this change to take effect", Toast.LENGTH_SHORT).show();

				return false;
			}
		});

	}

    @Override
    public void onBackPressed() {
        //TODO check the todo on MainActivity. This is a workaround to it
        startActivity(new Intent(PrefActivity.this,MainActivity.class));
        finish();
    }

    private String nameFromValue(String value, Preference preference)
	{
		String ofTheSpaceCowboy = "";

		String[] values = getResources().getStringArray(R.array.sorting_options_values);
		String[] names = getResources().getStringArray(R.array.sorting_options);

		for (int i=0; i < names.length; i++)
		{
			if(value.equals(values[i]))
			{
				ofTheSpaceCowboy = names[i];
			}
		}

		return ofTheSpaceCowboy;
	}
}
