package com.owentech.DevDrawer.adapters;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.activities.EditDialog;
import com.owentech.DevDrawer.appwidget.DDWidgetProvider;
import com.owentech.DevDrawer.data.model.Filter;
import com.owentech.DevDrawer.di.DaggerDatabaseComponent;
import com.owentech.DevDrawer.di.DatabaseModule;
import com.owentech.DevDrawer.utils.Database;
import com.owentech.DevDrawer.utils.RxUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;

/**
 * Created by tonyowen on 17/07/2014.
 */
public class ShortcutFilterAdapter extends RecyclerView.Adapter<ShortcutFilterAdapter.ListItemViewHolder> {

    private Activity activity;
    private List<Filter> packageCollections;
    public static int currentWidgetId = -1;
    @Inject
    Database database;

    public ShortcutFilterAdapter(Activity activity) {
        this.activity = activity;
        packageCollections = new ArrayList<>();
        DaggerDatabaseComponent.builder()
                .databaseModule(new DatabaseModule(activity))
                .build().inject(this);
        updatePackageCollections();
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.package_list_item, viewGroup, false);
        return new ListItemViewHolder(itemView);
    }

    public void updatePackageCollections() {
        RxUtils.fromCallable(database.getAllFiltersInDatabase(currentWidgetId))
                .subscribe(new Consumer<List<Filter>>() {
                    @Override
                    public void accept(List<Filter> filters) throws Exception {
                        packageCollections = filters;
                        notifyDataSetChanged();
                    }
                });
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

        viewHolder.txtPackageName.setText(packageCollections.get(position).package_());

        // OnClick action for Delete Button
        viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                RxUtils.fromCallable(database.removeFilterFromDatabase(packageCollections.get(position).id()))
                        .subscribe();
                RxUtils.fromCallable(database.removeAppFromDatabase(packageCollections.get(position).id()))
                        .subscribe();
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
                bundle.putString("text", packageCollections.get(position).package_());
                // TODO: 28/12/2016 rewrite
                bundle.putString("id", String.valueOf(packageCollections.get(position).id()));
                intent.putExtras(bundle);

                activity.startActivityForResult(intent, 0);
            }

        });

    }

    @Override
    public int getItemCount() {
        return packageCollections.size();
    }
}
