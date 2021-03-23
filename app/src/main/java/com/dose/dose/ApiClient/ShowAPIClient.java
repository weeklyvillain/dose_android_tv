package com.dose.dose.ApiClient;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

public class ShowAPIClient extends DoseAPIClient {

    public ShowAPIClient(String mainServerURL, String movieServerURL, String mainServerToken, String movieServerToken) {
        super(mainServerURL, movieServerURL, mainServerToken, movieServerToken);
    }

    @Override
    public String getPlaybackURL(String id, int startPos, String res) {
        return this.movieServerURL + String.format("/api/video/%s?type=show&token=%s&start=%d&quality=%s", id, super.getMovieJWT(), startPos, res);
    }

    @Override
    public JSONArray getNewContent() {
        String url = this.movieServerURL + String.format("/api/series/list?orderby=added_date&limit=20&token=%s", this.movieJWT);
        JSONArray result;
        try {
            result = super.customGet(url, new JSONObject()).getJSONArray("result");
        } catch(Exception e) {
            e.printStackTrace();
            result = new JSONArray();
        }
        Log.i("ALL THE FINE SHOWS: ", result.toString());
        return result;
    }

    @Override
    public JSONArray getOngoing() {
        return null;
    }

    @Override
    public int getDuration(String id) throws Exception {
        return 0;
    }

    @Override
    public JSONArray getWatchlist() {
        return null;
    }
}

