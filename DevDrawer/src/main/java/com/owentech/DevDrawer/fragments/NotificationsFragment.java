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

import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.dialogs.AddPackageDialogFragment;
import com.owentech.DevDrawer.events.OttoManager;
import com.owentech.DevDrawer.utils.Constants;

/**
 * Created by tonyowen on 09/07/2014.
 */
public class NotificationsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notifications_fragment, container, false);
        setHasOptionsMenu(true);
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.notifications_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_add) {
            showAddPackageDialog();
        }
        return false;
    }

    private void showAddPackageDialog(){
        AddPackageDialogFragment addPackageDialogFragment = AddPackageDialogFragment.newInstance(null, Constants.NOTIFICATION);
        addPackageDialogFragment.setTargetFragment(NotificationsFragment.this, 101);
        addPackageDialogFragment.show(getActivity().getSupportFragmentManager(), "dialog");
    }
}
