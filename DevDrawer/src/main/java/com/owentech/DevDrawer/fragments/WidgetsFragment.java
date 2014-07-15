package com.owentech.DevDrawer.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.adapters.FilterListAdapter;
import com.owentech.DevDrawer.dialogs.AddPackageDialogFragment;
import com.owentech.DevDrawer.dialogs.ChangeWidgetNameDialogFragment;
import com.owentech.DevDrawer.dialogs.ChooseWidgetDialogFragment;
import com.owentech.DevDrawer.events.ChangeWidgetEvent;
import com.owentech.DevDrawer.events.OttoManager;
import com.owentech.DevDrawer.events.PackageAddedEvent;
import com.owentech.DevDrawer.events.WidgetRenamedEvent;
import com.owentech.DevDrawer.utils.AppWidgetUtil;
import com.owentech.DevDrawer.utils.Database;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by tonyowen on 09/07/2014.
 */
public class WidgetsFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    @InjectView(R.id.selectionLayout) RelativeLayout selectionLayout;
    @InjectView(R.id.selectionShadow) View selectionShadow;
    @InjectView(R.id.listView) ListView listView;
    @InjectView(R.id.noWidgets) TextView noWidgets;
    @InjectView(R.id.currentWidgetName) TextView currentWidgetName;

    private int[] mAppWidgetIds;
    private FilterListAdapter filterListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.widgets_fragment, container, false);
        ButterKnife.inject(this, view);
        setHasOptionsMenu(true);
        selectionLayout.setOnClickListener(this);
        selectionLayout.setOnLongClickListener(this);
        return view;
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

    @Override
    public void onStart() {
        super.onStart();
        mAppWidgetIds = AppWidgetUtil.findAppWidgetIds(getActivity());
        showHideListView();

        if (mAppWidgetIds.length != 0) {
            if (FilterListAdapter.currentWidgetId == -1) {
                FilterListAdapter.currentWidgetId = mAppWidgetIds[0];
            }
        }

        filterListAdapter = new FilterListAdapter(getActivity());
        listView.setAdapter(filterListAdapter);
        filterListAdapter.notifyDataSetChanged();
        currentWidgetName.setText(Database.getInstance(getActivity()).getWidgetNames().get(FilterListAdapter.currentWidgetId));
    }

    @Override
    public void onClick(View view) {
        if (view == selectionLayout) {
            SparseArray<String> widgetNames = Database.getInstance(getActivity()).getWidgetNames();
            showChooseWidgetDialog();
        }
    }

    private void showHideListView(){
        if (mAppWidgetIds.length == 0){
            selectionLayout.setVisibility(View.INVISIBLE);
            selectionShadow.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.INVISIBLE);
            noWidgets.setVisibility(View.VISIBLE);
        }
        else{
            selectionLayout.setVisibility(View.VISIBLE);
            selectionShadow.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.VISIBLE);
            noWidgets.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.notifications_menu, menu);
        if (mAppWidgetIds.length == 0){
            menu.findItem(R.id.menu_add).setVisible(false);
        }
        else{
            menu.findItem(R.id.menu_add).setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        Log.d("MENU", "WidgetsFragment");
        if (item.getItemId() == R.id.menu_add) {
            Log.d("MENU", "Add");
            showAddPackageDialog();
        }
        return false;
    }

    private void showAddPackageDialog(){
        AddPackageDialogFragment addPackageDialogFragment = AddPackageDialogFragment.newInstance(null, FilterListAdapter.currentWidgetId);
        addPackageDialogFragment.setTargetFragment(WidgetsFragment.this, 101);
        addPackageDialogFragment.show(getActivity().getSupportFragmentManager(), "dialog");
    }

    private void showChooseWidgetDialog(){
        ChooseWidgetDialogFragment chooseWidgetDialogFragment = ChooseWidgetDialogFragment.newInstance(mAppWidgetIds[0]);
        chooseWidgetDialogFragment.setTargetFragment(WidgetsFragment.this, 101);
        chooseWidgetDialogFragment.show(getActivity().getSupportFragmentManager(), "dialog");
    }

    private void showChangeWidgetNameDialog(){
        ChangeWidgetNameDialogFragment changeWidgetNameDialogFragment = ChangeWidgetNameDialogFragment.newInstance(FilterListAdapter.currentWidgetId, currentWidgetName.getText().toString());
        changeWidgetNameDialogFragment.setTargetFragment(WidgetsFragment.this, 101);
        changeWidgetNameDialogFragment.show(getActivity().getSupportFragmentManager(), "dialog");
    }

    @Subscribe
    public void packageAdded(PackageAddedEvent event){
        if (filterListAdapter != null){
            filterListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onLongClick(View view) {
        showChangeWidgetNameDialog();
        return true;
    }

    @Subscribe
    public void changeWidget(ChangeWidgetEvent event){
        FilterListAdapter.currentWidgetId = event.widgetId;
        filterListAdapter.notifyDataSetChanged();
        currentWidgetName.setText(Database.getInstance(getActivity()).getWidgetNames().get(FilterListAdapter.currentWidgetId));
    }

    @Subscribe
    public void widgetRenamed(WidgetRenamedEvent event){
        currentWidgetName.setText(Database.getInstance(getActivity()).getWidgetNames().get(FilterListAdapter.currentWidgetId));
    }
}
