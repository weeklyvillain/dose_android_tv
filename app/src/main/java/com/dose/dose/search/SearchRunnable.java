package com.dose.dose.search;

import android.util.Log;

import com.dose.dose.ApiClient.DoseAPIClient;
import com.dose.dose.ApiClient.MovieAPIClient;

import org.json.JSONObject;

public class SearchRunnable implements Runnable {
    private DoseAPIClient doseAPIClient;
    private SearchListener searchListener;
    private String query;

    public SearchRunnable(DoseAPIClient doseAPIClient, SearchListener searchListener) {
        this.doseAPIClient  = doseAPIClient;
        this.searchListener = searchListener;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    public void run() {
        JSONObject result = doseAPIClient.search(query);
        searchListener.onResult(result);
        Log.i("SEARCH: ", result.toString());
    }

    public interface SearchListener {
        void onResult(JSONObject result);
    }
}
