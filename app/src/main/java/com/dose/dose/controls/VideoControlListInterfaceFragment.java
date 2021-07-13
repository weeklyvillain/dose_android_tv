package com.dose.dose.controls;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.leanback.widget.BrowseFrameLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dose.dose.R;
import com.dose.dose.interfaces.SelectedSetting;
import com.dose.dose.interfaces.VideoControlListInterface;

import java.util.List;

public class VideoControlListInterfaceFragment extends Fragment implements VideoControlListInterface {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private List<ControlSetting> data;
    private VideoControlListInterface videoControlListInterfaceEvent;
    // Indicates if this instance is changing audio, resolution or subtitle
    private SelectedSetting selectedSetting;

    public VideoControlListInterfaceFragment(List<ControlSetting> data, SelectedSetting selectedSetting, VideoControlListInterface videoControlListInterface) {
        this.data = data;
        this.selectedSetting = selectedSetting;
        this.videoControlListInterfaceEvent = videoControlListInterface;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_resolution_list, container, false);
        ((TextView) view.findViewById(R.id.settingTitle)).setText(this.selectedSetting.toString());

        ((BrowseFrameLayout) view.findViewById(R.id.listContainer)).setOnFocusSearchListener(new BrowseFrameLayout.OnFocusSearchListener() {
            @Override
            public View onFocusSearch(View focused, int direction) {
                return focused;
            }
        });

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        // Set the adapter
        //if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new MyItemRecyclerViewAdapter(data, this.selectedSetting, this));
        //}
        return view;
    }

    @Override
    public void settingSelected(ControlSetting setting, SelectedSetting selectedSetting) {
        videoControlListInterfaceEvent.settingSelected(setting, selectedSetting);
    }
}