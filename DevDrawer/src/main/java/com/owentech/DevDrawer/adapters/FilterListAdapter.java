package com.owentech.DevDrawer.adapters;

/**
 * Created with IntelliJ IDEA.
 * User: owent
 * Date: 29/01/2013
 * Time: 16:54
 * To change this template use File | Settings | File Templates.
 */

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.owentech.DevDrawer.activities.EditDialog;
import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.appwidget.DDWidgetProvider;
import com.owentech.DevDrawer.utils.Database;
import com.owentech.DevDrawer.utils.PackageCollection;

import java.util.List;

public class FilterListAdapter extends BaseAdapter
{

	Activity activity;
	List<PackageCollection> packageCollections;

	public FilterListAdapter(Activity activity, List<PackageCollection> packageCollections)
	{
		super();
		this.activity = activity;
		this.packageCollections = packageCollections;
	}

	public int getCount()
	{
		return packageCollections.size();
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
		TextView txtPackageName;
		ImageView editButton;
		ImageView deleteButton;
	}

	public View getView(final int position, View convertView, ViewGroup parent)
	{
		// Setup the list item text, onclicks etc
		ViewHolder holder;
		LayoutInflater inflater = activity.getLayoutInflater();

		if(convertView == null)
		{
			convertView = inflater.inflate(R.layout.package_list_item, null);
			holder = new ViewHolder();

			holder.txtPackageName = (TextView) convertView.findViewById(R.id.packageNameTextView);
			holder.deleteButton = (ImageView) convertView.findViewById(R.id.deleteImageButton);
			holder.editButton = (ImageView) convertView.findViewById(R.id.editImageButton);

			convertView.setTag(holder);

		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}

		holder.txtPackageName.setText(packageCollections.get(position).mPackageName);

		// OnClick action for Delete Button
		holder.deleteButton.setOnClickListener(new OnClickListener()
		{

			public void onClick(View view)
			{
				Database database = new Database(activity);
				database.removeFilterFromDatabase(packageCollections.get(position).mId);
				database.removeAppFromDatabase(packageCollections.get(position).mId);
				notifyDataSetChanged();

				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				{
					AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(activity);
					int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(activity, DDWidgetProvider.class));
					appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listView);
				}

			}

		});

		// OnClick action for Edit Button
		holder.editButton.setOnClickListener(new OnClickListener()
		{

			public void onClick(View view)
			{
				Intent intent = new Intent(activity, EditDialog.class);
				Bundle bundle = new Bundle();
				bundle.putString("text", packageCollections.get(position).mPackageName);
				bundle.putString("id", packageCollections.get(position).mId);
				intent.putExtras(bundle);

				activity.startActivityForResult(intent, 0);
			}

		});

		return convertView;
	}

	@Override
	public void notifyDataSetChanged()
	{
		Database database = new Database(activity);
		packageCollections = database.getAllFiltersInDatabase();

		super.notifyDataSetChanged();
	}
}