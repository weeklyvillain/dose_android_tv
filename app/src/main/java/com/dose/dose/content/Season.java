package com.dose.dose.content;

import android.util.Log;

import androidx.databinding.library.baseAdapters.BR;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;

import java.io.Serializable;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class Season extends BaseContent implements Serializable {
    private String seasonName;
    private int season_id;
    private String poster_path;
    private Show show;
    private List<Episode> episodes = new ArrayList<>();

    public Season(String name, int season_id, String poster_path, Show show) {
        super();
        this.seasonName = name;
        super.id = String.valueOf(season_id);
        this.poster_path = poster_path;
        this.show = show;
        this.images = show.getImages();
        this.hasValidData = true;
    }

    @Override
    public String toString() {
        return "Season{" +
                "season_id=" + season_id +
                ", name='" + seasonName + '\'' +
                ", poster_path='" + poster_path + '\'' +
                '}';
    }

    @Override
    public String getPosterImage(boolean originalQuality) {
        return String.format("https://image.tmdb.org/t/p/%s/%s", originalQuality ? "original" : "w500", poster_path);
    }

    @Override
    public String getCardImageUrl(boolean originalQuality) {
        return this.show.getCardImageUrl(originalQuality);
    }

    @Override
    public String getTitle() {
        return this.seasonName;
    }

    @Override
    public String getGenres() {
        return this.show.getGenres();
    }

    public String getSeasonTitle() {
        return this.seasonName;
    }

    public String getFullTitle() {
        return String.format("%s - %s", this.show.getTitle(), this.getSeasonTitle());
    }

    public Show getShow() {
        return this.show;
    }

    public void addEpisode(Episode episode) {
        episodes.add(episode);
        notifyPropertyChanged(BR.season);
    }

    public String getShowId() {
        return this.show.getId();
    }

    public List<Episode> getEpisodes() {
        return episodes;
    }
}
