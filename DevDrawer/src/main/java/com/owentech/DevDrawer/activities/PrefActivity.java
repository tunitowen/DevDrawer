package com.owentech.DevDrawer.activities;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.*;
import android.widget.Toast;

import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.appwidget.DDWidgetProvider;

import java.lang.ref.WeakReference;

/**
 * Created with IntelliJ IDEA.
 * User: owent
 * Date: 04/02/2013
 * Time: 16:33
 * To change this template use File | Settings | File Templates.
 */
public class PrefActivity extends PreferenceActivity {
    SharedPreferences sp;
    SwitchPreference rootPref;
    CheckBoxPreference rootQuickUninstall;
    CheckBoxPreference rootClearData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        sp = getPreferenceManager().getSharedPreferences();

        ListPreference activityChoicePref = (ListPreference) findPreference("widgetSorting");
//        ListPreference themePref = (ListPreference) findPreference("theme");
        ListPreference intentsPref = (ListPreference) findPreference("launchingIntents");

        activityChoicePref.setSummary(nameFromValue(sp.getString("widgetSorting", "order"), activityChoicePref));
//        themePref.setSummary(sp.getString("theme", "Light"));
        intentsPref.setSummary(intentNameFromValue(sp.getString("launchingIntents", "aosp"), intentsPref));

        activityChoicePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
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

        intentsPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(preference.getKey(), newValue.toString());
                editor.commit();

                preference.setSummary(intentNameFromValue(newValue.toString(), preference));

                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(PrefActivity.this, MainActivity.class));
        finish();
    }

    private String nameFromValue(String value, Preference preference) {
        String ofTheSpaceCowboy = "";

        String[] values = getResources().getStringArray(R.array.sorting_options_values);
        String[] names = getResources().getStringArray(R.array.sorting_options);

        for (int i = 0; i < names.length; i++) {
            if (value.equals(values[i])) {
                ofTheSpaceCowboy = names[i];
            }
        }

        return ofTheSpaceCowboy;
    }

    private String intentNameFromValue(String value, Preference preference) {
        String ofTheSpaceCowboy = "";

        String[] values = getResources().getStringArray(R.array.launching_intents_values);
        String[] names = getResources().getStringArray(R.array.launching_intents);

        for (int i = 0; i < names.length; i++) {
            if (value.equals(values[i])) {
                ofTheSpaceCowboy = names[i];
            }
        }

        return ofTheSpaceCowboy;
    }
}
