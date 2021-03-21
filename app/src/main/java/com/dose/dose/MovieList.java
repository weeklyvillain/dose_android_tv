package com.dose.dose;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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

    public static List<Movie> setupNewlyAddedMovies(MovieAPIClient movieAPIClient) throws JSONException {
        List<Movie> list = new ArrayList<>();
        JSONArray movies = movieAPIClient.getNewContent();

        if (movies != null) {
            for (int i=0;i<movies.length();i++) {
                Movie obj = buildMovieInfo(
                        movies.getJSONObject(i).getString("id"),
                        movies.getJSONObject(i).getString("title"),
                        movies.getJSONObject(i).getString("overview"),
                        movies.getJSONObject(i).getString("release_date"),
                        movies.getJSONObject(i).getJSONArray("images"),
                        movieAPIClient.movieJWT,
                        0); // TODO: We should get the watchtime for these movies aswell

                list.add(obj);
            }
        }
        return  list;
    }

    public static List<Movie> setupNewlyReleasedMovies(MovieAPIClient movieAPIClient) throws JSONException {
        List<Movie> list = new ArrayList<>();
        JSONArray movies = movieAPIClient.getNewReleases();

        if (movies != null) {
            for (int i=0;i<movies.length();i++) {
                Movie obj = buildMovieInfo(
                        movies.getJSONObject(i).getString("id"),
                        movies.getJSONObject(i).getString("title"),
                        movies.getJSONObject(i).getString("overview"),
                        movies.getJSONObject(i).getString("release_date"),
                        movies.getJSONObject(i).getJSONArray("images"),
                        movieAPIClient.movieJWT,
                        0); // TODO: We should get the watchtime for these movies aswell

                list.add(obj);
            }
        }
        return  list;
    }

    public static List<Movie> setupOngoingMovies(MovieAPIClient movieAPIClient) throws JSONException {
        List<Movie> list = new ArrayList<>();
        JSONArray movies = movieAPIClient.getOngoing();

        if (movies != null) {
            for (int i=0;i<movies.length();i++) {
                int watchTime = movies.getJSONObject(i).getInt("watchtime");
                Movie obj = buildMovieInfo(
                        movies.getJSONObject(i).getString("id"),
                        movies.getJSONObject(i).getString("title"),
                        movies.getJSONObject(i).getString("overview"),
                        movies.getJSONObject(i).getString("release_date"),
                        movies.getJSONObject(i).getJSONArray("images"),
                        movieAPIClient.movieJWT,
                        watchTime);

                list.add(obj);
            }
        }
        return  list;
    }

    public static List<Movie> setupMovieWatchlist(MovieAPIClient movieAPIClient) throws JSONException {
        List<Movie> list = new ArrayList<>();
        JSONArray movies = movieAPIClient.getWatchlist();

        if (movies != null) {
            for (int i=0;i<movies.length();i++) {
                Movie obj = buildMovieInfo(
                        movies.getJSONObject(i).getString("id"),
                        movies.getJSONObject(i).getString("title"),
                        movies.getJSONObject(i).getString("overview"),
                        movies.getJSONObject(i).getString("release_date"),
                        movies.getJSONObject(i).getJSONArray("images"),
                        movieAPIClient.movieJWT,
                        0); // TODO: We should get the watchtime for these movies aswell

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
            String JWT,
            int watchTime) {
        Movie movie = new Movie(id, title, description,release , cardImageUrl, JWT, watchTime);
        return movie;
    }
}