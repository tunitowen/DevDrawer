package com.owentech.DevDrawer;

import android.appwidget.AppWidgetManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import com.owentech.DevDrawer.activities.MainActivity;
import com.owentech.DevDrawer.adapters.FilterListAdapter;
import com.owentech.DevDrawer.appwidget.DDWidgetProvider;
import com.owentech.DevDrawer.utils.Database;

/**
 * Created by Niek on 1/12/14.
 */
public class AppWidgetFragment extends Fragment {

    private static final String ARGUMENT_APPWIDGET_ID = "appWidgetId";

    private FilterListAdapter mFilterListAdapter;

    private String mCurrentDatabaseName;

    public static AppWidgetFragment newInstance(int widgetId) {
        AppWidgetFragment instance = new AppWidgetFragment();

        Bundle args = new Bundle();
        args.putInt(ARGUMENT_APPWIDGET_ID, widgetId);
        instance.setArguments(args);

        return instance;
    }

    public int getAppWidgetId() {
        return getArguments().getInt(ARGUMENT_APPWIDGET_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appwidget, container, false);

        ListView listView = (ListView) view.findViewById(R.id.fragment_appwidget_packagesListView);
        mFilterListAdapter = new FilterListAdapter(getActivity(), getAppWidgetId());
        listView.setAdapter(mFilterListAdapter);

        EditText editText = (EditText) view.findViewById(R.id.fragment_appwidget_nameet);
        mCurrentDatabaseName = new Database(getActivity()).getWidgetNames().get(getAppWidgetId());

        editText.setText(mCurrentDatabaseName);
        editText.addTextChangedListener(new WidgetNameTextWatcher());

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        mFilterListAdapter.notifyDataSetChanged();
    }

    private class WidgetNameTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s != null) {
                String name = s.toString().trim();
                if (!name.equals(mCurrentDatabaseName)) {
                    new Database(getActivity()).renameWidget(getAppWidgetId(), name);
                    mCurrentDatabaseName = name;

                    new DDWidgetProvider().onUpdate(getActivity(), AppWidgetManager.getInstance(getActivity()), new int[]{getAppWidgetId()});

                    ((MainActivity) getActivity()).update();
                }
            }
        }
    }
}
