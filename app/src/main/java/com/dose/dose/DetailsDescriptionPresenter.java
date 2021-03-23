package com.dose.dose;

import androidx.leanback.widget.AbstractDetailsDescriptionPresenter;

import com.dose.dose.content.Movie;

public class DetailsDescriptionPresenter extends AbstractDetailsDescriptionPresenter {

    @Override
    protected void onBindDescription(ViewHolder viewHolder, Object item) {
        Movie movie = (Movie) item;

        if (movie != null) {
            viewHolder.getTitle().setText(movie.getTitle());
            viewHolder.getSubtitle().setText(movie.getReleaseDate());
            viewHolder.getBody().setText(movie.getDescription());
        }
    }
}