package com.dose.dose.search;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import androidx.fragment.app.Fragment;
import androidx.leanback.app.SearchSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.ObjectAdapter;

import com.dose.dose.R;

public class SearchFragment extends SearchSupportFragment implements SearchSupportFragment.SearchResultProvider {

    private static final int SEARCH_DELAY_MS = 300;
    private ArrayObjectAdapter rowsAdapter;
    private Handler handler = new Handler();
    //private SearchRunnable delayedLoad;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        setSearchResultProvider(this);
        //setOnItemClickedListener(getDefaultItemClickedListener());
        //delayedLoad = new SearchRunnable();
    }

    @Override
    public ObjectAdapter getResultsAdapter() {
        return rowsAdapter;
    }

    @Override
    public boolean onQueryTextChange(String newQuery) {
        rowsAdapter.clear();
        if (!TextUtils.isEmpty(newQuery)) {
            //delayedLoad.setSearchQuery(newQuery);
            //handler.removeCallbacks(delayedLoad);
            //handler.postDelayed(delayedLoad, SEARCH_DELAY_MS);
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        rowsAdapter.clear();
        if (!TextUtils.isEmpty(query)) {
            //delayedLoad.setSearchQuery(query);
            //andler.removeCallbacks(delayedLoad);
            //handler.postDelayed(delayedLoad, SEARCH_DELAY_MS);
        }
        return true;
    }
}