package com.owentech.DevDrawer.utils;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;

import com.owentech.DevDrawer.appwidget.DDWidgetProvider;

/**
 * Created by Niek on 1/12/14.
 */
public class AppWidgetUtil {
    public static int[] findAppWidgetIds(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        return appWidgetManager.getAppWidgetIds(new ComponentName(context, DDWidgetProvider.class));
    }

}
