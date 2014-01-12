package com.owentech.DevDrawer.appwidget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class DDWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return(new DDWidgetViewsFactory(this.getApplicationContext(),
                intent));
    }
}