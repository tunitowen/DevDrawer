package com.owentech.DevDrawer.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.adapters.ActivityListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: owent
 * Date: 06/02/2013
 * Time: 07:07
 * To change this template use File | Settings | File Templates.
 */
public class ChooseActivityDialog extends Activity implements ListView.OnItemClickListener
{

	ListView listView;
	String packageName;
	List<String> activitiesList;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choice);

		packageName = getIntent().getStringExtra("packageName");

		listView = (ListView) findViewById(R.id.listView);

		try
		{
			activitiesList = getActivityList(packageName);
		}
		catch(PackageManager.NameNotFoundException e)
		{
			e.printStackTrace();
		}

		listView.setAdapter(new ActivityListAdapter(this, activitiesList));
		listView.setOnItemClickListener(this);

	}

	private List<String> getActivityList(String packageName)
			throws PackageManager.NameNotFoundException {

		PackageManager pm = this.getPackageManager();

		List<String> adapter = new ArrayList<String>();

		PackageInfo info = pm.getPackageInfo(packageName,
				PackageManager.GET_ACTIVITIES);
		ActivityInfo[] list = info.activities;

		if (list != null && list.length != 0)
		{
			for (ActivityInfo activity : list) {
				if (activity.exported) {
					adapter.add(activity.name.toString());
				}
			}
		}

		return adapter;
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
	{
		Intent intent = new Intent();
		intent.setComponent(new ComponentName(
				packageName, activitiesList.get(i)
				.toString()));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

}
