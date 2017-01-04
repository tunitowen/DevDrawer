package com.owentech.DevDrawer.dialogs;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.owentech.DevDrawer.DevDrawerApplication;
import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.adapters.PartialMatchAdapter;
import com.owentech.DevDrawer.appwidget.DDWidgetProvider;
import com.owentech.DevDrawer.data.model.Filter;
import com.owentech.DevDrawer.di.ApplicationModule;
import com.owentech.DevDrawer.di.DaggerApplicationComponent;
import com.owentech.DevDrawer.utils.OttoManager;
import com.owentech.DevDrawer.events.PackageAddedEvent;
import com.owentech.DevDrawer.utils.Database;
import com.owentech.DevDrawer.utils.RxUtils;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.reactivex.functions.Consumer;

public class AddPackageDialogFragment extends DialogFragment implements TextWatcher {

    @InjectView(R.id.addPackage) AutoCompleteTextView addPackage;
    @InjectView(R.id.addButton) Button addButton;
    @Inject Database database;

    private PartialMatchAdapter partialMatchAdapter;
    final private static String EDIT = "edit";
    final private static String WIDGET_ID = "widget_id";
    private String editString;
    private int widgetId;
    private List<String> appPackages;

    public AddPackageDialogFragment() {
        // Empty constructor required for DialogFragment
    }

    public static AddPackageDialogFragment newInstance(String editString, int widget_id) {
        AddPackageDialogFragment addPackageDialogFragment = new AddPackageDialogFragment();
        Bundle args = new Bundle();
        args.putString(EDIT, editString);
        args.putInt(WIDGET_ID, widget_id);
        addPackageDialogFragment.setArguments(args);
        return addPackageDialogFragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((DevDrawerApplication)getActivity().getApplication()).getApplicationComponent().inject(this);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        View view = inflater.inflate(R.layout.add_package_dialog_fragment, container);
        ButterKnife.inject(this, view);

        widgetId = getArguments().getInt(WIDGET_ID);

        if (editString != null) {
            editString = getArguments().getString(EDIT);
            addPackage.setText(editString);
        }

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(addPackage, InputMethodManager.SHOW_IMPLICIT);

        appPackages = getExistingPackages(getActivity());
        partialMatchAdapter = new PartialMatchAdapter(getActivity(), appPackages);
        addPackage.setAdapter(partialMatchAdapter);
        addPackage.addTextChangedListener(this);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addPackage.getText().length() != 0) {
                    // Check filter doesn't exist
                    if (!database.doesFilterExist(addPackage.getText().toString(), widgetId)) {
                        // Add the filter to the mDatabase
                        RxUtils.fromCallable(database.addFilterToDatabase(addPackage.getText().toString(), widgetId))
                                .subscribe();

                        RxUtils.fromCallable(getAllAppsInstalledAndAdd(addPackage.getText().toString()))
                                .subscribe(new Consumer<Boolean>() {
                                    @Override
                                    public void accept(Boolean aBoolean) throws Exception {
                                        getDialog().dismiss();
                                    }
                                });

                        addPackage.setText("");
                        OttoManager.getInstance().post(new PackageAddedEvent());
                    } else {
                        Toast.makeText(getActivity(), "Filter already exists", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    @Override
    public void afterTextChanged(final Editable editable) {
        partialMatchAdapter.getFilter().filter(editable.toString());
    }

    // Method to get all apps installed and return as List
    private static List<String> getExistingPackages(final Context context) {
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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        OttoManager.getInstance().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        OttoManager.getInstance().unregister(this);
    }

    public Callable<Boolean> getAllAppsInstalledAndAdd(final String newFilter) {

        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                List<String> appPackages = new ArrayList<String>();
                PackageManager pm;
                List<ResolveInfo> list;

                // get installed applications
                pm = getActivity().getPackageManager();
                Intent intent = new Intent(Intent.ACTION_MAIN, null);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                list = pm.queryIntentActivities(intent,
                        PackageManager.PERMISSION_GRANTED);

                // Loop through the installed apps and check if they match the new filter
                for (ResolveInfo rInfo : list) {

                    String currentPackage = rInfo.activityInfo.applicationInfo.packageName.toLowerCase();

                    if (newFilter.contains("*")) {
                        if (currentPackage.toLowerCase().startsWith(newFilter.toLowerCase().substring(0, newFilter.indexOf("*"))))
                            appPackages.add(currentPackage);

                    } else {
                        if (currentPackage.toLowerCase().equals(newFilter.toLowerCase()))
                            appPackages.add(currentPackage);

                    }
                }

                // If the list is > 0 add the packages to the database
                if (appPackages.size() != 0) {
                    for (final String s : appPackages) {
                        RxUtils.fromCallable(database.getAllFiltersInDatabase())
                                .subscribe(new Consumer<List<Filter>>() {
                                    @Override
                                    public void accept(List<Filter> filters) throws Exception {
                                        RxUtils.fromCallable(database.addAppToDatabase(s, filters.get(filters.size() - 1).id(), widgetId))
                                                .subscribe();

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                            Log.d("Context", AddPackageDialogFragment.this.getActivity().toString());
                                            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(AddPackageDialogFragment.this.getActivity());
                                            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(getActivity(), DDWidgetProvider.class));
                                            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listView);
                                        }
                                    }
                                });
                    }
                }

                return true;
            }
        };
    }
}
