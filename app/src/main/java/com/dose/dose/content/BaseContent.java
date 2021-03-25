package com.dose.dose.content;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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

import org.json.JSONArray;

import java.io.Serializable;

public abstract class BaseContent implements Serializable  {
    static final long serialVersionUID = 727566175075960653L;

    protected String id;
    protected String title;
    protected String overview;
    protected String release_date;
    protected String images;
    protected String playBackUrl;
    protected int watchTime;
    protected int duration;

    public BaseContent() {
    }

    public BaseContent(String id, String title, String overview, String release_date, JSONArray images, String JWT, int watchTime) {
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.release_date = release_date;
        this.images = String.valueOf(images);
        this.watchTime = watchTime;
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

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getImages() {
        return this.images;
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
            if(active && type.equals("POSTER")) {
                imageURL = String.format("https://image.tmdb.org/t/p/%s/%s", originalQuality ? "original" : "w500", jObj.get("path").getAsString());
            }
        }
        Log.i("POSTERIMAGE: ", imageURL);
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
            if(active && type.equals("BACKDROP")) {
                imageURL = String.format("https://image.tmdb.org/t/p/%s/%s", originalQuality ? "original" : "w500", jObj.get("path").getAsString());
                break;
            }
        }
        return imageURL;
    }

    public void setImages(JsonArray images) {
        Gson g = new Gson();

        this.images = g.toJson(images);
    }
}