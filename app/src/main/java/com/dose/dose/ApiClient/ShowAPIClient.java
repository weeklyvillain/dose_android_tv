package com.dose.dose.ApiClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.dose.dose.content.Episode;
import com.dose.dose.token.TokenHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class ShowAPIClient extends DoseAPIClient {

    public ShowAPIClient(String mainServerURL, String movieServerURL, Context context) {
        super(mainServerURL, movieServerURL, context);
    }

    public static ShowAPIClient newInstance(Context context) {
        SharedPreferences settings =
                context.getSharedPreferences("UserInfo", 0);
        String mainServerURL = settings.getString("MainServerURL", "").toString();
        String contentServerURL = settings.getString("ContentServerURL", "").toString();

        return new ShowAPIClient(mainServerURL, contentServerURL, context);
    }

    @Override
    public String getPlaybackURL(String id, int startPos, String res) {
        getNewTokensIfNeeded();
        Log.i("PlaybackURL: ", String.format(Locale.US, "%s/api/video/%s?type=serie&token=%s&start=%d&quality=%s", this.movieServerURL, id, TokenHandler.Tokenhandler(context).getContentToken().getToken(), startPos, res));
        return String.format(Locale.US, "%s/api/video/%s?type=serie&token=%s&start=%d&quality=%s", this.movieServerURL, id, TokenHandler.Tokenhandler(context).getContentToken().getToken(), startPos, res);
    }

    @Override
    public JSONArray getNewContent() {
        JSONArray result;
        try {
            result = super.contentServerRequest("/api/series/list?orderby=added_date&limit=20&token=").getJSONArray("result");
        } catch(Exception e) {
            e.printStackTrace();
            result = new JSONArray();
        }
        Log.i("ALL THE FINE SHOWS: ", result.toString());
        return result;
    }

    public JSONArray getSeasons(String id) {
        String url = String.format("/api/series/%s?token=", id);
        JSONArray result;
        try {
            result = super.contentServerRequest(url).getJSONArray("seasons");
        } catch(Exception e) {
            e.printStackTrace();
            result = new JSONArray();
        }
        Log.i("ALLSEASONS: ", result.toString());
        return result;
    }

    public JSONObject getSeasonInformation(String showId, String season) {
        String url = String.format("/api/series/%s/season/%s?token=", showId, season);

        JSONObject result;
        try {
            result = super.contentServerRequest(url).getJSONObject("result");
        } catch(Exception e) {
            e.printStackTrace();
            result = new JSONObject();
        }
        Log.i("SEASONINFORMATION: ", result.toString());
        return result;
    }

    @Override
    public JSONArray getOngoing() {
        JSONObject result;
        JSONArray ongoing;
        JSONArray upcoming;

        JSONArray finalResults = new JSONArray();
        try {
            result = super.contentServerRequest("/api/series/list/ongoing?limit=20&token=");
            ongoing = result.getJSONArray("ongoing");
            upcoming = result.getJSONArray("upcoming");
            finalResults.put(0, ongoing);
            finalResults.put(1, upcoming);

            Log.i("SHOWONGOING: ", ongoing.toString());
            Log.i("SHOWUPCOMING: ", upcoming.toString());

        } catch(Exception e) {
            e.printStackTrace();
            finalResults = new JSONArray();
        }
        return finalResults;
    }

    @Override
    public int getDuration(String id) throws Exception {
        String url = super.movieServerURL + String.format("/api/video/%s/getDuration?type=serie&token=", id);
        return super.contentServerRequest(url).getInt("duration");
    }

    @Override
    public JSONArray getWatchlist() {
        return null;
    }

    @Override
    public void updateCurrentTime(String id, int time, int videoDuration) {
        String url = String.format(Locale.US, "/api/video/%s/currenttime/set?type=serie&time=%d&videoDuration=%s&token=", id, time, videoDuration);
        Log.i("UPDATECURRENTTIME: ", url);
        super.contentServerRequest(url);
    }

    @Override
    public JSONArray getByGenre(String genre) throws JSONException {
        return null;
    }

    public Episode getNextEpisode(Episode episode) {
        String url = String.format(Locale.US, "/api/series/getNextEpisode?serie_id=%s&season=%d&episode=%d&token=", episode.getShowId(), episode.getSeasonNumber(), episode.getEpisodeNumber());
        Log.i("GETNEXTEPISODE: ", url);

        // Get the episode_number and season number for the next episode
        JSONObject nextEpisodeInfo = super.contentServerRequest(url);
        boolean foundNextEpisode = false;
        try {
            foundNextEpisode = nextEpisodeInfo.getBoolean("foundEpisode");
        } catch (Exception e) {
            // Something has gone wrong, server down? Old access token?
            e.printStackTrace();
            return null;
        }

        if (foundNextEpisode) {
            try {
                url = String.format(Locale.US, "/api/series/%s/season/%d/episode/%d?token=",
                        episode.getShowId(),
                        nextEpisodeInfo.getInt("season"),
                        nextEpisodeInfo.getInt("episode"));
                JSONObject fullNextEpisodeJson = super.contentServerRequest(url);
                fullNextEpisodeJson = fullNextEpisodeJson.getJSONObject("result");
                return Episode.newInstance(
                        fullNextEpisodeJson.getString("name"),
                        fullNextEpisodeJson.getString("overview"),
                        fullNextEpisodeJson.getJSONArray("images"),
                        nextEpisodeInfo.getInt("episode"),
                        Integer.valueOf(fullNextEpisodeJson.getString("internalepisodeid")),
                        0,
                        -1,
                        nextEpisodeInfo.getInt("season"),
                        Integer.valueOf(episode.getShowId()),
                        0);
            } catch (Exception e) {
                // Something has gone wrong, server down? Old access token?
                e.printStackTrace();
                return null;
            }
        }

        // If the server doesn't have the next episode
        return null;
    }
}

