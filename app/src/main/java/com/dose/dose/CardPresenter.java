package com.dose.dose;

import android.graphics.drawable.Drawable;

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
import com.dose.dose.content.Show;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand.
 * It contains an Image CardView
 */
public class CardPresenter extends Presenter {
    private static final String TAG = "CardPresenter";
    private Drawable mDefaultCardImage;


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Log.d(TAG, "onCreateViewHolder");
        MovieCardView cardView = new MovieCardView(parent.getContext());
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        BaseContent content;
        if (item instanceof Movie) {
            content = (Movie) item;
        } else {
            content = (Show) item;
        }
        MovieCardView cardView = (MovieCardView) viewHolder.view;

        cardView.setOnFocusChangeListener((view, hasFocus) -> {
            if(hasFocus) {
                cardView.setTitleText(content.getTitle());
                cardView.setContentText(content.getDescription());
            } else {
                cardView.setTitleText("");
                cardView.setContentText("");
            }
        });


        Log.d(TAG, "onBindViewHolder");
        if (content.getCardImageUrl(false) != null) {
            cardView.setTitleText("");
            cardView.setContentText("");
            Log.i("NUÄRVIHÄR", content.getCardImageUrl(false));


            Glide.with(viewHolder.view.getContext())
                    .load(content.getCardImageUrl(false))
                    .error(mDefaultCardImage)
                    .into(cardView.getMainImageView());
        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
        Log.d(TAG, "onUnbindViewHolder");
        MovieCardView cardView = (MovieCardView) viewHolder.view;
        // Remove references to images so that the garbage collector can free up memory
        cardView.setMainImage(null);
    }
}