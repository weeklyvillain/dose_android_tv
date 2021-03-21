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

    private static List<Movie> list;
    private static long count = 0;

    public static List<Movie> getList() throws JSONException {
        if (list == null) {
            //list = setupMovies();
        }
        return list;
    }

    public static List<Movie> setupMovies(MovieAPIClient movieAPIClient) throws JSONException {
        list = new ArrayList<>();
        String title[] = {
                "Back To The Future",
                "Iron Man",
                "The Flash",
                "Harry Potter",
                "Iron Man 2"
        };

        String description = "Fusce id nisi turpis. Praesent viverra bibendum semper. "
                + "Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est "
                + "quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit "
                + "amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit "
                + "facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id "
                + "lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.";
        String studio[] = {
                "Marty McFly, a 17-year-old high school student, is accidentally sent thirty years into the past in a time-traveling DeLorean invented by his close friend, the eccentric scientist Doc Brown.",
                "After being held captive in an Afghan cave, billionaire engineer Tony Stark creates a unique weaponized suit of armor to fight evil. ",
                "After being struck by lightning, Barry Allen wakes up from his coma to discover he's been given the power of super speed, becoming the next Flash, fighting crime in Central City.",
                "An orphaned boy enrolls in a school of wizardry, where he learns the truth about himself, his family and the terrible evil that haunts the magical world. ",
                "With the world now aware of his identity as Iron Man, Tony Stark must contend with both his declining health and a vengeful mad man with ties to his father's legacy. "
        };
        String videoUrl[] = {
                "https://movietrailers.apple.com/movies/disney/raya-and-the-last-dragon/raya-and-the-last-dragon-trailer-1_h480p.mov",
                "https://movietrailers.apple.com/movies/disney/raya-and-the-last-dragon/raya-and-the-last-dragon-trailer-1_h480p.mov", // IRON MAN TRAILER
                "https://movietrailers.apple.com/movies/disney/raya-and-the-last-dragon/raya-and-the-last-dragon-trailer-1_h480p.mov",
                "https://movietrailers.apple.com/movies/disney/raya-and-the-last-dragon/raya-and-the-last-dragon-trailer-1_h480p.mov",
                "https://movietrailers.apple.com/movies/disney/raya-and-the-last-dragon/raya-and-the-last-dragon-trailer-1_h480p.mov"
        };
        String bgImageUrl[] = {
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202010_%20Year%20in%20Review/bg.jpg",
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%2020ft%20Search/bg.jpg",
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Gmail%20Blue/bg.jpg",
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Fiber%20to%20the%20Pole/bg.jpg",
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Nose/bg.jpg",
        };
        String cardImageUrl[] = {
                "https://ae01.alicdn.com/kf/HTB1FovjaYr1gK0jSZR0q6zP8XXa2/7x5ft-Back-to-The-Future-Car-Race-Track-Clouds-Custom-Photo-Studio-Background-Backdrop-Vinyl-220cm.jpg",
                "https://comicattractions.com/wp-content/uploads/2019/03/iron-man-backdrop.jpg",
                "https://wallpaperaccess.com/full/708457.jpg",
                "https://archziner.com/wp-content/uploads/2020/02/poster-for-deathly-hallows-part-two-movie-wallpaper-harry-potter-battle-of-hogwarts-450x280.jpg",
                "https://wallpaperaccess.com/full/464869.jpg"
        };

        /*for (int index = 0; index < title.length; index++) {
            list.add(
                    buildMovieInfo(
                            title[index],
                            description,
                            studio[index],
                            videoUrl[index],
                            cardImageUrl[index],
                            bgImageUrl[index]));
        }*/

        JSONArray movies = movieAPIClient.getNewContent();

        SendPostRequest spr = new SendPostRequest();
        JSONObject JsonObj = spr.sendPost();
        JSONArray jArr = JsonObj.getJSONArray("result");
        String JWT = JsonObj.getString("token");
        if (jArr != null) {
            for (int i=0;i<jArr.length();i++) {
                Movie obj = buildMovieInfo(
                        jArr.getJSONObject(i).getString("id"),
                        jArr.getJSONObject(i).getString("title"),
                        jArr.getJSONObject(i).getString("overview"),
                        jArr.getJSONObject(i).getString("release_date"),
                        jArr.getJSONObject(i).getJSONArray("images"),
                        JWT);

                list.add(obj);
            }
        }
        Log.i("JSON ", JsonObj.toString());
        return  list;
    }

    private static Movie buildMovieInfo(
            String id,
            String title,
            String description,
            String release,
            JSONArray cardImageUrl,
            String JWT) {
        Movie movie = new Movie(id, title, description,release , cardImageUrl, JWT);
        return movie;
    }
}