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

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class BrowseContentFragment extends RowsSupportFragment {
    // List row adapters
    private static ArrayObjectAdapter ongoingMovies;
    private static ArrayObjectAdapter ongoingShows;
    private static ArrayObjectAdapter movieWatchlist;
    private static ArrayObjectAdapter newlyAddedMovies;
    private static ArrayObjectAdapter newlyAddedShows;
    private static ArrayObjectAdapter newReleasesMovies;
    private MovieAPIClient movieAPIClient;
    private ShowAPIClient showAPIClient;

    private SelectedViewModel selectedViewModel;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        movieAPIClient = MovieAPIClient.newInstance(getActivity());
        showAPIClient = ShowAPIClient.newInstance(getActivity());
        selectedViewModel = new ViewModelProvider(requireActivity()).get(SelectedViewModel.class);

        try {
            loadRows();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void loadRows() throws JSONException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int rows = 0;
                    ListRowPresenter listRowPresenter = new ListRowPresenter(FocusHighlight.ZOOM_FACTOR_LARGE, true);
                    ArrayObjectAdapter rowsAdapter = new ArrayObjectAdapter(listRowPresenter);

                    CardPresenter cardPresenter = new CardPresenter();
                    HeaderItem header;
                    List<Movie> movieSpecificList;
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


                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setAdapter(rowsAdapter);
                            setOnItemViewSelectedListener(getItemSelectedListener());
                            setOnItemViewClickedListener(getItemClickedListener());
                        }
                    });
                } catch(Exception e) {
                    e.printStackTrace();
                }

            }
        });
        thread.start();

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
                    selectedViewModel.setSelected((BaseContent) item);
                }
            }
        };
    }

}