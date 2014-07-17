package com.owentech.DevDrawer.activities;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RemoteViews;

import com.astuetz.PagerSlidingTabStrip;
import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.appwidget.DDWidgetProvider;
import com.owentech.DevDrawer.fragments.ShortcutFragment;
import com.owentech.DevDrawer.utils.OttoManager;
import com.owentech.DevDrawer.fragments.NotificationsFragment;
import com.owentech.DevDrawer.fragments.WidgetsFragment;
import com.owentech.DevDrawer.utils.AppConstants;
import com.owentech.DevDrawer.utils.AppWidgetUtil;
import com.owentech.DevDrawer.utils.Database;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Filter;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;


public class MainActivity extends FragmentActivity implements TextWatcher {

    @InjectView(R.id.main_viewpager) ViewPager viewPager;
    @InjectView(R.id.tabs) PagerSlidingTabStrip tabs;

    WidgetsFragment widgetsFragment;
    NotificationsFragment notificationsFragment;
    ShortcutFragment shortcutFragment;
    private int[] mAppWidgetIds;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        // Set up ActionBar to use custom view (Robot Light font)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayShowTitleEnabled(false);
            LayoutInflater inflater = LayoutInflater.from(this);
            View customView = inflater.inflate(R.layout.custom_ab_title, null);
            getActionBar().setCustomView(customView);
            getActionBar().setDisplayShowCustomEnabled(true);
        }

        Database.getInstance(this).createTables();
        mAppWidgetIds = AppWidgetUtil.findAppWidgetIds(this);

        viewPager.setAdapter(pagerAdapter);

        tabs.setIndicatorColor(getResources().getColor(R.color.dev_drawer_orange));
        tabs.setViewPager(viewPager);

        if (getIntent() != null) {
            int appWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (appWidgetId != -1) {
                for (int i = 0; i < mAppWidgetIds.length; i++) {
                    if (appWidgetId == mAppWidgetIds[i]) {
                        viewPager.setCurrentItem(i);
                    }
                }

                Database.getInstance(this).addWidgetToDatabase(appWidgetId, "");
                Crouton.makeText(this, getString(R.string.back_to_save), Style.ALERT).show();
            }
        }
    }

    private PagerAdapter pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
        @Override
        public android.support.v4.app.Fragment getItem(int position) {

            switch(position){
                case 0:{
                    if (widgetsFragment == null) {
                        widgetsFragment = new WidgetsFragment();
                    }
                    return widgetsFragment;
                }
                case 1:{
                    if (notificationsFragment == null) {
                        notificationsFragment = new NotificationsFragment();
                    }
                    return notificationsFragment;
                }
                case 2:{
                    if (shortcutFragment == null) {
                        shortcutFragment = new ShortcutFragment();
                    }
                    return shortcutFragment;
                }

                default:
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch(position){
                case 0:{
                    return getString(R.string.tab_widgets);
                }
                case 1:{
                    return getString(R.string.tab_notifications);
                }
                case 2:{
                    return getString(R.string.tab_shortcut);
                }
                default:
                    return "";
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    @Override
    public void onBackPressed() {

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            RemoteViews widget = DDWidgetProvider.getRemoteViews(this, appWidgetId);
            appWidgetManager.updateAppWidget(appWidgetId, widget);
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }

        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        OttoManager.getInstance().unregister(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        OttoManager.getInstance().register(this);
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Catch the return from the EditDialog
        if (resultCode == AppConstants.EDIT_DIALOG_CHANGE) {
            Bundle bundle = data.getExtras();
            Database.getInstance(this).amendFilterEntryTo(bundle.getString("id"), bundle.getString("newText"));
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        super.onMenuItemSelected(featureId, item);
        switch (item.getItemId()) {
            case R.id.menu_shortcut: {
                addShortcut(this);
                return true;
            }
            case R.id.menu_settings: {
                startActivity(new Intent(MainActivity.this, PrefActivity.class));
                return true;
            }
            default:
                return false;
        }
    }

    public void addShortcut(Context context) {
        Intent shortcutIntent = new Intent(this, LegacyDialog.class);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "^DevDrawer");
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(context, R.drawable.shortcut_icon));
        intent.setAction(getString(R.string.action_install_shortcut));
        context.sendBroadcast(intent);
    }


    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }


    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }

    // Method to get all apps installed and return as List
    private static List<String> getExistingPackages(Context context) {
        // get installed applications
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.PERMISSION_GRANTED);

        Set<String> appSet = new HashSet<String>();

        for (ResolveInfo rInfo : list) {
            String appName = rInfo.activityInfo.applicationInfo.packageName;
            appSet.add(appName);
            while (appName.length() > 0) {
                int lastIndex = appName.lastIndexOf(".");
                if (lastIndex > 0) {
                    appName = appName.substring(0, lastIndex);
                    appSet.add(appName + ".*");
                } else {
                    appName = "";
                }
            }
        }

        Collator collator = Collator.getInstance();
        ArrayList<String> appList = new ArrayList<String>(appSet);
        Collections.sort(appList, collator);
        return appList;
    }
}
