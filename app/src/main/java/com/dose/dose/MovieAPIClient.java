package com.dose.dose;

import com.google.gson.JsonObject;

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
    public String getPlaybackURL(String id, String startPos, String res) {
        return this.movieServerURL + String.format("/api/video/%s?type=movie&token=%s&start=%s&quality=%s", id, super.getMovieJWT(), startPos, res);
    }

    @Override
    public JsonObject getNewContent() {
        return null;
    }

    @Override
    public JsonObject getOngoing() {
        return null;
    }
}
