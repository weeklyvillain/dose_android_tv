package com.dose.dose.ApiClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.dose.dose.ApiClient.DoseAPIClient;
import com.dose.dose.token.TokenHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class MovieAPIClient extends DoseAPIClient {

    public MovieAPIClient(String mainServerURL, String movieServerURL, Context context) {
        super(mainServerURL, movieServerURL, context);
        Log.i("HAIHAI", movieServerURL);
    }

    public static MovieAPIClient newInstance(Context context) {
        SharedPreferences settings =
                context.getSharedPreferences("UserInfo", 0);
        String mainServerURL = settings.getString("MainServerURL", "").toString();
        String contentServerURL = settings.getString("ContentServerURL", "").toString();

        return new MovieAPIClient(mainServerURL, contentServerURL, context);
    }

    @Override
    public JSONObject getResolution(String id) {
        String url = String.format("/api/video/%s/getResolution?type=movie&token=", id);
        JSONObject result = super.contentServerRequest(url);
        Log.i("Resolutions: ", result.toString());
        return result;
    }

    @Override
    public JSONArray getAudio(String id) {
        String url = String.format("/api/video/%s/getLanguages?type=movie&token=", id);
        JSONArray result = super.contentServerRequestArray(url);
        Log.i("Audio streams: ", result.toString());
        return result;
    }

    @Override
    public JSONObject getSubtitles(String id) {
        String url = String.format("/api/subtitles/list?content=%s&type=movie&token=", id);
        JSONObject result = super.contentServerRequest(url);
        Log.i("Subtitles: ", result.toString());
        return result;
    }

    @Override
    public String getSubtitleUrl(int subtitleId, int currentTime) {
        getNewTokensIfNeeded();
        String token = TokenHandler.Tokenhandler(context).getContentToken().getToken();
        return String.format(Locale.US, "%s/api/subtitles/get?type=movie&id=%d&start=%d&token=%s", super.movieServerURL, subtitleId, currentTime, token);
    }

    @Override
    public String getTrailer(String id) {
        return String.format("%s/api/trailer/%s?token=%s&type=MOVIE", super.movieServerURL, id, TokenHandler.Tokenhandler(context).getContentToken().getToken());
    }

    @Override
    public String getPlaybackURL(String id, int startPos, String res, int audioStream) {
        getNewTokensIfNeeded();

        String url = String.format(Locale.US, "%s/api/video/%s?type=movie&token=%s&start=%d&quality=%s", super.movieServerURL, id, TokenHandler.Tokenhandler(context).getContentToken().getToken(), startPos, res);
        if (audioStream != -1) {
            url = String.format(Locale.US, "%s&audio=%d", url, audioStream);
        }
        return url;
    }

    @Override
    public JSONArray getNewContent() {
        JSONArray result;
        try {
            result = super.contentServerRequest("/api/movies/list?orderby=added_date&limit=20&token=").getJSONArray("result");
        } catch(Exception e) {
            e.printStackTrace();
            result = new JSONArray();
        }
        Log.i("ALL THE FINE MOVIES: ", result.toString());
        return result;
    }


    public JSONArray getNewReleases() {
        JSONArray result;
        try {
            result = super.contentServerRequest("/api/movies/list?orderby=release_date&limit=20&token=").getJSONArray("result");
        } catch(Exception e) {
            e.printStackTrace();
            result = new JSONArray();
        }
        Log.i("NEWRELEASES: ", result.toString());
        return result;
    }

    @Override
    public JSONArray getOngoing() {
        JSONArray result;
        try {
            result = super.contentServerRequest("/api/movies/list/ongoing?limit=20&token=").getJSONArray("result");
        } catch(Exception e) {
            e.printStackTrace();
            result = new JSONArray();
        }
        Log.i("ONGOING: ", result.toString());
        return result;
    }

    @Override
    public JSONArray getWatchlist() {
        JSONArray result;
        try {
            result = super.contentServerRequest("/api/movies/list/watchlist?limit=20&token=").getJSONArray("result");
        } catch(Exception e) {
            e.printStackTrace();
            result = new JSONArray();
        }
        Log.i("MOVIEWATCHLIST: ", result.toString());
        return result;
    }

    @Override
    public int getDuration(String id) throws Exception {
        String url = String.format("/api/video/%s/getDuration?type=movie&token=", id);
        return super.contentServerRequest(url).getInt("duration");
    }

    @Override
    public void updateCurrentTime(String id, int time, int videoDuration) {
        String url = String.format(Locale.US, "/api/video/%s/currenttime/set?type=movie&time=%d&videoDuration=%s&token=", id, time, videoDuration);
        Log.i("UPDATECURRENTTIME: ", url);
        super.contentServerRequest(url);
    }

    @Override
    public JSONArray getByGenre(String genre) throws JSONException {
        String url = String.format(Locale.US, "/api/movies/list/genre/%s?token=", genre);
        return super.contentServerRequest(url).getJSONArray("result");
    }
}
