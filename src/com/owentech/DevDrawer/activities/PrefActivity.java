package com.owentech.DevDrawer.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import com.owentech.DevDrawer.R;

/**
 * Created with IntelliJ IDEA.
 * User: owent
 * Date: 04/02/2013
 * Time: 16:33
 * To change this template use File | Settings | File Templates.
 */
public class PrefActivity extends PreferenceActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

	}
}
