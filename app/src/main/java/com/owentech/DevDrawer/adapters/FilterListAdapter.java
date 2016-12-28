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
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.activities.EditDialog;
import com.owentech.DevDrawer.appwidget.DDWidgetProvider;
import com.owentech.DevDrawer.utils.AppWidgetUtil;
import com.owentech.DevDrawer.utils.Database;
import com.owentech.DevDrawer.utils.PackageCollection;

import java.util.ArrayList;
import java.util.List;

public class FilterListAdapter extends RecyclerView.Adapter<FilterListAdapter.ListItemViewHolder> {

    private Activity activity;
    private List<PackageCollection> packageCollections;
    public static int currentWidgetId = -1;

    public FilterListAdapter(Activity activity) {
        this.activity = activity;
        packageCollections = new ArrayList<PackageCollection>();
        updatePackageCollections();
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.package_list_item, viewGroup, false);
        return new ListItemViewHolder(itemView);
    }

    public void updatePackageCollections(){
        packageCollections = Database.getInstance(activity).getAllFiltersInDatabase(currentWidgetId);
        notifyDataSetChanged();
    }

    public int getCount() {
        return packageCollections.size();
    }

    public final static class ListItemViewHolder extends RecyclerView.ViewHolder {
        TextView txtPackageName;
        ImageView editButton;
        ImageView deleteButton;

        public ListItemViewHolder(View itemView) {
            super(itemView);
            txtPackageName = (TextView) itemView.findViewById(R.id.packageNameTextView);
            deleteButton = (ImageView) itemView.findViewById(R.id.deleteImageButton);
            editButton = (ImageView) itemView.findViewById(R.id.editImageButton);
        }
    }



    @Override
    public void onBindViewHolder(ListItemViewHolder viewHolder, final int position) {

        viewHolder.txtPackageName.setText(packageCollections.get(position).mPackageName);
        viewHolder.deleteButton.setImageDrawable(AppWidgetUtil.getColoredDrawable(activity, R.drawable.trash, activity.getResources().getColor(R.color.devDrawerDark)));
        viewHolder.editButton.setImageDrawable(AppWidgetUtil.getColoredDrawable(activity, R.drawable.edit, activity.getResources().getColor(R.color.devDrawerDark)));
        // OnClick action for Delete Button
        viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Database.getInstance(activity).removeFilterFromDatabase(packageCollections.get(position).mId);
                Database.getInstance(activity).removeAppFromDatabase(packageCollections.get(position).mId);
                updatePackageCollections();
                notifyDataSetChanged();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(activity);
                    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(activity, DDWidgetProvider.class));
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listView);
                }

            }

        });

        // OnClick action for Edit Button
        viewHolder.editButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent intent = new Intent(activity, EditDialog.class);
                Bundle bundle = new Bundle();
                bundle.putString("text", packageCollections.get(position).mPackageName);
                // TODO: 28/12/2016 rewrite
                bundle.putString("id", String.valueOf(packageCollections.get(position).mId));
                intent.putExtras(bundle);

                activity.startActivityForResult(intent, 0);
            }

        });

    }

    @Override
    public int getItemCount() {
        return packageCollections.size();
    }

//    @Override
//    public void notifyDataSetChanged() {
//        packageCollections = Database.getInstance(activity).getAllFiltersInDatabase(currentWidgetId);
//        super.notifyDataSetChanged();
//    }
//
//    public int getCount() {
//        return packageCollections.size();
//    }
//
//    public Object getItem(int position) {
//        return null;
//    }
//
//    public long getItemId(int position) {
//        return 0;
//    }
//
//    private class ViewHolder {
//        TextView txtPackageName;
//        ImageView editButton;
//        ImageView deleteButton;
//    }
//
//    public View getView(final int position, View convertView, ViewGroup parent) {
//        // Setup the list item text, onclicks etc
//        ViewHolder holder;
//        LayoutInflater inflater = activity.getLayoutInflater();
//
//        if (convertView == null) {
//            convertView = inflater.inflate(R.layout.package_list_item, null);
//            holder = new ViewHolder();
//
//            holder.txtPackageName = (TextView) convertView.findViewById(R.id.packageNameTextView);
//            holder.deleteButton = (ImageView) convertView.findViewById(R.id.deleteImageButton);
//            holder.editButton = (ImageView) convertView.findViewById(R.id.editImageButton);
//
//            convertView.setTag(holder);
//
//        } else {
//            holder = (ViewHolder) convertView.getTag();
//        }
//
//        holder.txtPackageName.setText(packageCollections.get(position).mPackageName);
//
//        // OnClick action for Delete Button
//        holder.deleteButton.setOnClickListener(new OnClickListener() {
//
//            public void onClick(View view) {
//                Database.getInstance(activity).removeFilterFromDatabase(packageCollections.get(position).mId);
//                Database.getInstance(activity).removeAppFromDatabase(packageCollections.get(position).mId);
//                notifyDataSetChanged();
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(activity);
//                    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(activity, DDWidgetProvider.class));
//                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listView);
//                }
//
//            }
//
//        });
//
//        // OnClick action for Edit Button
//        holder.editButton.setOnClickListener(new OnClickListener() {
//
//            public void onClick(View view) {
//                Intent intent = new Intent(activity, EditDialog.class);
//                Bundle bundle = new Bundle();
//                bundle.putString("text", packageCollections.get(position).mPackageName);
//                bundle.putString("id", packageCollections.get(position).mId);
//                intent.putExtras(bundle);
//
//                activity.startActivityForResult(intent, 0);
//            }
//
//        });
//
//        return convertView;
//    }
}