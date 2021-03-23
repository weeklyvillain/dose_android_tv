package com.dose.dose.ApiClient;

import android.util.Log;

import com.dose.dose.ApiClient.DoseAPIClient;

import org.json.JSONArray;
import org.json.JSONObject;

public class MovieAPIClient extends DoseAPIClient {

    public MovieAPIClient(String mainServerURL, String movieServerURL, String mainServerToken, String movieServerToken) {
        super(mainServerURL, movieServerURL, mainServerToken, movieServerToken);
        Log.i("HAIHAI", movieServerURL);
    }


    @Override
    public String getPlaybackURL(String id, int startPos, String res) {
        return super.movieServerURL + String.format("/api/video/%s?type=movie&token=%s&start=%d&quality=%s", id, super.getMovieJWT(), startPos, res);
    }

    @Override
    public JSONArray getNewContent() {
        String url = super.movieServerURL + String.format("/api/movies/list?orderby=added_date&limit=20&token=%s", this.movieJWT);

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


    public JSONArray getNewReleases() {
        String url = super.movieServerURL + String.format("/api/movies/list?orderby=release_date&limit=20&token=%s", this.movieJWT);

        JSONArray result;
        try {
            result = super.customGet(url, new JSONObject()).getJSONArray("result");
        } catch(Exception e) {
            e.printStackTrace();
            result = new JSONArray();
        }
        Log.i("NEWRELEASES: ", result.toString());
        return result;
    }

    @Override
    public JSONArray getOngoing() {
        String url = super.movieServerURL + String.format("/api/movies/list/ongoing?limit=20&token=%s", this.movieJWT);

        JSONArray result;
        try {
            result = super.customGet(url, new JSONObject()).getJSONArray("result");
        } catch(Exception e) {
            e.printStackTrace();
            result = new JSONArray();
        }
        Log.i("ONGOING: ", result.toString());
        return result;
    }

    @Override
    public JSONArray getWatchlist() {
        String url = super.movieServerURL + String.format("/api/movies/list/watchlist?limit=20&token=%s", this.movieJWT);

        JSONArray result;
        try {
            result = super.customGet(url, new JSONObject()).getJSONArray("result");
        } catch(Exception e) {
            e.printStackTrace();
            result = new JSONArray();
        }
        Log.i("MOVIEWATCHLIST: ", result.toString());
        return result;
    }

    @Override
    public int getDuration(String id) throws Exception {
        String url = super.movieServerURL + String.format("/api/video/%s/getDuration?type=movie&token=%s", id, super.getMovieJWT());
        return super.customGet(url, new JSONObject()).getInt("duration");
    }
}
