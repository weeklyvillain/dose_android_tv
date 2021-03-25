package com.dose.dose;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.leanback.widget.BaseCardView;

public class SeasonCardView extends BaseCardView {

    private TextView seasonText;
    private ImageView seasonImageView;


    public SeasonCardView(Context context) {
        super(context);
        buildCardView();
    }

    protected void buildCardView() {
        // Make sure this view is clickable and focusable
        setClickable(true);
        setFocusable(true);
        setFocusableInTouchMode(true);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate((R.layout.season_poster_card), this);

        seasonImageView = findViewById(R.id.poster);
        seasonText = findViewById(R.id.seasonText);
    }

    /**
     * Sets the image drawable.
     */
    public void setPosterImage(Drawable drawable)
    {
        seasonImageView.setImageDrawable(drawable);
    }


    /**
     * Sets the title text.
     */
    public void setSeasonText(CharSequence text) {
        if (seasonText == null) {
            return;
        }
        seasonText.setText(text);
    }

    public ImageView getPosterImage() {
        return seasonImageView;
    }

    /*public TextView get_titleView() {
        return _titleView;
    }

    public void setMainImageView(ImageView mainImageView) {
        this.mainImageView = mainImageView;
    }*/
}