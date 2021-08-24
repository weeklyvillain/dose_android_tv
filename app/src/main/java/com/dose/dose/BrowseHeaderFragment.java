package com.dose.dose;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.graphics.ColorUtils;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.palette.graphics.Palette;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.TransitionOptions;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.dose.dose.ApiClient.MovieAPIClient;
import com.dose.dose.content.BaseContent;
import com.dose.dose.content.Episode;
import com.dose.dose.content.Movie;
import com.dose.dose.databinding.FragmentBrowseHeaderBinding;
import com.dose.dose.search.SearchActivity;
import com.dose.dose.viewModels.SelectedViewModel;

import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class BrowseHeaderFragment extends Fragment {
    private BaseContent selected;
    private ImageView backdrop;
    private ImageView logo;
    private ImageButton searchBtn;
    private ImageButton browsePlayButton;
    private ImageButton browseInfoButton;
    private VideoView video;
    private MovieAPIClient movieAPIClient;
    private Movie randomMovie;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        movieAPIClient = MovieAPIClient.newInstance(getContext());
        new Thread(() -> {
            randomMovie = movieAPIClient.getRandom();
            requireActivity().runOnUiThread(() -> updateHeader(randomMovie, true));
        }).start();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        video.start();
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Create empty movie
        selected = new Movie();

        // Inflate the layout for this fragment
        FragmentBrowseHeaderBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_browse_header, container, false);
        View view = binding.getRoot();
        binding.setSelected(selected);
        backdrop = view.findViewById(R.id.header_backdrop);
        logo = view.findViewById(R.id.headerLogo);
        video = view.findViewById(R.id.videoView2);
        browsePlayButton = view.findViewById(R.id.browsePlayButton);
        browseInfoButton = view.findViewById(R.id.browseInfoButton);
        browsePlayButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), VideoActivity.class);
            intent.putExtra(VideoActivity.TYPE, VideoActivity.Type.MOVIE);
            intent.putExtra(VideoActivity.CONTINUE_WATCHING, false);
            intent.putExtra(VideoActivity.MOVIE, this.randomMovie);
            startActivity(intent);
        });

        video.setOnErrorListener((mp, what, extra) -> {
            Log.d("Video", "Error");
            return true;
        });

        video.setOnPreparedListener(mp -> mp.setLooping(true));

        searchBtn = view.findViewById(R.id.search_button);
        searchBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SearchActivity.class);

            Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    getActivity(),
                    searchBtn,
                    SearchActivity.SHARED_ELEMENT_NAME)
                    .toBundle();
            getActivity().startActivity(intent, bundle);
        });

        SelectedViewModel model = new ViewModelProvider(requireActivity()).get(SelectedViewModel.class);
        model.getSelected().observe(getViewLifecycleOwner(), item -> {
            updateHeader(item, false);
        });




        return view;
    }

    private void updateHeader(BaseContent item, boolean isRandomMovie) {
        // If this was the first selected, don't update the header
        selected.setDescription(item.getDescription());
        selected.setGenres(item.getGenresList());
        selected.setReleaseDate(String.format(" | %s", item.getReleaseDate()));

        if (item.gotLogo()) {
            logo.setVisibility(View.VISIBLE);
            selected.setTitle("");
            Glide.with(getContext())
                    .load(item.getLogoImageUrl(true))
                    .transition(DrawableTransitionOptions.withCrossFade(250))
                    .override(500, 300)
                    .into(logo);
        } else {
            selected.setTitle(item.getTitle());
            logo.setVisibility(View.GONE);
        }


        if (item instanceof Movie && item.getId() != null) {
            backdrop.setVisibility(View.INVISIBLE);
            video.setVisibility(View.VISIBLE);
            MovieAPIClient movieAPIClient = MovieAPIClient.newInstance(this.getContext());
            String trailerUrl = movieAPIClient.getTrailer(item.getId());
            Log.i("Trailer: ", trailerUrl);
            video.setVideoPath(trailerUrl);
            video.start();
        } else {
            video.stopPlayback();
            video.setVisibility(View.INVISIBLE);
            backdrop.setVisibility(View.VISIBLE);
            Glide.with(getContext())
                    .load(item.getCardImageUrl(true))
                    .transition(DrawableTransitionOptions.withCrossFade(250))
                    .into(backdrop);
        }
    }

    public int convertPxToDp(Context context, float px) {
        return (int) (px / context.getResources().getDisplayMetrics().density);
    }

}