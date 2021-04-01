package com.dose.dose.ApiClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.dose.dose.content.Episode;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

public class ShowAPIClient extends DoseAPIClient {

    public ShowAPIClient(String mainServerURL, String movieServerURL, String mainServerToken, String movieServerToken) {
        super(mainServerURL, movieServerURL, mainServerToken, movieServerToken);
    }

    public static ShowAPIClient newInstance(Context context) {
        SharedPreferences settings =
                context.getSharedPreferences("UserInfo", 0);
        String JWT = settings.getString("MainServerJWT", "").toString();
        String mainServerURL = settings.getString("MainServerURL", "").toString();
        String contentServerURL = settings.getString("ContentServerURL", "").toString();
        String contentServerJWT = settings.getString("ContentServerJWT", "").toString();

        return new ShowAPIClient(mainServerURL, contentServerURL, JWT, contentServerJWT);
    }

    @Override
    public String getPlaybackURL(String id, int startPos, String res) {
        Log.i("PlaybackURL: ", this.movieServerURL + String.format("/api/video/%s?type=serie&token=%s&start=%d&quality=%s", id, super.getMovieJWT(), startPos, res));
        return this.movieServerURL + String.format("/api/video/%s?type=serie&token=%s&start=%d&quality=%s", id, super.getMovieJWT(), startPos, res);
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

    public JSONArray getSeasons(String id) {
        String url = String.format("%s/api/series/%s?token=%s", super.movieServerURL, id, super.getMovieJWT());
        JSONArray result;
        try {
            result = super.customGet(url, new JSONObject()).getJSONObject("result").getJSONArray("seasons");
        } catch(Exception e) {
            e.printStackTrace();
            result = new JSONArray();
        }
        Log.i("ALLSEASONS: ", result.toString());
        return result;
    }

    public JSONObject getSeasonInformation(String showId, String season) {
        String url = String.format("%s/api/series/%s/season/%s?token=%s", super.movieServerURL, showId, season, super.getMovieJWT());

        JSONObject result;
        try {
            result = super.customGet(url, new JSONObject()).getJSONObject("result");
        } catch(Exception e) {
            e.printStackTrace();
            result = new JSONObject();
        }
        Log.i("SEASONINFORMATION: ", result.toString());
        return result;
    }

    @Override
    public JSONArray getOngoing() {
        String url = super.movieServerURL + String.format("/api/series/list/ongoing?limit=20&token=%s", this.movieJWT);

        JSONObject result;
        JSONArray ongoing;
        JSONArray upcoming;

        JSONArray finalResults = new JSONArray();
        try {
            result = super.customGet(url, new JSONObject());
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
        String url = super.movieServerURL + String.format("/api/video/%s/getDuration?type=serie&token=%s", id, super.getMovieJWT());
        return super.customGet(url, new JSONObject()).getInt("duration");
    }

    @Override
    public JSONArray getWatchlist() {
        return null;
    }

    @Override
    public void updateCurrentTime(String id, int time, int videoDuration) {
        String url = String.format(Locale.US, "%s/api/video/%s/currenttime/set?type=serie&time=%d&videoDuration=%s&token=%s", super.movieServerURL, id, time, videoDuration, super.getMovieJWT());
        Log.i("UPDATECURRENTTIME: ", url);
        super.customGet(url, new JSONObject());
    }

    public Episode getNextEpisode(Episode episode) {
        String url = String.format(Locale.US, "%s/api/series/getNextEpisode?serie_id=%s&season=%d&episode=%d&token=%s", super.movieServerURL, episode.getShowId(), episode.getSeasonNumber(), episode.getEpisodeNumber(), super.getMovieJWT());
        Log.i("GETNEXTEPISODE: ", url);

        // Get the episode_number and season number for the next episode
        JSONObject nextEpisodeInfo = super.customGet(url, new JSONObject());
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
                url = String.format(Locale.US, "%s/api/series/%s/season/%d/episode/%d?token=%s",
                        super.movieServerURL,
                        episode.getShowId(),
                        nextEpisodeInfo.getInt("season"),
                        nextEpisodeInfo.getInt("episode"),
                        super.getMovieJWT());
                JSONObject fullNextEpisodeJson = super.customGet(url, new JSONObject());
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

