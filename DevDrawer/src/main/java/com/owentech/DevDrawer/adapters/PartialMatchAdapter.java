package com.owentech.DevDrawer.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.owentech.DevDrawer.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: owent
 * Date: 07/05/2013
 * Time: 08:15
 * To change this template use File | Settings | File Templates.
 */
public class PartialMatchAdapter extends BaseAdapter implements Filterable
{

	Activity activity;
	List<String> items;
	List<String> filteredItems;
	Filter filter;

	public PartialMatchAdapter(Activity activity, List<String> items)
	{
		this.activity = activity;
		this.items = items;
	}

	@Override
	public int getCount()
	{
		return filteredItems.size();
	}

	@Override
	public Object getItem(int position)
	{
		return filteredItems.get(position);
	}

	@Override
	public long getItemId(int i)
	{
		return i;
	}

	class ViewHolder
	{
		TextView textView;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup)
	{

		// Setup the list item text, onclicks etc
		try
		{
			ViewHolder holder;
			LayoutInflater inflater = activity.getLayoutInflater();

			if(convertView == null)
			{
				convertView = inflater.inflate(R.layout.dropdown_list_item, null);
				holder = new ViewHolder();

				holder.textView = (TextView) convertView.findViewById(android.R.id.text1);
				convertView.setTag(holder);

			}
			else
			{
				holder = (ViewHolder) convertView.getTag();
			}

			holder.textView.setText(filteredItems.get(position));
		}
		catch(Exception e)
		{
			return null;
		}

		return convertView;
	}

	@Override
	public Filter getFilter()
	{
		if (filter == null)
		{

			filter = new Filter()
			{
				@Override
				protected FilterResults performFiltering(CharSequence charSequence)
				{

					filteredItems = new ArrayList<String>();
					if(charSequence != null){
						for (String item : items)
						{
							if (item.toLowerCase().contains(charSequence.toString().toLowerCase()))
							{
								filteredItems.add(item);
							}
						}
					}

					FilterResults filterResults = new FilterResults();
					filterResults.count = filteredItems.size();
					filterResults.values = filteredItems;
					return filterResults;
				}

				@Override
				protected void publishResults(CharSequence charSequence, FilterResults filterResults)
				{
					notifyDataSetChanged();
				}
			};

		}

		return filter;
	}
}
