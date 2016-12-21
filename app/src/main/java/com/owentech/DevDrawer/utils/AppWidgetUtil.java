package com.owentech.DevDrawer.utils;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;

import com.owentech.DevDrawer.appwidget.DDWidgetProvider;

/**
 * Created by Niek on 1/12/14.
 */
public class AppWidgetUtil {
    public static int[] findAppWidgetIds(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        return appWidgetManager.getAppWidgetIds(new ComponentName(context, DDWidgetProvider.class));
    }

    public static Drawable getColoredDrawable(Context context, int whiteDrawableResId, int targetColor) {
        Drawable drawable = context.getResources().getDrawable(whiteDrawableResId);
        ColorFilter filter = new LightingColorFilter(targetColor, 0);
        drawable.mutate().setColorFilter(filter);
        return drawable;
    }

}
