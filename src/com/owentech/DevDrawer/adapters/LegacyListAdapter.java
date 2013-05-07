package com.owentech.DevDrawer.adapters;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.activities.ClickHandlingActivity;
import com.owentech.DevDrawer.utils.Database;

import java.util.ArrayList;
import java.util.List;

public class LegacyListAdapter extends BaseAdapter
{
	PackageManager pm;

	public List<String> applicationNames;
	public List<String> packageNames;
	public List<Drawable> applicationIcons;

	Activity activity;
	Database database;
	SharedPreferences sp;
    boolean rootClearCache;

	public LegacyListAdapter (Activity activity)
	{
		super();
		this.activity = activity;
		database = new Database(activity);
		getApps();
		notifyDataSetChanged();
		sp = PreferenceManager.getDefaultSharedPreferences(activity);
        rootClearCache = sp.getBoolean("rootClearCache", false);
	}

	public int getCount()
	{
		return applicationNames.size();
	}

	public Object getItem(int position)
	{
		return null;
	}

	public long getItemId(int position)
	{
		return 0;
	}

	private class ViewHolder
	{
		ImageView icon;
		TextView packageName;
		TextView appName;
		ImageView delete;
		ImageView settings;
        ImageView clear;
        ImageView more;
		Button touchArea;
	}

	public View getView(final int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder;
		final LayoutInflater inflater = activity.getLayoutInflater();

		if(convertView == null)
		{
			convertView = inflater.inflate(rootClearCache ? R.layout.list_item_more_legacy : R.layout.list_item, null);
			holder = new ViewHolder();

			holder.icon = (ImageView) convertView.findViewById(R.id.imageView);
			holder.packageName = (TextView) convertView.findViewById(R.id.packageNameTextView);
			holder.appName = (TextView) convertView.findViewById(R.id.appNameTextView);
			holder.delete = (ImageView) convertView.findViewById(R.id.uninstallImageButton);
			holder.settings = (ImageView) convertView.findViewById(R.id.appDetailsImageButton);
            holder.clear = (ImageView) convertView.findViewById(R.id.clearImageButton);
            holder.more = (ImageView) convertView.findViewById(R.id.moreImageButton);
			holder.touchArea = (Button) convertView.findViewById(R.id.touchArea);

			convertView.setTag(holder);

		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}

		holder.icon.setImageBitmap(convertFromDrawable(applicationIcons.get(position)));
		holder.packageName.setText(packageNames.get(position));
		holder.appName.setText(applicationNames.get(position));

		if(sp.getString("theme", "Light").equals("Light"))
		{
			holder.appName.setTextColor(activity.getResources().getColor(R.color.app_name_light));
			if (holder.delete != null) holder.delete.setImageResource(R.drawable.delete_imageview);
            if (holder.settings != null) holder.settings.setImageResource(R.drawable.settings_imageview);
            if (holder.clear != null) holder.clear.setImageResource(R.drawable.clear_imageview);
            if (holder.more != null) holder.more.setImageResource(R.drawable.more_imageview);
		}
		else
		{
			holder.appName.setTextColor(activity.getResources().getColor(R.color.app_name_dark));
            if (holder.delete != null) holder.delete.setImageResource(R.drawable.delete_imageview_dark);
            if (holder.settings != null) holder.settings.setImageResource(R.drawable.settings_imageview_dark);
            if (holder.clear != null) holder.clear.setImageResource(R.drawable.clear_imageview_dark);
            if (holder.more != null) holder.more.setImageResource(R.drawable.more_imageview_dark);
		}

        if (holder.delete != null) holder.delete.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				ClickHandlingActivity.startUninstall(activity, packageNames.get(position));
			}
		});

        if (holder.settings != null) holder.settings.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				ClickHandlingActivity.startAppDetails(activity, packageNames.get(position));
			}
		});

        if (holder.clear != null) holder.clear.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ClickHandlingActivity.startClearCache(activity, packageNames.get(position));
            }
        });

        if (holder.more != null) holder.more.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ClickHandlingActivity.startMoreOverflowMenu(activity, packageNames.get(position));
            }
        });

		holder.touchArea.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				ClickHandlingActivity.startApp(activity, packageNames.get(position));
			}
		});

		return convertView;
	}

	@Override
	public void notifyDataSetChanged()
	{
		super.notifyDataSetChanged();
	}

	// Method to get all apps from the app database and add to the dataset
	public void getApps()
	{

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);

		// Get all apps from the app table
		String[] packages = database.getAllAppsInDatabase(sp.getString("widgetSorting", "added"));
		pm = activity.getPackageManager();

		// Defensive code, was getting some strange behaviour and forcing the lists seems to fix
		applicationNames = null;
		packageNames = null;
		applicationIcons = null;

		// Setup the lists holding the data
		applicationNames = new ArrayList<String>();
		packageNames = new ArrayList<String>();
		applicationIcons = new ArrayList<Drawable>();

		// Loop though adding details from PackageManager to the lists
		for(String s : packages)
		{
			ApplicationInfo applicationInfo;

			try {
				applicationInfo = pm.getPackageInfo(s, PackageManager.GET_ACTIVITIES).applicationInfo;
				applicationNames.add(applicationInfo.loadLabel(pm).toString());
				packageNames.add(applicationInfo.packageName.toString());
				applicationIcons.add(applicationInfo.loadIcon(pm));

			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}
		}

	}

	// Method to return a bitmap from drawable
	public Bitmap convertFromDrawable(Drawable d)
	{
		return ((BitmapDrawable)d).getBitmap();
	}
}
