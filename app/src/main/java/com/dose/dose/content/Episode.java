package com.dose.dose.content;

import android.os.Bundle;

import java.io.Serializable;

public class Episode extends BaseContent implements Serializable {
    private int episodeNumber;
    private Season season;
    private String backdrop;

    public Episode(String name, int episodeNumber, String backdrop, String overview, int internalId, float voteAverage, Season season) {
        super();
        this.episodeNumber = episodeNumber;
        this.title = name;
        this.backdrop = backdrop;
        this.season = season;
        this.setId(String.valueOf(internalId));
        this.setDescription(overview);
        setVoteAverage(voteAverage);
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

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    @Override
    public String getPosterImage(boolean originalQuality) {
        return this.season.getPosterImage(originalQuality);
    }

    @Override
    public String getCardImageUrl(boolean originalQuality) {
        return String.format("https://image.tmdb.org/t/p/%s/%s", originalQuality ? "original" : "w500", backdrop);
    }
}
