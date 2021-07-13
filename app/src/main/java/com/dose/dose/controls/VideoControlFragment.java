package com.dose.dose.controls;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dose.dose.R;
import com.dose.dose.interfaces.SelectedSetting;
import com.dose.dose.interfaces.VideoControlInterface;

public class VideoControlFragment extends Fragment {
    private VideoControlInterface videoControlEvent;

    public VideoControlFragment(VideoControlInterface videoControlEvent) {
        this.videoControlEvent = videoControlEvent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video_control, container, false);

        view.findViewById(R.id.resolutionButton).setOnClickListener(v -> {
            this.videoControlEvent.openSetting(SelectedSetting.RESOLUTION);
        });
        view.findViewById(R.id.subtitleButton).setOnClickListener(v -> {
            this.videoControlEvent.openSetting(SelectedSetting.SUBTITLE);
        });
        view.findViewById(R.id.audioButton).setOnClickListener(v -> {
            this.videoControlEvent.openSetting(SelectedSetting.AUDIO);
        });

        return view;
    }
}