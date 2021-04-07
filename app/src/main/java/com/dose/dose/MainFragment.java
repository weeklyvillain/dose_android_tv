package com.dose.dose;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.leanback.app.BackgroundManager;
import androidx.leanback.app.BrowseFragment;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.dose.dose.ApiClient.MovieAPIClient;
import com.dose.dose.ApiClient.ShowAPIClient;
import com.dose.dose.content.BaseContent;
import com.dose.dose.content.Episode;
import com.dose.dose.content.Movie;
import com.dose.dose.content.Show;
import com.dose.dose.details.MovieDetailsActivity;
import com.dose.dose.details.ShowDetailsActivity;

import org.json.JSONException;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class MainFragment extends BrowseSupportFragment {
    private static final String TAG = "MainFragment";

    private static final int BACKGROUND_UPDATE_DELAY = 300;
    private static final int GRID_ITEM_WIDTH = 200;
    private static final int GRID_ITEM_HEIGHT = 200;
    private static final int NUM_ROWS = 6;
    private static final int NUM_COLS = 15;

    private final Handler mHandler = new Handler();
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private Timer mBackgroundTimer;
    private String mBackgroundUri;
    private BackgroundManager mBackgroundManager;
    private MovieAPIClient movieAPIClient;
    private ShowAPIClient showAPIClient;

    // List row adapters
    private static ArrayObjectAdapter ongoingMovies;
    private static ArrayObjectAdapter ongoingShows;
    private static ArrayObjectAdapter movieWatchlist;
    private static ArrayObjectAdapter newlyAddedMovies;
    private static ArrayObjectAdapter newlyAddedShows;
    private static ArrayObjectAdapter newReleasesMovies;

    // Private update functions
    private static void updateOngoingMovie(Movie movie) {
        for (int i = 0; i < ongoingMovies.size(); i++) {
            Movie currentMovie = (Movie) ongoingMovies.get(i);
            if (currentMovie.getId().equals(movie.getId())) {
                ongoingMovies.replace(i, movie);
                ongoingMovies.notifyArrayItemRangeChanged(i, 1);
                break;
            }
        }
    }

    private static void updateWatchlistMovie(Movie movie) {
        for (int i = 0; i < movieWatchlist.size(); i++) {
            Movie currentMovie = (Movie) movieWatchlist.get(i);
            if (currentMovie.getId().equals(movie.getId())) {
                movieWatchlist.replace(i, movie);
                movieWatchlist.notifyArrayItemRangeChanged(i, 1);
                break;
            }
        }
    }

    private static void updateNewlyAddedMovie(Movie movie) {
        for (int i = 0; i < newlyAddedMovies.size(); i++) {
            Movie currentMovie = (Movie) newlyAddedMovies.get(i);
            if (currentMovie.getId().equals(movie.getId())) {
                newlyAddedMovies.replace(i, movie);
                newlyAddedMovies.notifyArrayItemRangeChanged(i, 1);
                break;
            }
        }
    }

    private static void updateNewlyAddedShow(Show show) {
        for (int i = 0; i < newlyAddedShows.size(); i++) {
            Show currentShow = (Show) newlyAddedShows.get(i);
            if (currentShow.getId().equals(show.getId())) {
                newlyAddedShows.replace(i, show);
                newlyAddedShows.notifyArrayItemRangeChanged(i, 1);
                break;
            }
        }
    }

    private static void updateNewReleaseMovie(Movie movie) {
        for (int i = 0; i < newReleasesMovies.size(); i++) {
            Movie currentMovie = (Movie) newReleasesMovies.get(i);
            if (currentMovie.getId().equals(movie.getId())) {
                newReleasesMovies.replace(i, movie);
                newReleasesMovies.notifyArrayItemRangeChanged(i, 1);
                break;
            }
        }
    }



    // Public update functions
    public static void removeMovieFromOngoing(Movie movie) {
        for (int i = 0; i < ongoingMovies.size(); i++) {
            Movie currentMovie = (Movie) ongoingMovies.get(i);
            if (currentMovie.getId().equals(movie.getId())) {
                ongoingMovies.remove(currentMovie);
                ongoingMovies.notifyArrayItemRangeChanged(i-1, 2);
                break;
            }
        }
    }

    public static void removeEpisodeFromOngoing(Episode episode) {
        for (int i = 0; i < ongoingShows.size(); i++) {
            Episode currentEpisode = (Episode) ongoingShows.get(i);
            if (currentEpisode.getId().equals(episode.getId())) {
                ongoingShows.remove(currentEpisode);
                ongoingShows.notifyArrayItemRangeChanged(i-1, 2);
                break;
            }
        }
    }

    public static void removeMovieFromWatchlist(Movie movie) {
        for (int i = 0; i < movieWatchlist.size(); i++) {
            Movie currentMovie = (Movie) movieWatchlist.get(i);
            if (currentMovie.getId().equals(movie.getId())) {
                movieWatchlist.remove(currentMovie);
                movieWatchlist.notifyArrayItemRangeChanged(i-1, 2);
                break;
            }
        }
    }

    public static void updateAllMovieInfo(Movie movie) {
        updateOngoingMovie(movie);
        updateNewlyAddedMovie(movie);
        updateNewReleaseMovie(movie);
        updateWatchlistMovie(movie);
    }

    public static void updateOngoingEpisode(Episode episode) {
        for (int i = 0; i < ongoingShows.size(); i++) {
            Episode currentEpisode = (Episode) ongoingShows.get(i);
            if (currentEpisode.getId().equals(episode.getId())) {
                ongoingShows.replace(i, episode);
                ongoingShows.notifyArrayItemRangeChanged(i, 1);
                break;
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);

        prepareBackgroundManager();

        movieAPIClient = MovieAPIClient.newInstance(getActivity());
        showAPIClient = ShowAPIClient.newInstance(getActivity());

        setupUIElements();

        try {
            loadRows();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        setupEventListeners();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mBackgroundTimer) {
            Log.d(TAG, "onDestroy: " + mBackgroundTimer.toString());
            mBackgroundTimer.cancel();
        }
    }

    private void loadRows() throws JSONException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int rows = 0;
                    ArrayObjectAdapter rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
                    CardPresenter cardPresenter = new CardPresenter();
                    HeaderItem header;
                    List <Movie> movieSpecificList;
                    List <BaseContent> contentList;

                    // ONGOING (MOVIES)
                    contentList = MovieList.setupOngoing(movieAPIClient);
                    ongoingMovies = new ArrayObjectAdapter(cardPresenter);
                    for (int j = 0; j < Math.min(contentList.size(), 20); j++) {
                        ongoingMovies.add(contentList.get(j));
                    }
                    header = new HeaderItem(rows++, "Ongoing");
                    rowsAdapter.add(new ListRow(header, ongoingMovies));

                    // ONGOING (SHOWS)
                    contentList = MovieList.setupOngoing(showAPIClient);
                    ongoingShows = new ArrayObjectAdapter(cardPresenter);
                    for (int j = 0; j < Math.min(contentList.size(), 20); j++) {
                        ongoingShows.add(contentList.get(j));
                    }
                    header = new HeaderItem(rows++, "Continue watching");
                    rowsAdapter.add(new ListRow(header, ongoingShows));

                    // WATCHLIST (MOVIES)
                    movieSpecificList = MovieList.setupMovieWatchlist(movieAPIClient);
                    movieWatchlist = new ArrayObjectAdapter(cardPresenter);
                    for (int j = 0; j < Math.min(movieSpecificList.size(), 20); j++) {
                        movieWatchlist.add(movieSpecificList.get(j));
                    }
                    header = new HeaderItem(rows++, "Watchlist");
                    rowsAdapter.add(new ListRow(header, movieWatchlist));

                    // NEWLY ADDED (MOVIES)
                    contentList = MovieList.setupNewlyAdded(movieAPIClient);
                    newlyAddedMovies = new ArrayObjectAdapter(cardPresenter);
                    for (int j = 0; j < Math.min(contentList.size(), 20); j++) {
                        newlyAddedMovies.add(contentList.get(j));
                    }
                    header = new HeaderItem(rows++, "New Movies");
                    rowsAdapter.add(new ListRow(header, newlyAddedMovies));

                    // NEWLY ADDED (SHOWS)
                    contentList = MovieList.setupNewlyAdded(showAPIClient);
                    newlyAddedShows = new ArrayObjectAdapter(cardPresenter);
                    for (int j = 0; j < Math.min(contentList.size(), 20); j++) {
                        newlyAddedShows.add(contentList.get(j));
                    }
                    header = new HeaderItem(rows++, "New Shows");
                    rowsAdapter.add(new ListRow(header, newlyAddedShows));

                    // NEW RELEASESE (MOVIES)
                    movieSpecificList = MovieList.setupNewlyReleasedMovies(movieAPIClient);
                    newReleasesMovies = new ArrayObjectAdapter(cardPresenter);
                    for (int j = 0; j < Math.min(movieSpecificList.size(), 20); j++) {
                        newReleasesMovies.add(movieSpecificList.get(j));
                    }
                    header = new HeaderItem(rows++, "New Releases");
                    rowsAdapter.add(new ListRow(header, newReleasesMovies));

                    HeaderItem gridHeader = new HeaderItem(rows, "PREFERENCES");

                    GridItemPresenter mGridPresenter = new GridItemPresenter();
                    ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);
                    gridRowAdapter.add(getResources().getString(R.string.grid_view));
                    gridRowAdapter.add(getString(R.string.error_fragment));
                    gridRowAdapter.add("Logout!");
                    rowsAdapter.add(new ListRow(gridHeader, gridRowAdapter));

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setAdapter(rowsAdapter);
                        }
                    });
                } catch(Exception e) {
                    e.printStackTrace();
                }

            }
        });
        thread.start();

    }

    private void prepareBackgroundManager() {

        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());

        mDefaultBackground = ContextCompat.getDrawable(getContext(), R.drawable.default_background);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void setupUIElements() {
        // setBadgeDrawable(getActivity().getResources().getDrawable(
        // R.drawable.videos_by_google_banner));
        setTitle(getString(R.string.browse_title)); // Badge, when set, takes precedent
        // over title
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);

        // set fastLane (or headers) background color
        setBrandColor(ContextCompat.getColor(getContext(), R.color.fastlane_background));
        // set search icon color
        setSearchAffordanceColor(ContextCompat.getColor(getContext(), R.color.search_opaque));
    }

    private void setupEventListeners() {
        setOnSearchClickedListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Implement your own in-app search", Toast.LENGTH_LONG)
                        .show();
            }
        });

        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }

    private void updateBackground(String uri) {
        int width = mMetrics.widthPixels;
        int height = mMetrics.heightPixels;
        Glide.with(getActivity())
                .asBitmap()
                .load(uri)
                .transform(new BlurTransformation(), new CenterCrop())
                .error(mDefaultBackground)
                .into(new CustomTarget<Bitmap>(width, height) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        mBackgroundManager.setBitmap(resource);

                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
        mBackgroundTimer.cancel();
    }

    private void startBackgroundTimer() {
        if (null != mBackgroundTimer) {
            mBackgroundTimer.cancel();
        }
        mBackgroundTimer = new Timer();
        mBackgroundTimer.schedule(new UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY);
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Movie) {
                Movie movie = (Movie) item;
                Log.d(TAG, "Item: " + item.toString());
                Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
                intent.putExtra(MovieDetailsActivity.MOVIE, movie);

                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        getActivity(),
                        itemViewHolder.view,
                        MovieDetailsActivity.SHARED_ELEMENT_NAME)
                        .toBundle();
                getActivity().startActivity(intent, bundle);
            } else if (item instanceof Show) {
                Show show = (Show) item;
                Log.d(TAG, "Item: " + item.toString());
                Intent intent = new Intent(getActivity(), com.dose.dose.details.ShowDetailsActivity.class);
                intent.putExtra(com.dose.dose.details.ShowDetailsActivity.SHOW, show);

                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        getActivity(),
                        itemViewHolder.view,
                        com.dose.dose.details.ShowDetailsActivity.SHARED_ELEMENT_NAME)
                        .toBundle();
                getActivity().startActivity(intent, bundle);
            } else if (item instanceof Episode) {
                Intent intent = new Intent(getActivity(), VideoActivity.class);
                intent.putExtra(VideoActivity.TYPE, VideoActivity.Type.EPISODE);
                intent.putExtra(VideoActivity.CONTINUE_WATCHING, true);
                intent.putExtra(VideoActivity.EPISODE, (Episode) item);

                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        getActivity(),
                        itemViewHolder.view,
                        VideoActivity.SHARED_ELEMENT_NAME)
                        .toBundle();
                getActivity().startActivity(intent, bundle);
            } else if (item instanceof String) {
                if (((String) item).contains(getString(R.string.error_fragment))) {
                    Intent intent = new Intent(getActivity(), BrowseErrorActivity.class);
                    startActivity(intent);
                } else if(((String) item).contains("Logout!")) {
                    Toast.makeText(getActivity(), "Logging Out", Toast.LENGTH_SHORT).show();
                    SharedPreferences settings = getActivity().getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.remove("MainServerJWT");
                    editor.remove("ContentServerURL");
                    editor.remove("ContentServerJWT");
                    editor.apply();
                    getActivity().finish();
                }
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(
                Presenter.ViewHolder itemViewHolder,
                Object item,
                RowPresenter.ViewHolder rowViewHolder,
                Row row) {
            if (item instanceof Movie ||
                item instanceof Show  ||
                item instanceof Episode) {
                mBackgroundUri = ((BaseContent) item).getCardImageUrl(true);
                Log.i("INSTANCEOF", mBackgroundUri);
                startBackgroundTimer();
            }
        }
    }

    private class UpdateBackgroundTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateBackground(mBackgroundUri);
                }
            });
        }
    }

    private class GridItemPresenter extends Presenter {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            TextView view = new TextView(parent.getContext());
            view.setLayoutParams(new ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT));
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.setBackgroundColor(
                    ContextCompat.getColor(getContext(), R.color.default_background));
            view.setTextColor(Color.WHITE);
            view.setGravity(Gravity.CENTER);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            ((TextView) viewHolder.view).setText((String) item);
        }

        @Override
        public void onUnbindViewHolder(ViewHolder viewHolder) {
        }
    }

}