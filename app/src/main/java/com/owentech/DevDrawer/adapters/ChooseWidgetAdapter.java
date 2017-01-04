package com.owentech.DevDrawer.adapters;

import android.app.Activity;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.owentech.DevDrawer.DevDrawerApplication;
import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.di.DaggerApplicationComponent;
import com.owentech.DevDrawer.di.ApplicationModule;
import com.owentech.DevDrawer.utils.Database;
import com.owentech.DevDrawer.utils.RxUtils;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;

/**
 * Created by tonyowen on 14/07/2014.
 */
public class ChooseWidgetAdapter extends BaseAdapter {

    Activity activity;
    SparseArray<String> widgets;
    @Inject Database database;

    public ChooseWidgetAdapter(final Activity activity) {
        this.activity = activity;
        ((DevDrawerApplication)activity.getApplication()).getApplicationComponent().inject(this);
        RxUtils.fromCallable(database.getWidgetNames(activity))
                .subscribe(new Consumer<SparseArray<String>>() {
                    @Override
                    public void accept(SparseArray<String> stringSparseArray) throws Exception {
                        widgets = stringSparseArray;
                    }
                });
    }


    @Override
    public int getCount() {
        return widgets.size();
    }

    public Object getItem(int position){
        return null;
    }

    public long getItemId(int position){
        return 0;
    }

    private class ViewHolder {
        TextView name;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        LayoutInflater inflater = activity.getLayoutInflater();

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.choose_widget_list_item, null);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name.setText(widgets.get(widgets.keyAt(position)));
        return convertView;
    }
}
