package com.dose.dose.details.ui.main;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;

import com.bumptech.glide.Glide;
import com.dose.dose.ApiClient.ShowAPIClient;
import com.dose.dose.MovieList;
import com.dose.dose.R;
import com.dose.dose.content.Episode;
import com.dose.dose.content.Season;
import com.dose.dose.content.Show;
import com.dose.dose.databinding.SeasonDetailsBinding;
import com.dose.dose.details.SeasonDetailsActivity;
import com.dose.dose.details.ShowDetailsActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import adapters.EpisodeAdapter;
import adapters.SeasonAdapter;

public class SeasonDetailsFragment extends Fragment {
    private final String TAG = "SeasonDetailsFragment";
    private SeasonDetailsViewModel mViewModel;
    private Season season;
    private ShowAPIClient showAPIClient;
    private EpisodeAdapter adapter;
    private RecyclerView recyclerView;

    public static SeasonDetailsFragment newInstance() {
        return new SeasonDetailsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        season = (Season) getActivity().getIntent().getSerializableExtra(SeasonDetailsActivity.SEASON);
        showAPIClient = ShowAPIClient.newInstance(getContext());
        SeasonDetailsBinding binding = DataBindingUtil.inflate(inflater, R.layout.season_details, container, false);
        View view = binding.getRoot();
        binding.setSeason(season);

        setSeasonInformationAndShowEpisodeListRow(view);
        setupImages(view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SeasonDetailsViewModel.class);
        // TODO: Use the ViewModel
    }

    private void setupImages(View view) {
        ImageView posterImage = view.findViewById(R.id.seasonPoster);
        Glide.with(getContext())
                .load(season.getPosterImage(true))
                .into(posterImage);

    }

    private void setSeasonInformationAndShowEpisodeListRow(View view) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject seasonInformation = showAPIClient.getSeasonInformation(season.getShow().getId(), season.getId());
                try {
                    season.setDescription(seasonInformation.getString("overview"));
                    JSONArray episodes = seasonInformation.getJSONArray("episodes");
                    for (int i = 0; i < episodes.length(); i++) {
                        season.addEpisode(Episode.newInstance(
                                episodes.getJSONObject(i).getString("name"),
                                episodes.getJSONObject(i).getInt("episode"),
                                episodes.getJSONObject(i).getString("backdrop"),
                                episodes.getJSONObject(i).getString("overview"),
                                Integer.valueOf(episodes.getJSONObject(i).getString("internalID")),
                                Float.valueOf(episodes.getJSONObject(i).getString("vote_average")),
                                season
                        ));
                    }
                    setupSeasonListRow(view);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void setupSeasonListRow(View view) throws JSONException {
        recyclerView = (RecyclerView) view.findViewById(R.id.episodes);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        adapter = new EpisodeAdapter(getContext(), season.getEpisodes());
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setAdapter(adapter);
            }
        });


    }

}