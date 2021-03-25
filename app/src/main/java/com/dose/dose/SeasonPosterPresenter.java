package com.dose.dose;

import android.graphics.drawable.Drawable;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.Presenter;
import androidx.core.content.ContextCompat;
import androidx.leanback.widget.TitleView;

import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dose.dose.ApiClient.DoseAPIClient;
import com.dose.dose.content.BaseContent;
import com.dose.dose.content.Movie;
import com.dose.dose.content.Season;
import com.dose.dose.content.Show;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand.
 * It contains an Image CardView
 */
public class SeasonPosterPresenter extends Presenter {
    private static final String TAG = "SeasonPosterPresenter";
    private Drawable mDefaultCardImage;


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Log.d(TAG, "onCreateViewHolder");
        SeasonCardView cardView = new SeasonCardView(parent.getContext());
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        Season season = (Season) item;

        SeasonCardView cardView = (SeasonCardView) viewHolder.view;

        cardView.setOnFocusChangeListener((view, hasFocus) -> {
            if(hasFocus) {
                cardView.setSeasonText(season.getTitle());
            } else {
                cardView.setSeasonText("");
            }
        });


        Log.d(TAG, "onBindViewHolder");
        if (season.getPosterImage(true) != null) {
            cardView.setSeasonText("");
            Log.i("NUÄRVIHÄR", season.getPosterImage(true));


            Glide.with(viewHolder.view.getContext())
                    .load(season.getPosterImage(true))
                    .error(mDefaultCardImage)
                    .into(cardView.getPosterImage());
        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
        Log.d(TAG, "onUnbindViewHolder");
        SeasonCardView cardView = (SeasonCardView) viewHolder.view;
        // Remove references to images so that the garbage collector can free up memory
        cardView.setPosterImage(null);
    }
}