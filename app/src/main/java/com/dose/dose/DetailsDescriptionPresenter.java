package com.dose.dose;

import androidx.leanback.widget.AbstractDetailsDescriptionPresenter;

import com.dose.dose.content.BaseContent;
import com.dose.dose.content.Movie;

public class DetailsDescriptionPresenter extends AbstractDetailsDescriptionPresenter {

    @Override
    protected void onBindDescription(ViewHolder viewHolder, Object item) {
        BaseContent content = (BaseContent) item;

        if (content != null) {
            viewHolder.getTitle().setText(content.getTitle());
            viewHolder.getSubtitle().setText(content.getReleaseDate());
            viewHolder.getBody().setText(content.getDescription());
        }
    }
}