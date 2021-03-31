package com.dose.dose.content;

import android.os.Bundle;

import org.json.JSONArray;

import java.io.Serializable;

public class Episode extends BaseContent implements Serializable {
    private int episodeNumber;
    private Season season;
    private String backdrop;

    private int seasonNumber;
    private int showId;
    private InfoLevel currentInfoLevel;
    private enum InfoLevel {
            FULL,
            LIMITED
    };


    public Episode(String name, int episodeNumber, String backdrop, String overview, int internalId, float voteAverage, Season season) {
        super();
        this.episodeNumber = episodeNumber;
        this.title = name;
        this.backdrop = backdrop;
        this.season = season;
        this.setId(String.valueOf(internalId));
        this.setDescription(overview);
        setVoteAverage(voteAverage);
        this.currentInfoLevel = InfoLevel.FULL;
    }

    /*
    THIS IS USED FOR ONGOING SHOWS WHERE WE DO NOT HAVE FULL INFORMATION ABOUT THE SHOW/SEASON
    */
    public Episode(String name, String overview, JSONArray images, int episodeNumber, int internalId, int watchTime, int totalTime, int seasonNumber, int showId, double lastWatched) {
        super();
        this.episodeNumber = episodeNumber;
        this.title = name;
        this.lastWatched = lastWatched;
        this.watchTime = watchTime;
        this.duration = totalTime;
        this.images = String.valueOf(images);
        this.seasonNumber = seasonNumber;
        this.showId = showId;

        this.setId(String.valueOf(internalId));
        this.setDescription(overview);
        this.currentInfoLevel = InfoLevel.LIMITED;
    }

    @Override
    public String toString() {
        return "Episode{" +
                "episodeNumber=" + episodeNumber +
                ", title='" + title + '\'' +
                ", backdrop='" + backdrop + '\'' +
                ", id='" + getId() + '\'' +
                ", overview='" + getDescription() + '\'' +
                ", voteAverage='" + getVoteAverage() + '\'' +
                '}';
    }

    public static Episode newInstance(String name, int episodeNumber, String backdrop, String overview, int internalId, float voteAverage, Season season) {
        return new Episode(name, episodeNumber, backdrop, overview, internalId, voteAverage, season);
    }

    /*
    THIS IS USED FOR ONGOING SHOWS WHERE WE DO NOT HAVE FULL INFORMATION ABOUT THE SHOW/SEASON
    */
    public static Episode newInstance(String name, String overview, JSONArray images, int episodeNumber, int internalId, int watchTime, int totalTime, int seasonNumber, int showId, double lastWatched) {

        return new Episode(name, overview, images, episodeNumber, internalId, watchTime, totalTime, seasonNumber, showId, lastWatched);
    }

    public int getSeasonNumber() {
        if (this.currentInfoLevel == InfoLevel.FULL) {
            return Integer.valueOf(this.season.getId());
        } else {
            return seasonNumber;
        }
    }

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    @Override
    public String getPosterImage(boolean originalQuality) {
        if (this.currentInfoLevel == InfoLevel.FULL) {
            return this.season.getPosterImage(originalQuality);
        } else {
            return super.getPosterImage(originalQuality);
        }
    }

    @Override
    public String getCardImageUrl(boolean originalQuality) {
        if (this.currentInfoLevel == InfoLevel.FULL) {
            return String.format("https://image.tmdb.org/t/p/%s/%s", originalQuality ? "original" : "w500", backdrop);
        } else {
            return super.getCardImageUrl(originalQuality);
        }
    }
}
