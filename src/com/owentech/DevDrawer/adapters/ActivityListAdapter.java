package com.owentech.DevDrawer.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.owentech.DevDrawer.R;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: owent
 * Date: 06/02/2013
 * Time: 16:31
 * To change this template use File | Settings | File Templates.
 */
public class ActivityListAdapter extends BaseAdapter
{
	Activity activity;
	List<String> activityList;

	public ActivityListAdapter (Activity activity, List<String> activityList)
	{
		super();
		this.activity = activity;
		this.activityList = activityList;
	}

	public int getCount()
	{
		return activityList.size();
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
		TextView txtActivityPath;
		TextView txtActivityName;
	}

	public View getView(final int position, View convertView, ViewGroup parent)
	{
		// Setup the list item text, onclicks etc
		ViewHolder holder;
		LayoutInflater inflater = activity.getLayoutInflater();

		if(convertView == null)
		{
			convertView = inflater.inflate(R.layout.activity_choice_list_item, null);
			holder = new ViewHolder();

			holder.txtActivityPath = (TextView) convertView.findViewById(R.id.activityPathTextView);
			holder.txtActivityName = (TextView) convertView.findViewById(R.id.activityNameTextView);

			convertView.setTag(holder);

		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}

		holder.txtActivityPath.setText(activityList.get(position).substring(0, activityList.get(position).lastIndexOf('.')));
		holder.txtActivityName.setText(activityList.get(position)
				.substring(activityList.get(position).lastIndexOf('.'), activityList.get(position).length()));

		// OnClick action for Delete Button

		return convertView;
	}

	@Override
	public void notifyDataSetChanged()
	{
//		Database database = new Database(activity);
//		packageCollections = database.getAllFiltersInDatabase();

		super.notifyDataSetChanged();
	}
}
