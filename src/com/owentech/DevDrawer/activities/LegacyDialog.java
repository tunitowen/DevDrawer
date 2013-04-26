package com.owentech.DevDrawer.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
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

		int bgResId = -1;
		if(sharedPreferences.getString("theme", "Light").equals("Light"))
		{
		   bgResId = R.drawable.background_repeat;
		}
		else
		{
		   bgResId = R.drawable.background_repeat_dark;
		}
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
	      legacyListView.setBackgroundDrawable(getResources().getDrawable(bgResId));
		}
		else {
         setLegacyBackgroundPostJB(bgResId);
		}

		LegacyListAdapter listAdapter = new LegacyListAdapter(this);
		legacyListView.setAdapter(listAdapter);
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
   private void setLegacyBackgroundPostJB(int bgResId) {
      legacyListView.setBackground(getResources().getDrawable(bgResId));
	}
	
	
}
