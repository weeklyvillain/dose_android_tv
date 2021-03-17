package com.dose.dose;

import android.util.Log;

import com.google.gson.JsonObject;

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
    public JsonObject getNewContent() {
        return null;
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
