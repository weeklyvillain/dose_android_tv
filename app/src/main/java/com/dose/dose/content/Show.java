package com.dose.dose.content;

import org.json.JSONArray;

import java.io.Serializable;

public class Show extends BaseContent implements Serializable {
    public Show(String id, String title, String overview, String release_date, JSONArray images, JSONArray genres, int watchTime)
    {
        super(id, title, overview, release_date, images, genres, watchTime);
    }

    @Override
    public String toString() {
        return "Show{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", overview='" + overview + '\'' +
                ", backgroundImageUrl='" + getCardImageUrl(true) + '\'' +
                '}';
    }


}
