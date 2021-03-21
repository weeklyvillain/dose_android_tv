package com.dose.dose;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/*
 * Movie class represents video entity with title, description, image thumbs and video url.
 */
public class Movie implements Serializable {
    static final long serialVersionUID = 727566175075960653L;
    private String id;
    private String title;
    private String overview;
    private String release_date;
    private String images;
    private String playBackUrl;
    private int watchTime;

    public Movie(String id, String title, String overview, String release_date, JSONArray images, String JWT, int watchTime) {
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.release_date = release_date;
        this.images = String.valueOf(images);
        this.watchTime = watchTime;
        this.playBackUrl = "https://vnc.fgbox.appboxes.co/doseserver/api/video/" + id+ "?type=movie&token=" + JWT + "&start=0&quality=720p";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPlayBackUrl() {
        return playBackUrl;
    }

    public String getDescription() {
        return overview;
    }

    public void setDescription(String overview) {
        this.overview = overview;
    }

    public String getReleaseDate() {
        return release_date;
    }

    public void setReleaseDate(String release_date) {
        this.release_date = release_date;
    }

    public int getWatchTime() {
        return watchTime;
    }

    public String getPosterImage(boolean originalQuality) {
        Gson g = new Gson();
        JsonArray arr = g.fromJson(images, JsonArray.class);
        String imageURL = "";
        Boolean active;
        String type;
        for (int i = 0; i < arr.size(); i++) {
            JsonObject jObj = (JsonObject) arr.get(i);
            active = jObj.get("active").getAsBoolean();
            type = jObj.get("type").getAsString();
            Log.i("IMAGETYPE", type);
            if(active && type.equals("POSTER")) {
                imageURL = String.format("https://image.tmdb.org/t/p/%s/%s", originalQuality ? "original" : "w500", jObj.get("path").getAsString());
            }
        }
        return imageURL;
    }

    public String getCardImageUrl(boolean originalQuality) {
        Gson g = new Gson();
        JsonArray arr = g.fromJson(images, JsonArray.class);
        String imageURL = "";
        Boolean active;
        String type;
        for (int i = 0; i < arr.size(); i++) {
            JsonObject jObj = (JsonObject) arr.get(i);
            active = jObj.get("active").getAsBoolean();
            type = jObj.get("type").getAsString();
            Log.i("IMAGETYPE", type);
            if(active && type.equals("BACKDROP")) {
                imageURL = String.format("https://image.tmdb.org/t/p/%s/%s", originalQuality ? "original" : "w500", jObj.get("path").getAsString());
            }
        }
        return imageURL;
    }

    public void setImages(JsonArray images) {
        Gson g = new Gson();

        this.images = g.toJson(images);
    }

    @Override
    public String toString() {
        return "Movie{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", overview='" + overview + '\'' +
                ", backgroundImageUrl='" + getCardImageUrl(true) + '\'' +
                '}';
    }
}