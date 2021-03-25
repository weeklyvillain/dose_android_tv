package com.dose.dose.content;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;

import java.io.Serializable;

public class Season extends BaseContent implements Serializable {
    private String name;
    private int season_id;
    private String poster_path;
    private Show show;

    public Season(String name, int season_id, String poster_path, Show show) {
        super();
        this.name = name;
        this.season_id = season_id;
        this.poster_path = poster_path;
        this.show = show;
        this.images = show.getImages();
    }

    @Override
    public String toString() {
        return "Season{" +
                "season_id=" + season_id +
                ", name='" + name + '\'' +
                ", poster_path='" + poster_path + '\'' +
                '}';
    }

    @Override
    public String getPosterImage(boolean originalQuality) {
        return String.format("https://image.tmdb.org/t/p/%s/%s", originalQuality ? "original" : "w500", poster_path);
    }

    @Override
    public String getTitle() {
        return this.name;
    }
}
