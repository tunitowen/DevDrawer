package com.owentech.DevDrawer.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Window;
import android.widget.ListView;
import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.adapters.LegacyListAdapter;

public class LegacyDialog extends Activity
{

	ListView legacyListView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.legacy_dialog);
		legacyListView = (ListView) findViewById(R.id.legacyListView);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		if(sharedPreferences.getString("theme", "Light").equals("Light"))
		{
			legacyListView.setBackground(getResources().getDrawable(R.drawable.background_repeat));
		}
		else
		{
			legacyListView.setBackground(getResources().getDrawable(R.drawable.background_repeat_dark));
		}

		LegacyListAdapter listAdapter = new LegacyListAdapter(this);
		legacyListView.setAdapter(listAdapter);
	}
}
