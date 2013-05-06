package com.owentech.DevDrawer.activities;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.*;
import android.widget.Toast;

import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.appwidget.DDWidgetProvider;
import com.owentech.DevDrawer.utils.RootFeatures;

import java.lang.ref.WeakReference;

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
    SwitchPreference rootPref;

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

        rootPref = (SwitchPreference)findPreference("rootEnabled");
        rootPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                toggleRootAccess((Boolean)newValue);
                return true;
            }
        });
	}

    /*
     * Handler to receive result from AsyncTask
     *
     * When the screen orientation changes right after switching Root featues ON/OFF
     * then we won't receive the result from the AsyncTask, and therefore the Root
     * features would be enabled even though there's no root available.
     *
     * Inspired from the following article
     * http://www.androiddesignpatterns.com/2013/01/inner-class-handler-memory-leak.html
     */
    private static class RootResultHandler extends Handler {
        private final WeakReference<PrefActivity> mActivity;

        public RootResultHandler(PrefActivity activity) {
            mActivity = new WeakReference<PrefActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            PrefActivity activity = mActivity.get();
            if (activity != null) {
                if (msg.arg1 == 1) {
                    activity.toggleRootViews(true);
                }
                else {
                    activity.rootPref.setChecked(false);
                    noRootAccessError(activity);
                }
            }
        }
    }

    private final RootResultHandler handler = new RootResultHandler(this);

    private void toggleRootAccess(final boolean enabled) {
        if (enabled) {
            RootFeatures.checkAccess(new RootFeatures.Listener() {
                @Override
                public void onFinished(boolean result) {
                    Message msg = new Message();
                    msg.arg1 = result ? 1 : 0;
                    handler.sendMessage(msg);
                }
            });
        }
        else {
            toggleRootViews(false);
        }
    }

    private static void noRootAccessError(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("No root access available!")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Error")
                .setNegativeButton("OK", null);

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void toggleRootViews(boolean enabled) {
        CheckBoxPreference rootQuickUninstall = (CheckBoxPreference)findPreference("rootQuickUninstall");
        CheckBoxPreference rootClearCache = (CheckBoxPreference)findPreference("rootClearCache");

        rootQuickUninstall.setEnabled(enabled);
        rootClearCache.setEnabled(enabled);

        rootQuickUninstall.setChecked(enabled);
        rootClearCache.setChecked(enabled);
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
