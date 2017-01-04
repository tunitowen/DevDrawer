package com.owentech.DevDrawer.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.SparseArray;
import android.view.View;
import android.widget.RemoteViews;

import com.owentech.DevDrawer.DevDrawerApplication;
import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.activities.ClickHandlingActivity;
import com.owentech.DevDrawer.di.DaggerApplicationComponent;
import com.owentech.DevDrawer.di.ApplicationModule;
import com.owentech.DevDrawer.utils.AppConstants;
import com.owentech.DevDrawer.utils.AppWidgetUtil;
import com.owentech.DevDrawer.utils.Database;
import com.owentech.DevDrawer.utils.RxUtils;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;

public class DDWidgetProvider extends AppWidgetProvider {

    public static String PACKAGE_STRING = "default.package";
    public static String REFRESH = "refresh";

    @Inject
    Database database;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews widget = getRemoteViews(context, appWidgetId);
            appWidgetManager.updateAppWidget(appWidgetId, widget);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    public RemoteViews getRemoteViews(Context context, final int appWidgetId) {
        injectDependencies(context);

        // Setup the widget, and data source / adapter
        Intent svcIntent = new Intent(context, DDWidgetService.class);

        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        final RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        widget.setRemoteAdapter(R.id.listView, svcIntent);

        Intent clickIntent = new Intent(context, ClickHandlingActivity.class);
        PendingIntent clickPI = PendingIntent.getActivity(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        RxUtils.fromCallable(database.getWidgetNames(context))
                .subscribe(new Consumer<SparseArray<String>>() {
                    @Override
                    public void accept(SparseArray<String> stringSparseArray) throws Exception {
                        String name = stringSparseArray.get(appWidgetId);
                        if (name == null || name.trim().isEmpty() || name.equalsIgnoreCase(AppConstants.UNNAMED)) {
                            name = "DevDrawer";
                        }
                        widget.setTextViewText(R.id.widget_layout_titletv, name);
                    }
                });

        // TODO: 30/12/2016 why is this true
        if (true){
            widget.setViewVisibility(R.id.lightHeader, View.VISIBLE);
            widget.setViewVisibility(R.id.darkHeader, View.INVISIBLE);
            widget.setViewVisibility(R.id.lightBackground, View.VISIBLE);
            widget.setViewVisibility(R.id.darkBackground, View.INVISIBLE);
//            widget.setTextColor(R.id.widget_layout_titletv, context.getResources().getColor(android.R.color.white));
//            widget.setImageViewBitmap(R.id.refresh, drawableToBitmap(AppWidgetUtil.getColoredDrawable(context, R.drawable.refresh, context.getResources().getColor(android.R.color.white))));
//            widget.setTextColor(R.id.widget_layout_titletv, context.getResources().getColor(android.R.color.black));
//            widget.setImageViewBitmap(R.id.refresh, drawableToBitmap(AppWidgetUtil.getColoredDrawable(context, R.drawable.refresh, context.getResources().getColor(android.R.color.black))));
        }
        else{
            widget.setViewVisibility(R.id.lightHeader, View.INVISIBLE);
            widget.setViewVisibility(R.id.darkHeader, View.VISIBLE);
            widget.setViewVisibility(R.id.lightBackground, View.INVISIBLE);
            widget.setViewVisibility(R.id.darkBackground, View.VISIBLE);
//            widget.setTextColor(R.id.widget_layout_titletv, context.getResources().getColor(android.R.color.white));
//            widget.setImageViewBitmap(R.id.refresh, drawableToBitmap(AppWidgetUtil.getColoredDrawable(context, R.drawable.refresh, context.getResources().getColor(android.R.color.white))));
        }

        widget.setViewVisibility(R.id.widget_layout_titletv, View.VISIBLE);

        widget.setPendingIntentTemplate(R.id.listView, clickPI);

        Intent refreshIntent = new Intent(context, DDWidgetProvider.class);
        refreshIntent.setAction(REFRESH);
        refreshIntent.getIntExtra("widgetId", appWidgetId);
        PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, 0);

        widget.setOnClickPendingIntent(R.id.refresh, refreshPendingIntent);

        return widget;
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        injectDependencies(context);
        for (int appWidgetId : appWidgetIds) {
            RxUtils.fromCallable(database.removeWidgetFromDatabase(appWidgetId))
                    .subscribe();
        }
    }

    private void injectDependencies(Context context){
        ((DevDrawerApplication)context.getApplicationContext()).getApplicationComponent().inject(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.getAction().equals(REFRESH)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                int[] appWidgetIds = AppWidgetUtil.findAppWidgetIds(context);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listView);
                onUpdate(context, appWidgetManager, appWidgetIds);
            }
        }
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

}