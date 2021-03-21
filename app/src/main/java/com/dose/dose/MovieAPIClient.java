package com.dose.dose;

import android.util.Log;

import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

public class MovieAPIClient extends DoseAPIClient {

    private String movieServerURL;

    public MovieAPIClient(String mainServerURL, String movieServerURL) {
        super(mainServerURL);
        this.movieServerURL = movieServerURL;
    }

    public MovieAPIClient(String mainServerURL, String movieServerURL, String mainServerToken, String movieServerToken) {
        super(mainServerURL, mainServerToken, movieServerToken);
        this.movieServerURL = movieServerURL;
    }


    @Override
    public String getPlaybackURL(String id, int startPos, String res) {
        return this.movieServerURL + String.format("/api/video/%s?type=movie&token=%s&start=%d&quality=%s", id, super.getMovieJWT(), startPos, res);
    }

    @Override
    public JSONArray getNewContent() {
        String url = this.movieServerURL + String.format("/api/movies/list?orderby=added_date&limit=20&token=%s", this.movieJWT);

        JSONArray result;
        try {
            result = super.customGet(url, new JSONObject()).getJSONArray("result");
        } catch(Exception e) {
            e.printStackTrace();
            result = new JSONArray();
        }
        Log.i("ALL THE FINE MOVIES: ", result.toString());
        return result;
    }

    @Override
    public JsonObject getOngoing() {
        return null;
    }

    @Override
    public int getDuration(String id) throws Exception {
        String url = this.movieServerURL + String.format("/api/video/%s/getDuration?type=movie&token=%s", id, super.getMovieJWT());
        return super.customGet(url, new JSONObject()).getInt("duration");
    }
}
