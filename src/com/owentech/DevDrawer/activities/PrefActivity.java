package com.owentech.DevDrawer.activities;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
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

		activityChoicePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
		{
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				SharedPreferences.Editor editor = sp.edit();
				editor.putString(preference.getKey(), newValue.toString());
				editor.commit();

				AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
				int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(getApplicationContext(), DDWidgetProvider.class));
				appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listView);

				return false;
			}
		});

	}
}
