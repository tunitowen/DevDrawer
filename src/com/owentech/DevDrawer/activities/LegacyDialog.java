package com.owentech.DevDrawer.activities;

import android.app.Activity;
import android.os.Bundle;
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

		LegacyListAdapter listAdapter = new LegacyListAdapter(this);
		legacyListView.setAdapter(listAdapter);
	}
}
