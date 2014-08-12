package com.owentech.DevDrawer.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.adapters.NotificationFilterAdapter;
import com.owentech.DevDrawer.dialogs.AddPackageDialogFragment;
import com.owentech.DevDrawer.events.PackageAddedEvent;
import com.owentech.DevDrawer.utils.OttoManager;
import com.owentech.DevDrawer.utils.AppConstants;
import com.shamanland.fab.FloatingActionButton;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by tonyowen on 09/07/2014.
 */
public class NotificationsFragment extends Fragment {

    @InjectView(R.id.listView) ListView listView;
    @InjectView(R.id.fab) FloatingActionButton fab;
    private NotificationFilterAdapter notificationFilterAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notifications_fragment, container, false);
        ButterKnife.inject(this, view);
        setHasOptionsMenu(true);

        fab.setColor(getResources().getColor(R.color.devDrawerPrimary));
        fab.initBackground();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddPackageDialog();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        NotificationFilterAdapter.currentWidgetId = AppConstants.NOTIFICATION;
        notificationFilterAdapter = new NotificationFilterAdapter(getActivity());
        listView.setAdapter(notificationFilterAdapter);
        notificationFilterAdapter.notifyDataSetChanged();
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.notifications_menu, menu);
        menu.findItem(R.id.menu_add).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_add) {
            showAddPackageDialog();
        }
        return false;
    }

    private void showAddPackageDialog(){
        AddPackageDialogFragment addPackageDialogFragment = AddPackageDialogFragment.newInstance(null, AppConstants.NOTIFICATION);
        addPackageDialogFragment.setTargetFragment(NotificationsFragment.this, 101);
        addPackageDialogFragment.show(getActivity().getSupportFragmentManager(), "dialog");
    }

    @Subscribe
    public void packageAdded(PackageAddedEvent event){
        if (notificationFilterAdapter != null){
            notificationFilterAdapter.notifyDataSetChanged();
        }
    }
}
