package com.dose.dose.content;

import android.util.Log;

import com.dose.dose.controls.ControlSetting;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import androidx.databinding.BaseObservable;
import androidx.databinding.library.baseAdapters.BR;

import org.json.JSONArray;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


// TODO: Fix so ids are int
public abstract class BaseContent extends BaseObservable implements Serializable  {
    static final long serialVersionUID = 727566175075960653L;

    protected String id;
    protected String title;
    protected String overview;
    protected String release_date;
    protected String images;
    protected String playBackUrl;
    protected double lastWatched;
    protected int watchTime;
    protected int duration;
    protected float voteAverage;
    protected ArrayList<String> genres;
    protected List<ControlSetting> availableResolutions;
    protected List<ControlSetting> availableAudioStreams;
    protected List<ControlSetting> availableSubtitles;

    public BaseContent() {
    }

    public BaseContent(String id, String title, String overview, String release_date, JSONArray images, JSONArray genres, int watchTime) {
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.release_date = release_date;
        this.images = String.valueOf(images);
        this.watchTime = watchTime;
        this.genres = new ArrayList<>();
        try {
            if (genres != null) {
                for (int i = 0; i < genres.length(); i++) {
                    this.genres.add(genres.get(i).toString());
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    public void setAvailableSubtitles(List<ControlSetting> availableSubtitles) {
        this.availableSubtitles = availableSubtitles;
    }

    public List<ControlSetting> getAvailableSubtitles() {
        return availableSubtitles;
    }

    public void setAvailableAudioStreams(List<ControlSetting> availableAudioStreams) {
        this.availableAudioStreams = availableAudioStreams;
    }

    public List<ControlSetting> getAvailableAudioStreams() {
        return availableAudioStreams;
    }

    public void setAvailableResolutions(List<ControlSetting> availableResolutions) {
        this.availableResolutions = availableResolutions;
    }

    public List<ControlSetting> getAvailableResolutions() {
        return this.availableResolutions;
    }

    public void setGenres(ArrayList<String> genres) {
        this.genres = genres;
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
        notifyPropertyChanged(BR._all);
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

    public void setWatchTime(int watchTime) {
        this.watchTime = watchTime;
    }

    public String getReadableDuration() {
        int hours = duration / 60 / 60;
        int minutes = (duration / 60) % 60;
        String text = "";
        if (hours > 0) {
            text += String.format("%dh", hours);
        }
        text += String.format(" %dmin", minutes);
        return text;
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

    public String getRuntime() {return "";}

    public ArrayList<String> getGenresList() {
        return genres;
    }

    public String getGenres() {
        return getGenres("/ ");
    }

    public String getGenres(String divider) {
        StringBuilder text = new StringBuilder();
        if (this.genres == null) {
            return "";
        }
        for (int i = 0; i < genres.size(); i++) {
            String genre = genres.get(i).substring(0, 1).toUpperCase() + genres.get(i).substring(1);
            text.append(genre);
            if (i != genres.size()-1) {
                text.append(String.format("%s", divider));
            }
        }
        return text.toString();
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

    public String getLogoImageUrl(boolean originalQuality) {
        Gson g = new Gson();
        JsonArray arr = g.fromJson(images, JsonArray.class);
        String imageURL = "";
        Boolean active;
        String type;
        for (int i = 0; i < arr.size(); i++) {
            JsonObject jObj = (JsonObject) arr.get(i);
            active = jObj.get("active").getAsBoolean();
            type = jObj.get("type").getAsString();
            if(active && type.equals("LOGO")) {
                imageURL = String.format("https://image.tmdb.org/t/p/%s/%s", originalQuality ? "original" : "w500", jObj.get("path").getAsString());
                break;
            }
        }
        return imageURL;
    }

    public Boolean gotLogo() {
        Gson g = new Gson();
        JsonArray arr = g.fromJson(images, JsonArray.class);
        Boolean active;
        String type;
        for (int i = 0; i < arr.size(); i++) {
            JsonObject jObj = (JsonObject) arr.get(i);
            active = jObj.get("active").getAsBoolean();
            type = jObj.get("type").getAsString();
            String path = jObj.get("path").getAsString();
            if(active && type.equals("LOGO") && !path.equals("no_image")) {
                return true;
            }
        }
        return false;
    }

    public void setImages(JsonArray images) {
        Gson g = new Gson();

        this.images = g.toJson(images);
    }

    public float getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(float voteAverage) {
        this.voteAverage = voteAverage;
        notifyPropertyChanged(BR._all);
    }
}