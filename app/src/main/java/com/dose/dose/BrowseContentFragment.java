package com.dose.dose;

import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.leanback.app.RowsSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ClassPresenterSelector;
import androidx.leanback.widget.FocusHighlight;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dose.dose.ApiClient.MovieAPIClient;
import com.dose.dose.ApiClient.ShowAPIClient;
import com.dose.dose.content.BaseContent;
import com.dose.dose.content.Episode;
import com.dose.dose.content.Movie;
import com.dose.dose.content.Show;
import com.dose.dose.details.MovieDetailsActivity;
import com.dose.dose.viewModels.SelectedViewModel;

import org.json.JSONException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class BrowseContentFragment extends RowsSupportFragment {
    private CardPresenter cardPresenter;
    private ListRowPresenter listRowPresenter;
    private ArrayObjectAdapter rowsAdapter;
    private int rows = 0;

    // List row adapters that uses data
    private static ArrayObjectAdapter ongoingMovies;
    private static ArrayObjectAdapter ongoingShows;
    private static ArrayObjectAdapter movieWatchlist;
    private static ArrayObjectAdapter newlyAddedMovies;
    private static ArrayObjectAdapter newlyAddedShows;
    private static ArrayObjectAdapter newReleasesMovies;
    private static List<Pair<HeaderItem, ArrayObjectAdapter>> movieGenres;
    private MovieAPIClient movieAPIClient;
    private ShowAPIClient showAPIClient;

    private SelectedViewModel selectedViewModel;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        movieAPIClient = MovieAPIClient.newInstance(getActivity());
        showAPIClient = ShowAPIClient.newInstance(getActivity());
        selectedViewModel = new ViewModelProvider(requireActivity()).get(SelectedViewModel.class);

        setupRows();
        setupGenres();
        try {
            loadRows();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void setupGenres() {
        movieGenres = new ArrayList<>();
        new Thread(() -> {
            List<String> genres = movieAPIClient.getGenres();
            int rowsAdapterInitialLength = rowsAdapter.size();
            for (String genre : genres) {
                // Make first letter uppercase
                genre = genre.substring(0, 1).toUpperCase() + genre.substring(1);
                // Create an adapter for this row
                ArrayObjectAdapter adapter = new ArrayObjectAdapter(cardPresenter);
                // Greate a new header and add the new adapter to the view
                HeaderItem header = new HeaderItem(rows++, genre);

                // Save the adapter as a pair with the genre name
                movieGenres.add(new Pair<>(header, adapter));
                rowsAdapter.add(new ListRow(header, adapter));
            }
            // Notify rowsAdapter that new rows has been added
            rowsAdapter.notifyArrayItemRangeChanged(rowsAdapterInitialLength, rowsAdapter.size() - rowsAdapterInitialLength);
            loadGenres();
        }).start();
    }

    private void loadGenres() {
        for (Pair<HeaderItem, ArrayObjectAdapter> genre : movieGenres) {
            new Thread(() -> {
                try {
                    List<BaseContent> contentList = MovieList.setupByGenre(movieAPIClient, genre.first.getName().toLowerCase());
                    requireActivity().runOnUiThread(() -> setAdapterContent(contentList, genre.second));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void setupRows() {
        cardPresenter = new CardPresenter();
        listRowPresenter = new ListRowPresenter(FocusHighlight.ZOOM_FACTOR_LARGE, true);
        rowsAdapter = new ArrayObjectAdapter(listRowPresenter);
        HeaderItem header;

        ongoingMovies = new ArrayObjectAdapter(cardPresenter);
        header = new HeaderItem(rows++, "Ongoing");
        rowsAdapter.add(new ListRow(header, ongoingMovies));

        ongoingShows = new ArrayObjectAdapter(cardPresenter);
        header = new HeaderItem(rows++, "Continue watching");
        rowsAdapter.add(new ListRow(header, ongoingShows));

        movieWatchlist = new ArrayObjectAdapter(cardPresenter);
        header = new HeaderItem(rows++, "Watchlist");
        rowsAdapter.add(new ListRow(header, movieWatchlist));

        newlyAddedMovies = new ArrayObjectAdapter(cardPresenter);
        header = new HeaderItem(rows++, "New Movies");
        rowsAdapter.add(new ListRow(header, newlyAddedMovies));

        newlyAddedShows = new ArrayObjectAdapter(cardPresenter);
        header = new HeaderItem(rows++, "New Shows");
        rowsAdapter.add(new ListRow(header, newlyAddedShows));

        newReleasesMovies = new ArrayObjectAdapter(cardPresenter);
        header = new HeaderItem(rows++, "New Releases");
        rowsAdapter.add(new ListRow(header, newReleasesMovies));

        setAdapter(rowsAdapter);
        setOnItemViewSelectedListener(getItemSelectedListener());
        setOnItemViewClickedListener(getItemClickedListener());
    }

    private void setAdapterContent(List<BaseContent> content, ArrayObjectAdapter adapter) {
        for (int j = 0; j < Math.min(content.size(), 20); j++) {
            adapter.add(content.get(j));
        }
        synchronized (adapter) {
            adapter.notifyAll();
        }
    }

    private void loadRows() throws JSONException {
        // Load ongoing movies
        new Thread(() -> {
            final List<BaseContent> contentList;
            try {
                contentList = MovieList.setupOngoing(movieAPIClient);
                requireActivity().runOnUiThread(() -> setAdapterContent(contentList, ongoingMovies));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // Load ongoing shows
        new Thread(() -> {
            final List<BaseContent> contentList;
            try {
                contentList = MovieList.setupOngoing(showAPIClient);
                requireActivity().runOnUiThread(() -> setAdapterContent(contentList, ongoingShows));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // Load movie watchlist
        new Thread(() -> {
            final List<BaseContent> contentList;
            try {
                contentList = MovieList.setupMovieWatchlist(movieAPIClient);
                requireActivity().runOnUiThread(() -> setAdapterContent(contentList, movieWatchlist));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // Load newly added movies
        new Thread(() -> {
            final List<BaseContent> contentList;
            try {
                contentList = MovieList.setupNewlyAdded(movieAPIClient);
                requireActivity().runOnUiThread(() -> setAdapterContent(contentList, newlyAddedMovies));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // Load newly added shows
        new Thread(() -> {
            final List<BaseContent> contentList;
            try {
                contentList = MovieList.setupNewlyAdded(showAPIClient);
                requireActivity().runOnUiThread(() -> setAdapterContent(contentList, newlyAddedShows));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // Load new releases
        new Thread(() -> {
            final List<BaseContent> contentList;
            try {
                contentList = MovieList.setupNewlyReleasedMovies(movieAPIClient);
                requireActivity().runOnUiThread(() -> setAdapterContent(contentList, newReleasesMovies));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }

    private OnItemViewClickedListener getItemClickedListener() {
        return new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                if (item instanceof Movie) {
                    Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
                    intent.putExtra(MovieDetailsActivity.MOVIE, (Movie) item);

                    Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            getActivity(),
                            itemViewHolder.view,
                            MovieDetailsActivity.SHARED_ELEMENT_NAME)
                            .toBundle();
                    getActivity().startActivity(intent, bundle);
                } else if (item instanceof Show) {
                    Intent intent = new Intent(getActivity(), com.dose.dose.details.ShowDetailsActivity.class);
                    intent.putExtra(com.dose.dose.details.ShowDetailsActivity.SHOW, (Show) item);

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
                }
            }
        };
    }

    private OnItemViewSelectedListener getItemSelectedListener() {
        return new OnItemViewSelectedListener() {
            @Override
            public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                if (item != null) {
                    Log.i("Selected: ", ((BaseContent)item).getGenres());
                    selectedViewModel.setSelected((BaseContent) item);
                }
            }
        };
    }

}