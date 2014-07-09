package com.owentech.DevDrawer.activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.owentech.DevDrawer.AppWidgetFragment;
import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.adapters.PartialMatchAdapter;
import com.owentech.DevDrawer.appwidget.DDWidgetProvider;
import com.owentech.DevDrawer.dialogs.AddPackageDialogFragment;
import com.owentech.DevDrawer.events.OttoManager;
import com.owentech.DevDrawer.fragments.NotificationsFragment;
import com.owentech.DevDrawer.fragments.WidgetsFragment;
import com.owentech.DevDrawer.utils.AddAllAppsAsync;
import com.owentech.DevDrawer.utils.AppWidgetUtil;
import com.owentech.DevDrawer.utils.Constants;
import com.owentech.DevDrawer.utils.Database;
import com.viewpagerindicator.TitlePageIndicator;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;


public class MainActivity extends FragmentActivity implements TextWatcher, View.OnClickListener {

    WidgetsFragment widgetsFragment;
    NotificationsFragment notificationsFragment;

    private ViewPager mViewPager;
    private WidgetFragmentViewPagerAdapter mViewPagerAdapter;

//    private TitlePageIndicator mTitlePageIndicator;
    private PagerSlidingTabStrip tabs;

    private Database mDatabase;
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

        mDatabase = new Database(this);
        mDatabase.createTables();

        mAppWidgetIds = AppWidgetUtil.findAppWidgetIds(this);

        setupViews();

        if (getIntent() != null) {
            int appWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (appWidgetId != -1) {
                for (int i = 0; i < mAppWidgetIds.length; i++) {
                    if (appWidgetId == mAppWidgetIds[i]) {
                        mViewPager.setCurrentItem(i);
                    }
                }

                mDatabase.addWidgetToDatabase(appWidgetId, "");
                Crouton.makeText(this, "Press back to save the widget, not home", Style.ALERT).show();
            }
        }
    }

    private void setupViews() {
        List<String> appPackages = getExistingPackages(this);

        mViewPagerAdapter = new WidgetFragmentViewPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.main_viewpager);
//        mViewPager.setAdapter(mViewPagerAdapter);

        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);

        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
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
                    default:
                        return null;
                }
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch(position){
                    case 0:{
                        return "WIDGETS";
                    }
                    case 1:{
                        return "NOTIFICATIONS";
                    }
                    default:
                        return "Nothing";
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        });

        tabs.setIndicatorColor(getResources().getColor(R.color.dev_drawer_orange));
        tabs.setShouldExpand(true);
        tabs.setViewPager(mViewPager);

        mViewPagerAdapter.setWidgetIds(mAppWidgetIds);
        mViewPagerAdapter.notifyDataSetChanged();

        boolean hasWidgets = mAppWidgetIds.length > 0;
