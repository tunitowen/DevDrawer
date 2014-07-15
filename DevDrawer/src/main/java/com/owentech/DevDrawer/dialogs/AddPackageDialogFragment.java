package com.owentech.DevDrawer.dialogs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.adapters.PartialMatchAdapter;
import com.owentech.DevDrawer.utils.OttoManager;
import com.owentech.DevDrawer.events.PackageAddedEvent;
import com.owentech.DevDrawer.utils.AddAllAppsAsync;
import com.owentech.DevDrawer.utils.Database;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by tonyowen on 09/07/2014.
 */
public class AddPackageDialogFragment extends DialogFragment implements TextWatcher {

    @InjectView(R.id.addPackage)
    AutoCompleteTextView addPackage;
    @InjectView(R.id.addButton)
    ImageButton addButton;

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
                // TODO: Send back to main activity

                if (addPackage.getText().length() != 0) {
                    // Check filter doesn't exist
                    if (!Database.getInstance(getActivity()).doesFilterExist(addPackage.getText().toString(), widgetId)) {
                        // Add the filter to the mDatabase
                        Database.getInstance(getActivity()).addFilterToDatabase(addPackage.getText().toString(), widgetId);

                        // Check existing apps and add to installed apps table if they match new filter
                        new AddAllAppsAsync(getActivity(), addPackage.getText().toString(), widgetId).execute();

                        addPackage.setText("");
                        OttoManager.getInstance().post(new PackageAddedEvent());
                    } else {
                        Toast.makeText(getActivity(), "Filter already exists", Toast.LENGTH_SHORT).show();
                    }
                }
                getDialog().dismiss();
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
}
