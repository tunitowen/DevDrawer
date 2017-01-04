package com.owentech.DevDrawer.fragments;

import android.app.Activity;
import android.graphics.Outline;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.adapters.NotificationFilterAdapter;
import com.owentech.DevDrawer.adapters.ShortcutFilterAdapter;
import com.owentech.DevDrawer.dialogs.AddPackageDialogFragment;
import com.owentech.DevDrawer.events.PackageAddedEvent;
import com.owentech.DevDrawer.utils.AppConstants;
import com.owentech.DevDrawer.utils.DebugLog;
import com.owentech.DevDrawer.utils.OttoManager;
import com.shamanland.fab.FloatingActionButton;
import com.shamanland.fab.ShowHideOnScroll;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by tonyowen on 17/07/2014.
 */
public class ShortcutFragment extends Fragment {

    @InjectView(R.id.recyclerView) RecyclerView recyclerView;
    @InjectView(R.id.fab) ImageButton fab;
    private ShortcutFilterAdapter shortcutFilterAdapter;
    float originalFabY;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notifications_fragment, container, false);
        ButterKnife.inject(this, view);
        setHasOptionsMenu(true);

        //Outline
        int size = getResources().getDimensionPixelSize(R.dimen.fab_size);
//        Outline outline = new Outline();
//        outline.setOval(0, 0, size, size);
//        fab.setOutline(outline);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddPackageDialog();
            }
        });
        originalFabY = fab.getTranslationY();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        ShortcutFilterAdapter.currentWidgetId = AppConstants.SHORTCUT;
        shortcutFilterAdapter = new ShortcutFilterAdapter(getActivity());

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(shortcutFilterAdapter);
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
        AddPackageDialogFragment addPackageDialogFragment = AddPackageDialogFragment.newInstance(null, AppConstants.SHORTCUT);
        addPackageDialogFragment.setTargetFragment(ShortcutFragment.this, 101);
        addPackageDialogFragment.show(getActivity().getSupportFragmentManager(), "dialog");
    }

    @Subscribe
    public void packageAdded(PackageAddedEvent event){
        if (shortcutFilterAdapter != null){
            shortcutFilterAdapter.updatePackageCollections();
        }
    }
}