//        mAutoCompleteTextView.setVisibility(hasWidgets ? View.VISIBLE : View.GONE);
//        addButton.setVisibility(hasWidgets ? View.VISIBLE : View.GONE);
//        mViewPager.setVisibility(hasWidgets ? View.VISIBLE : View.GONE);
//        tabs.setVisibility(hasWidgets ? View.VISIBLE : View.GONE);
//        tabs.setIndicatorColor(getResources().getColor(R.color.dev_drawer_orange));
//        findViewById(R.id.activity_main_filterTitle).setVisibility(hasWidgets ? View.VISIBLE : View.GONE);
    }

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
        if (resultCode == Constants.EDIT_DIALOG_CHANGE) {
            Bundle bundle = data.getExtras();

            Database database = new Database(this);
            database.amendFilterEntryTo(bundle.getString("id"), bundle.getString("newText"));

            updateFragments();
        }
    }

    private void updateFragments() {
        int currentItem = mViewPager.getCurrentItem();
        for (int i = currentItem - 1; i <= currentItem + 1; i++) {
            if (i >= 0 && i < mViewPagerAdapter.getCount()) {
                getWidgetFragment(i).notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        super.onMenuItemSelected(featureId, item);
        switch (item.getItemId()) {
            case R.id.menu_shortcut: {
                Log.d("MENU", "Shortcut");
                addShortcut(this);
                return true;
            }
            case R.id.menu_settings: {
                Log.d("MENU", "Settings");
                startActivity(new Intent(MainActivity.this, PrefActivity.class));
                return true;
            }
            default:
                Log.d("MENU", "Default");
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
        intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        context.sendBroadcast(intent);
    }


    @Override
    protected void onStop() {
        super.onStop();
        //TODO is this really needed? It makes the prefActivity to close the app on backpress
        // this is called to prevent a new app, back pressed, opening this activity
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
//        mPartialMatchAdapter.getFilter().filter(editable.toString());
    }

    private AppWidgetFragment getWidgetFragment(int position) {
        return (AppWidgetFragment) mViewPagerAdapter.instantiateItem(mViewPager, position);
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

    public void update() {
        mViewPagerAdapter.updateNames();
    }

    @Override
    public void onClick(View v) {
//        if (v.getId() == R.id.activity_main_addButton) {
//            if (mAutoCompleteTextView.getText().length() != 0) {
//                // Check filter doesn't exist
//                int appWidgetId = getWidgetFragment(mViewPager.getCurrentItem()).getAppWidgetId();
//                if (!mDatabase.doesFilterExist(mAutoCompleteTextView.getText().toString(), appWidgetId)) {
//                    // Add the filter to the mDatabase
//                    mDatabase.addFilterToDatabase(mAutoCompleteTextView.getText().toString(), appWidgetId);
//
//                    // Check existing apps and add to installed apps table if they match new filter
//                    new AddAllAppsAsync(MainActivity.this, mAutoCompleteTextView.getText().toString(), appWidgetId).execute();
//
//                    mAutoCompleteTextView.setText("");
//                    updateFragments();
//                } else {
//                    Toast.makeText(MainActivity.this, "Filter already exists", Toast.LENGTH_SHORT).show();
//                }
//            }
//        }
    }

    private class WidgetFragmentViewPagerAdapter extends FragmentPagerAdapter {

        @Override
        public CharSequence getPageTitle(int position) {
            return mNames[position].toUpperCase(Locale.getDefault());
        }

        private int[] mWidgetIds;
        private String[] mNames;

        public WidgetFragmentViewPagerAdapter(FragmentManager fm) {
            super(fm);
            mWidgetIds = new int[0];
        }

        @Override
        public Fragment getItem(int i) {
            return AppWidgetFragment.newInstance(mWidgetIds[i]);
        }

        @Override
        public int getCount() {
            return mWidgetIds.length;
        }

        public void updateNames() {
            Log.d("MainActivity", "updateNames");
            mNames = new String[mWidgetIds.length];

            Map<Integer, String> widgetNames = new Database(MainActivity.this).getWidgetNames();
            for (int i = 0; i < mWidgetIds.length; i++) {
                mNames[i] = widgetNames.get(mWidgetIds[i]);
                if (mNames[i] == null){
                    Log.d("mNamesNull", "Empty");
                    mNames[i] = "Unnamed";
                }
                else {
                    Log.d("mNames", mNames[i]);
                }
                if (mNames[i] == null || mNames[i].trim().isEmpty()) {
                    mNames[i] = "No Name";
                }
            }
            tabs.notifyDataSetChanged();
        }

        public void setWidgetIds(int[] widgetIds) {
            mWidgetIds = widgetIds;
            updateNames();
        }
    }

    private void showTestNotification(){
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setContentTitle("30DAYS");
        notificationBuilder.setContentText("com.owentech.thirtydays");
        notificationBuilder.setSmallIcon(R.drawable.ic_launcher);

        notificationBuilder.addAction(R.drawable.ic_action_trash_white, "Uninstall", null);
        notificationBuilder.addAction(R.drawable.ic_action_settings_white, "App Settings", null);
        notificationBuilder.setPriority(Notification.PRIORITY_LOW);

        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(ns);;
        mNotificationManager.notify(1,
                notificationBuilder.build());
    }
}
