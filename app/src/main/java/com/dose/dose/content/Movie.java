package com.dose.dose.content;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;

import java.io.Serializable;

/*
 * Movie class represents video entity with title, description, image thumbs and video url.
 */
public class Movie extends BaseContent implements Serializable {

    public Movie(String id, String title, String overview, String release_date, JSONArray images, JSONArray genres, String JWT, int watchTime) {
        super(id, title, overview, release_date, images, genres, JWT, watchTime);
    }
    public Movie() {
        super();
    }

    @Override
    public String toString() {
        return "Movie{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", overview='" + overview + '\'' +
                ", genres='" + genres.toString() + '\'' +
                ", backgroundImageUrl='" + getCardImageUrl(true) + '\'' +
                '}';
    }
}