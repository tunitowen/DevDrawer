package com.owentech.DevDrawer.dialogs;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.adapters.ChooseWidgetAdapter;
import com.owentech.DevDrawer.adapters.PartialMatchAdapter;
import com.owentech.DevDrawer.events.ChangeWidgetEvent;
import com.owentech.DevDrawer.utils.OttoManager;
import com.owentech.DevDrawer.utils.Database;
import com.owentech.DevDrawer.utils.RxUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.reactivex.functions.Consumer;

/**
 * Created by tonyowen on 10/07/2014.
 */
public class ChooseWidgetDialogFragment extends DialogFragment implements AdapterView.OnItemClickListener {

    @InjectView(R.id.listView) ListView listView;

    private PartialMatchAdapter partialMatchAdapter;
    final private static String EDIT = "edit";
    final private static String WIDGET_ID = "widget_id";
    private int widgetId;
    private SparseArray<String> widgetNames;
    private ChooseWidgetAdapter chooseWidgetAdapter;

    public ChooseWidgetDialogFragment() {
        // Empty constructor required for DialogFragment
    }

    public static ChooseWidgetDialogFragment newInstance(int widget_id) {
        ChooseWidgetDialogFragment chooseWidgetDialogFragment = new ChooseWidgetDialogFragment();
        Bundle args = new Bundle();
        args.putInt(WIDGET_ID, widget_id);
        chooseWidgetDialogFragment.setArguments(args);
        return chooseWidgetDialogFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        View view = inflater.inflate(R.layout.choose_widget_dialog_fragment, container);
        ButterKnife.inject(this, view);

        widgetId = getArguments().getInt(WIDGET_ID);
        RxUtils.backgroundSingleFromCallable(Database.getInstance(getActivity()).getWidgetNames(getActivity()))
                .subscribe(new Consumer<SparseArray<String>>() {
                    @Override
                    public void accept(SparseArray<String> stringSparseArray) throws Exception {
                        widgetNames = stringSparseArray;
                        chooseWidgetAdapter = new ChooseWidgetAdapter(getActivity());
                        listView.setAdapter(chooseWidgetAdapter);
                        listView.setOnItemClickListener(ChooseWidgetDialogFragment.this);
                    }
                });

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
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        OttoManager.getInstance().post(new ChangeWidgetEvent(widgetNames.keyAt(position)));
        getDialog().dismiss();
    }
}