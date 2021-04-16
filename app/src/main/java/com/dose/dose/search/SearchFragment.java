package com.dose.dose.search;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.leanback.app.SearchSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.ObjectAdapter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;

import com.dose.dose.ApiClient.MovieAPIClient;
import com.dose.dose.CardPresenter;
import com.dose.dose.R;
import com.dose.dose.VideoActivity;
import com.dose.dose.content.Episode;
import com.dose.dose.content.Movie;
import com.dose.dose.content.Show;
import com.dose.dose.details.MovieDetailsActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;

public class SearchFragment extends SearchSupportFragment implements SearchSupportFragment.SearchResultProvider {

    private static final int SEARCH_DELAY_MS = 300;
    private ArrayObjectAdapter rowsAdapter;
    private final HandlerThread ht = new HandlerThread("searchHandler");
    private Handler handler;

    private MovieAPIClient movieAPIClient;
    private SearchRunnable.SearchListener searchListener;
    private SearchRunnable delayedLoad;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupListener();

        ht.start();
        handler = new Handler(ht.getLooper());
        movieAPIClient = MovieAPIClient.newInstance(requireContext());
        delayedLoad = new SearchRunnable(movieAPIClient, searchListener);
        rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        setSearchResultProvider(this);
        setOnItemViewClickedListener(getItemClickedListener());
    }

    @Override
    public ObjectAdapter getResultsAdapter() {
        return rowsAdapter;
    }

    @Override
    public boolean onQueryTextChange(String newQuery) {
        rowsAdapter.clear();
        if (!TextUtils.isEmpty(newQuery)) {
            delayedLoad.setQuery(newQuery);
            handler.removeCallbacks(delayedLoad);
            handler.postDelayed(delayedLoad, SEARCH_DELAY_MS);
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        rowsAdapter.clear();
        if (!TextUtils.isEmpty(query)) {
            delayedLoad.setQuery(query);
            handler.removeCallbacks(delayedLoad);
            handler.postDelayed(delayedLoad, SEARCH_DELAY_MS);
        }
        return true;
    }

    private void setupListener() {
        searchListener = result -> {
            HeaderItem header;
            CardPresenter cardPresenter = new CardPresenter();
            int rows = 0;

            JSONArray movieList = new JSONArray();
            JSONArray showList  = new JSONArray();
            try {
                movieList = result.getJSONArray("movies");
                showList  = result.getJSONArray("series");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (movieList.length() != 0) {
                header = new HeaderItem(rows++, "Movies");
                ArrayObjectAdapter movies = new ArrayObjectAdapter(cardPresenter);

                for (int i = 0; i < movieList.length(); i++) {
                    try {
                        Movie movie = new Movie(movieList.getJSONObject(i).getString("id"),
                                movieList.getJSONObject(i).getString("title"),
                                movieList.getJSONObject(i).getString("overview"),
                                movieList.getJSONObject(i).getString("release_date"),
                                movieList.getJSONObject(i).getJSONArray("images"),
                                movieList.getJSONObject(i).getJSONArray("genres"),
                                movieAPIClient.getMovieJWT(),
                                0);
                        movies.add(movie);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                rowsAdapter.add(new ListRow(header, movies));
            }

            if (showList.length() != 0) {
                header = new HeaderItem(rows+1, "Shows");
                ArrayObjectAdapter shows = new ArrayObjectAdapter(cardPresenter);

                for (int i = 0; i < showList.length(); i++) {
                    try {
                        Show show = new Show(showList.getJSONObject(i).getString("id"),
                                showList.getJSONObject(i).getString("title"),
                                showList.getJSONObject(i).getString("overview"),
                                showList.getJSONObject(i).getString("first_air_date"),
                                showList.getJSONObject(i).getJSONArray("images"),
                                showList.getJSONObject(i).getJSONArray("genres"),
                                movieAPIClient.getMovieJWT(),
                                0);
                        shows.add(show);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                rowsAdapter.add(new ListRow(header, shows));
            }



        };
    }

    private OnItemViewClickedListener getItemClickedListener() {
        return new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                if (item instanceof Movie) {
                    Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
                    intent.putExtra(MovieDetailsActivity.MOVIE, (Movie) item);

                    Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            requireActivity(),
                            itemViewHolder.view,
                            MovieDetailsActivity.SHARED_ELEMENT_NAME)
                            .toBundle();
                    requireActivity().startActivity(intent, bundle);
                } else if (item instanceof Show) {
                    Intent intent = new Intent(getActivity(), com.dose.dose.details.ShowDetailsActivity.class);
                    intent.putExtra(com.dose.dose.details.ShowDetailsActivity.SHOW, (Show) item);

                    Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            requireActivity(),
                            itemViewHolder.view,
                            com.dose.dose.details.ShowDetailsActivity.SHARED_ELEMENT_NAME)
                            .toBundle();
                    requireActivity().startActivity(intent, bundle);
                }
            }
        };
    }


}