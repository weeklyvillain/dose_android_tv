package com.dose.dose;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;

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
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.dose.dose.ApiClient.MovieAPIClient;
import com.dose.dose.ApiClient.ShowAPIClient;
import com.dose.dose.content.BaseContent;
import com.dose.dose.content.Movie;

import org.json.JSONException;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);

        prepareBackgroundManager();

        SharedPreferences settings = this.getActivity().getSharedPreferences("UserInfo", 0);
        String JWT = settings.getString("MainServerJWT", "").toString();
        String mainServerURL = settings.getString("MainServerURL", "").toString();
        String contentServerURL = settings.getString("ContentServerURL", "").toString();
        String contentServerJWT = settings.getString("ContentServerJWT", "").toString();
        Log.i(TAG, contentServerURL);

        movieAPIClient = new MovieAPIClient(mainServerURL, contentServerURL, JWT, contentServerJWT);
        showAPIClient  = new ShowAPIClient(mainServerURL, contentServerURL, JWT, contentServerJWT);


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
                    ArrayObjectAdapter listRowAdapter;
                    HeaderItem header;
                    List <Movie> movieSpecificList;
                    List <BaseContent> contentList;

                    // ONGOING (MOVIES)
                    contentList = MovieList.setupOngoing(movieAPIClient);
                    listRowAdapter = new ArrayObjectAdapter(cardPresenter);
                    for (int j = 0; j < Math.min(contentList.size(), 20); j++) {
                        listRowAdapter.add(contentList.get(j));
                    }
                    header = new HeaderItem(rows++, "Ongoing");
                    rowsAdapter.add(new ListRow(header, listRowAdapter));

                    // WATCHLIST (MOVIES)
                    movieSpecificList = MovieList.setupMovieWatchlist(movieAPIClient);
                    listRowAdapter = new ArrayObjectAdapter(cardPresenter);
                    for (int j = 0; j < Math.min(movieSpecificList.size(), 20); j++) {
                        listRowAdapter.add(movieSpecificList.get(j));
                    }
                    header = new HeaderItem(rows++, "Watchlist");
                    rowsAdapter.add(new ListRow(header, listRowAdapter));

                    // NEWLY ADDED (MOVIES)
                    contentList = MovieList.setupNewlyAdded(movieAPIClient);
                    listRowAdapter = new ArrayObjectAdapter(cardPresenter);
                    for (int j = 0; j < Math.min(contentList.size(), 20); j++) {
                        listRowAdapter.add(contentList.get(j));
                    }
                    header = new HeaderItem(rows++, "New Movies");
                    rowsAdapter.add(new ListRow(header, listRowAdapter));

                    // NEWLY ADDED (SHOWS)
                    contentList = MovieList.setupNewlyAdded(showAPIClient);
                    listRowAdapter = new ArrayObjectAdapter(cardPresenter);
                    for (int j = 0; j < Math.min(contentList.size(), 20); j++) {
                        listRowAdapter.add(contentList.get(j));
                    }
                    header = new HeaderItem(rows++, "New Shows");
                    rowsAdapter.add(new ListRow(header, listRowAdapter));


                    // NEW RELEASESE (MOVIES)
                    movieSpecificList = MovieList.setupNewlyReleasedMovies(movieAPIClient);
                    listRowAdapter = new ArrayObjectAdapter(cardPresenter);
                    for (int j = 0; j < Math.min(movieSpecificList.size(), 20); j++) {
                        listRowAdapter.add(movieSpecificList.get(j));
                    }
                    header = new HeaderItem(rows++, "New Releases");
                    rowsAdapter.add(new ListRow(header, listRowAdapter));


                    /*
                    int i;
                    for (i = 0; i < NUM_ROWS; i++) {
                        if (i != 0) {
                            Collections.shuffle(list);
                        }
                        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
                        for (int j = 0; j < NUM_COLS; j++) {
                            listRowAdapter.add(list.get(j % 5));
                        }
                        HeaderItem header = new HeaderItem(i, MovieList.MOVIE_CATEGORY[i]);
                        rowsAdapter.add(new ListRow(header, listRowAdapter));
                    }
                    */

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
                .load(uri)
                .centerCrop()
                .error(mDefaultBackground)
                .into(new SimpleTarget<GlideDrawable>(width, height) {
                    @Override
                    public void onResourceReady(GlideDrawable resource,
                                                GlideAnimation<? super GlideDrawable>
                                                        glideAnimation) {
                        mBackgroundManager.setDrawable(resource);
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
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra(DetailsActivity.MOVIE, movie);

                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        getActivity(),
                        itemViewHolder.view,
                        DetailsActivity.SHARED_ELEMENT_NAME)
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
                    editor.remove("JWT");
                    editor.remove("ServerURL");
                    editor.commit();
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
            if (item instanceof Movie) {
                mBackgroundUri = ((Movie) item).getCardImageUrl(true);
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