package com.dose.dose.details.ui.main;

import androidx.databinding.DataBindingUtil;
import androidx.leanback.app.DetailsFragmentBackgroundController;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.dose.dose.ApiClient.MovieAPIClient;
import com.dose.dose.R;
import com.dose.dose.VideoActivity;
import com.dose.dose.content.Movie;
import com.dose.dose.databinding.MovieDetailsBinding;
import com.dose.dose.details.MovieDetailsActivity;

public class MovieDetailsFragment extends Fragment {

    private MovieDetailsViewModel mViewModel;
    private Movie movie;
    private MovieAPIClient movieAPIClient;
    private final String TAG = "MovieDetailsFragment";


    public static MovieDetailsFragment newInstance() {
        return new MovieDetailsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        movie = (Movie) getActivity().getIntent().getSerializableExtra(MovieDetailsActivity.MOVIE);
        movieAPIClient = MovieAPIClient.newInstance(getContext());


        MovieDetailsBinding binding = DataBindingUtil.inflate(inflater, R.layout.movie_details, container, false);
        View view = binding.getRoot();
        binding.setMovie(movie);
        setupImages(view);
        setupDuration(view);
        setupOnClickListeners(view);
        return view;
        //return inflater.inflate(R.layout.movie_details, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(MovieDetailsViewModel.class);
        // TODO: Use the ViewModel
    }

    private void setupOnClickListeners(View view) {
        ImageButton playButton = view.findViewById(R.id.playButton);
        ImageButton continueButton = view.findViewById(R.id.continueButton);
        ImageButton addToWatchlistButton = view.findViewById(R.id.addButton);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "KLICKAD");
                Intent intent = new Intent(getActivity(), VideoActivity.class);
                intent.putExtra(VideoActivity.TYPE, VideoActivity.Type.MOVIE);
                intent.putExtra(VideoActivity.CONTINUE_WATCHING, false);
                intent.putExtra(VideoActivity.MOVIE, movie);
                startActivity(intent);
            }
        });
    }

    private void setupImages(View view) {
        ImageView posterImage = (ImageView)view.findViewById(R.id.moviePoster);
        Glide.with(requireContext())
                .load(movie.getPosterImage(true))
                .into(posterImage);

        RatingBar ratingBar = (RatingBar)view.findViewById(R.id.movieRating);
        ratingBar.setRating(3);

    }

    private void setupDuration(View view) {
        // Get the duration of the video
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    movie.setDuration(movieAPIClient.getDuration(movie.getId()));
                    TextView durationTextView = view.findViewById(R.id.movieRuntime);
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            durationTextView.setText(movie.getReadableDuration());
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    // Do something
                }

            }
        });
        thread.start();
    }

}