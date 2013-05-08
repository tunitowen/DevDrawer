package com.owentech.DevDrawer.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.utils.Database;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: owent
 * Date: 07/05/2013
 * Time: 16:39
 * To change this template use File | Settings | File Templates.
 */
public class LocaleListAdapter extends BaseAdapter
{

	Activity activity;
	List<String> localeList;
	Database database;

	public LocaleListAdapter(Activity activity)
	{
		this.activity = activity;
		database = new Database(activity);
		localeList = new ArrayList<String>();
		localeList.addAll(database.getLocales());
		notifyDataSetChanged();
	}


	@Override
	public int getCount()
	{
		return localeList.size();
	}

	@Override
	public Object getItem(int position)
	{
		return localeList.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	private class ViewHolder
	{
		TextView localeTextView;
		ImageView delete;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup viewGroup)
	{
		ViewHolder holder;
		LayoutInflater inflater = activity.getLayoutInflater();

		if(convertView == null)
		{
			convertView = inflater.inflate(R.layout.locale_list_item, null);
			holder = new ViewHolder();

			holder.localeTextView = (TextView) convertView.findViewById(R.id.localeTextView);
			holder.delete = (ImageView) convertView.findViewById(R.id.deleteImageButton);
			convertView.setTag(holder);

		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}

		holder.localeTextView.setText(localeList.get(position));

		holder.delete.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				database.deleteLocale(localeList.get(position));
				notifyDataSetChanged();
			}
		});

		return convertView;
	}

	@Override
	public void notifyDataSetChanged()
	{
		localeList = null;
		localeList = new ArrayList<String>();
		localeList.addAll(database.getLocales());
		super.notifyDataSetChanged();
	}
}
