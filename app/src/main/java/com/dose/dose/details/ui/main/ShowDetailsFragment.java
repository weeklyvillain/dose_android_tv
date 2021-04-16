package com.dose.dose.details.ui.main;

import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ClassPresenterSelector;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
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
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RatingBar;

import com.bumptech.glide.Glide;
import com.dose.dose.ApiClient.ShowAPIClient;
import com.dose.dose.MovieList;
import com.dose.dose.R;
import com.dose.dose.SeasonPosterPresenter;
import com.dose.dose.content.Season;
import com.dose.dose.content.Show;
import com.dose.dose.databinding.ShowDetailsBinding;
import com.dose.dose.details.ShowDetailsActivity;

import org.json.JSONException;

import java.util.List;

import adapters.SeasonAdapter;

public class ShowDetailsFragment extends Fragment {

    private ShowDetailsViewModel mViewModel;
    private Show show;
    private ShowAPIClient showAPIClient;
    private final String TAG = "ShowDetailsFragment";
    private ArrayAdapter mAdapter;
    private ClassPresenterSelector mPresenterSelector;
    private SeasonAdapter adapter;
    private RecyclerView recyclerView;

    public static ShowDetailsFragment newInstance() {
        return new ShowDetailsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        show = (Show) getActivity().getIntent().getSerializableExtra(ShowDetailsActivity.SHOW);
        showAPIClient = ShowAPIClient.newInstance(getContext());

        ShowDetailsBinding binding = DataBindingUtil.inflate(inflater, R.layout.show_details, container, false);
        View view = binding.getRoot();
        binding.setShow(show);

        setupImages(view);
        try {
            setupSeasonListRow(view);
        } catch(Exception e) {
            e.printStackTrace();
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ShowDetailsViewModel.class);
        // TODO: Use the ViewModel
    }

    private void setupImages(View view) {
        ImageView posterImage = view.findViewById(R.id.showPoster);
        Glide.with(getContext())
                .load(show.getPosterImage(true))
                .into(posterImage);

        RatingBar ratingBar = view.findViewById(R.id.showRating);
        ratingBar.setRating(3);

    }

    private void setupSeasonListRow(View view) throws JSONException {
        recyclerView = (RecyclerView) view.findViewById(R.id.seasons);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        Thread thread = new Thread(() -> {
            try {
                List<Season> seasons = MovieList.setupSeasons(showAPIClient, show);
                adapter = new SeasonAdapter(getContext(), seasons);
                Log.i(TAG, seasons.toString());

                requireActivity().runOnUiThread(() -> recyclerView.setAdapter(adapter));

            } catch(Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();

    }


}