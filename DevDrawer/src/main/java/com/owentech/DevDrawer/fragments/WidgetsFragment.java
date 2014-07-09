package com.owentech.DevDrawer.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.adapters.FilterListAdapter;
import com.owentech.DevDrawer.dialogs.AddPackageDialogFragment;
import com.owentech.DevDrawer.events.OttoManager;
import com.owentech.DevDrawer.events.PackageAddedEvent;
import com.owentech.DevDrawer.utils.AppWidgetUtil;
import com.squareup.otto.Subscribe;


import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by tonyowen on 09/07/2014.
 */
public class WidgetsFragment extends Fragment implements View.OnClickListener {

    @InjectView(R.id.selectionLayout) RelativeLayout selectionLayout;
    @InjectView(R.id.selectionShadow) View selectionShadow;
    @InjectView(R.id.listView) ListView listView;
    @InjectView(R.id.noWidgets) TextView noWidgets;

    private int[] mAppWidgetIds;
    private FilterListAdapter filterListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.widgets_fragment, container, false);
        ButterKnife.inject(this, view);
        setHasOptionsMenu(true);
        selectionLayout.setOnClickListener(this);
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

        if (mAppWidgetIds.length > 0){
            filterListAdapter = new FilterListAdapter(getActivity(), mAppWidgetIds[0]);
            listView.setAdapter(filterListAdapter);
            filterListAdapter.notifyDataSetChanged();
        }

        if (mAppWidgetIds.length <= 1){
            selectionLayout.setVisibility(View.GONE);
            selectionShadow.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        if (view == selectionLayout) {
            Toast.makeText(getActivity(), "Number of widgets " + mAppWidgetIds.length, Toast.LENGTH_LONG).show();
        }
    }

    private void showHideListView(){
        if (mAppWidgetIds.length == 0){
            listView.setVisibility(View.INVISIBLE);
            noWidgets.setVisibility(View.VISIBLE);
        }
        else{
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
        AddPackageDialogFragment addPackageDialogFragment = AddPackageDialogFragment.newInstance(null, mAppWidgetIds[0]);
        addPackageDialogFragment.setTargetFragment(WidgetsFragment.this, 101);
        addPackageDialogFragment.show(getActivity().getSupportFragmentManager(), "dialog");
    }

    @Subscribe
    public void packageAdded(PackageAddedEvent event){
        if (filterListAdapter != null){
            filterListAdapter.notifyDataSetChanged();
        }
    }

}
