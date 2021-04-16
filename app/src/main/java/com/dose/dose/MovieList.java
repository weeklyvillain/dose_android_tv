package com.dose.dose;

import android.util.Log;

import com.dose.dose.ApiClient.DoseAPIClient;
import com.dose.dose.ApiClient.MovieAPIClient;
import com.dose.dose.ApiClient.ShowAPIClient;
import com.dose.dose.content.BaseContent;
import com.dose.dose.content.Episode;
import com.dose.dose.content.Movie;
import com.dose.dose.content.Season;
import com.dose.dose.content.Show;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class MovieList {
    public static final String MOVIE_CATEGORY[] = {
            "Pågående serier",
            "Pågående filmer",
            "Nya serier",
            "Pågående filmer",
            "övrigt",
            "annat",
    };

    private static List<Movie> list = new ArrayList<>();
    private static long count = 0;

    public static List<Movie> getList() throws JSONException {
        if (list == null) {
            //list = setupMovies();
        }
        return list;
    }

    public static List<BaseContent> setupByGenre(DoseAPIClient apiClient, String genre) throws JSONException {
        Log.i("HELLOO", "TEST");
        List<BaseContent> list = new ArrayList<>();
        JSONArray movies = apiClient.getByGenre(genre);
        Log.i(String.format("MOVIEGENRE %s", genre), movies.toString());

        for (int i=0;i<movies.length();i++) {
            BaseContent obj =
                    buildMovieInfo(
                        movies.getJSONObject(i).getString("id"),
                        movies.getJSONObject(i).getString("title"),
                        movies.getJSONObject(i).getString("overview"),
                        movies.getJSONObject(i).getString("release_date"),
                        movies.getJSONObject(i).getJSONArray("images"),
                        movies.getJSONObject(i).getJSONArray("genres"),
                        0); // TODO: We should get the watchtime for these movies aswell

            list.add(obj);
        }
        return list;
    }


    public static List<BaseContent> setupNewlyAdded(DoseAPIClient apiClient) throws JSONException {
        List<BaseContent> list = new ArrayList<>();
        JSONArray movies = apiClient.getNewContent();

        if (movies != null) {
            for (int i=0;i<movies.length();i++) {
                BaseContent obj;
                if (apiClient instanceof MovieAPIClient) {
                    obj = buildMovieInfo(
                            movies.getJSONObject(i).getString("id"),
                            movies.getJSONObject(i).getString("title"),
                            movies.getJSONObject(i).getString("overview"),
                            movies.getJSONObject(i).getString("release_date"),
                            movies.getJSONObject(i).getJSONArray("images"),
                            movies.getJSONObject(i).getJSONArray("genres"),
                            0); // TODO: We should get the watchtime for these movies aswell
                } else {
                    obj = buildShowInfo(
                            movies.getJSONObject(i).getString("id"),
                            movies.getJSONObject(i).getString("title"),
                            movies.getJSONObject(i).getString("overview"),
                            movies.getJSONObject(i).getString("first_air_date"),
                            movies.getJSONObject(i).getJSONArray("images"),
                            movies.getJSONObject(i).getJSONArray("genres"),
                            0); // TODO: We should get the watchtime for these movies aswell
                }

                list.add(obj);
            }
        }
        return list;
    }

    public static List<BaseContent> setupNewlyReleasedMovies(MovieAPIClient movieAPIClient) throws JSONException {
        List<BaseContent> list = new ArrayList<>();
        JSONArray movies = movieAPIClient.getNewReleases();

        if (movies != null) {
            for (int i=0;i<movies.length();i++) {
                Movie obj = buildMovieInfo(
                        movies.getJSONObject(i).getString("id"),
                        movies.getJSONObject(i).getString("title"),
                        movies.getJSONObject(i).getString("overview"),
                        movies.getJSONObject(i).getString("release_date"),
                        movies.getJSONObject(i).getJSONArray("images"),
                        movies.getJSONObject(i).getJSONArray("genres"),
                        0); // TODO: We should get the watchtime for these movies aswell

                list.add(obj);
            }
        }
        return list;
    }

    public static List<BaseContent> setupOngoing(DoseAPIClient apiClient) throws JSONException {
        List<BaseContent> list = new ArrayList<>();
        JSONArray content = apiClient.getOngoing();

        if (content != null) {
            for (int i=0;i<content.length();i++) {
                BaseContent obj;
                if (apiClient instanceof MovieAPIClient) {
                    int watchTime = content.getJSONObject(i).getInt("watchtime");
                    obj = buildMovieInfo(
                            content.getJSONObject(i).getString("id"),
                            content.getJSONObject(i).getString("title"),
                            content.getJSONObject(i).getString("overview"),
                            content.getJSONObject(i).getString("release_date"),
                            content.getJSONObject(i).getJSONArray("images"),
                            content.getJSONObject(i).getJSONArray("genres"),
                            watchTime);
                    list.add(obj);
                } else {
                    // TODO: This places upcoming episodes at the end, we have to sort them based last_watched
                    boolean ongoing = i == 0;

                    // If we are getting ongoing shows the apiClient will return both ongoing and upcoming. It will look something like this:
                    // content = [{ongoing}, {upcoming}] Where ongoing is a JSONObject and upcoming is another upcoming
                    for (int j = 0; j < content.getJSONArray(i).length(); j++) {
                        JSONObject currentJsonObj = (JSONObject) content.getJSONArray(i).get(j);
                        int timeWatched = ongoing ? currentJsonObj.getInt("time_watched") : 0;
                        int totalTime = ongoing ? currentJsonObj.getInt("total_time") : -1;
                            //(String name, String overview, JSONArray images, int episodeNumber, int internalId, int watchTime, int totalTime, int seasonNumber, int showId, int lastWatched)
                        obj = Episode.newInstance(
                                currentJsonObj.getString("name"),
                                currentJsonObj.getString("overview"),
                                currentJsonObj.getJSONArray("images"),
                                currentJsonObj.getInt("episode_number"),
                                Integer.valueOf(currentJsonObj.getString("internalepisodeid")),
                                timeWatched,
                                totalTime,
                                currentJsonObj.getInt("season_number"),
                                currentJsonObj.getInt("show_id"),
                                Double.valueOf(currentJsonObj.getString("last_watched"))
                        );
                        list.add(obj);
                    }
                }
            }
        }
        return  list;
    }

    public static List<BaseContent> setupMovieWatchlist(MovieAPIClient movieAPIClient) throws JSONException {
        List<BaseContent> list = new ArrayList<>();
        JSONArray movies = movieAPIClient.getWatchlist();

        if (movies != null) {
            for (int i=0;i<movies.length();i++) {
                Movie obj = buildMovieInfo(
                        movies.getJSONObject(i).getString("id"),
                        movies.getJSONObject(i).getString("title"),
                        movies.getJSONObject(i).getString("overview"),
                        movies.getJSONObject(i).getString("release_date"),
                        movies.getJSONObject(i).getJSONArray("images"),
                        movies.getJSONObject(i).getJSONArray("genres"),
                        0); // TODO: We should get the watchtime for these movies aswell

                list.add(obj);
            }
        }
        return list;
    }

    public static List<Season> setupSeasons(ShowAPIClient showAPIClient, Show show) throws JSONException {
        List<Season> list = new ArrayList<>();
        JSONArray seasons = showAPIClient.getSeasons(show.getId());

        if (seasons != null) {
            for (int i=0;i<seasons.length();i++) {
                Log.i("JSONOBJECTOVERVIEW: ", seasons.getJSONObject(i).toString());
                Season obj = buildSeasonInfo(
                        seasons.getJSONObject(i).getString("name"),
                        Integer.parseInt(seasons.getJSONObject(i).getString("season_id")),
                        seasons.getJSONObject(i).getString("poster_path"),
                        show); // TODO: We should get the watchtime for these movies aswell

                list.add(obj);
            }
        }
        return  list;
    }

    private static Movie buildMovieInfo(
            String id,
            String title,
            String description,
            String release,
            JSONArray cardImageUrl,
            JSONArray genres,
            int watchTime) {
        Movie movie = new Movie(id, title, description,release , cardImageUrl, genres, watchTime);
        return movie;
    }

    private static Show buildShowInfo(
            String id,
            String title,
            String description,
            String release,
            JSONArray cardImageUrl,
            JSONArray genres,
            int watchTime) {
        return new Show(id, title, description, release, cardImageUrl, genres, watchTime);
    }

    private static Season buildSeasonInfo(
            String name,
            int season_id,
            String poster_path,
            Show show) {
        return new Season(name, season_id, poster_path, show);
    }
}