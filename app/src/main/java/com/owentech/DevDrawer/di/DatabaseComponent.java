package com.owentech.DevDrawer.di;

import android.content.BroadcastReceiver;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.widget.BaseAdapter;

import com.owentech.DevDrawer.activities.MainActivity;
import com.owentech.DevDrawer.adapters.FilterListAdapter;
import com.owentech.DevDrawer.adapters.NotificationFilterAdapter;
import com.owentech.DevDrawer.adapters.ShortcutFilterAdapter;
import com.owentech.DevDrawer.appwidget.DDWidgetProvider;
import com.owentech.DevDrawer.appwidget.DDWidgetViewsFactory;
import com.owentech.DevDrawer.dialogs.AddPackageDialogFragment;
import com.owentech.DevDrawer.dialogs.ChangeWidgetNameDialogFragment;
import com.owentech.DevDrawer.dialogs.ChooseWidgetDialogFragment;
import com.owentech.DevDrawer.fragments.WidgetsFragment;
import com.owentech.DevDrawer.receivers.AppInstalledReceiver;
import com.owentech.DevDrawer.receivers.AppUninstalledReceiver;
import com.owentech.DevDrawer.utils.Database;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {DatabaseModule.class})
public interface DatabaseComponent {
    void inject(MainActivity activity);
    void inject(BaseAdapter baseAdapter);
    void inject(FilterListAdapter adapter);
    void inject(NotificationFilterAdapter adapter);
    void inject(ShortcutFilterAdapter adapter);
    void inject(DDWidgetViewsFactory ddWidgetViewsFactory);
    void inject(WidgetsFragment fragment);
    void inject(DDWidgetProvider ddWidgetProvider);
    void inject(AppInstalledReceiver receiver);
    void inject(AppUninstalledReceiver receiver);
    void inject(AddPackageDialogFragment dialogFragment);
    void inject(ChangeWidgetNameDialogFragment dialogFragment);
    void inject(ChooseWidgetDialogFragment dialogFragment);
}
