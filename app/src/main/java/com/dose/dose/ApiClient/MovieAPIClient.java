package com.dose.dose.ApiClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.dose.dose.ApiClient.DoseAPIClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class MovieAPIClient extends DoseAPIClient {

    public MovieAPIClient(String mainServerURL, String movieServerURL, String mainServerToken, String mainServerRefreshToken, String movieServerToken, String mainServerValidTo, String contentServerValidTo, Context context) {
        super(mainServerURL, movieServerURL, mainServerToken, mainServerRefreshToken, movieServerToken, mainServerValidTo, contentServerValidTo, context);
        Log.i("HAIHAI", movieServerURL);
    }

    public static MovieAPIClient newInstance(Context context) {
        SharedPreferences settings =
                context.getSharedPreferences("UserInfo", 0);
        String JWT = settings.getString("MainServerJWT", "").toString();
        String mainServerRefreshToken = settings.getString("MainServerRefreshToken", "");
        String mainServerValidTo = settings.getString("MainServerValidTo", "").toString();
        String mainServerURL = settings.getString("MainServerURL", "").toString();
        String contentServerURL = settings.getString("ContentServerURL", "").toString();
        String contentServerJWT = settings.getString("ContentServerJWT", "").toString();
        String contentServerValidTo = settings.getString("ContentServerValidTo", "").toString();

        return new MovieAPIClient(mainServerURL, contentServerURL, JWT, mainServerRefreshToken, contentServerJWT, mainServerValidTo, contentServerValidTo, context);
    }


    @Override
    public String getPlaybackURL(String id, int startPos, String res) {
        getNewTokensIfNeeded();
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

    @Override
    public void updateCurrentTime(String id, int time, int videoDuration) {
        String url = String.format(Locale.US, "%s/api/video/%s/currenttime/set?type=movie&time=%d&videoDuration=%s&token=%s", super.movieServerURL, id, time, videoDuration, super.getMovieJWT());
        Log.i("UPDATECURRENTTIME: ", url);
        super.customGet(url, new JSONObject());
    }

    @Override
    public JSONArray getByGenre(String genre) throws JSONException {
        String url = String.format(Locale.US, "%s/api/movies/list/genre/%s?token=%s", super.movieServerURL, genre, super.getMovieJWT());
        return super.customGet(url, new JSONObject()).getJSONArray("result");
    }
}
