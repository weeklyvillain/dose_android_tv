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

import org.w3c.dom.Text;

public class MovieCardView extends BaseCardView {

    private TextView _titleView;
    private TextView contentText;
    private TextView seasonAndEpisode;
    private ImageView mainImageView;

    public MovieCardView(Context context) {
        super(context);
        buildCardView();
    }

    protected void buildCardView() {
        // Make sure this view is clickable and focusable
        setClickable(true);
        setFocusable(true);
        setFocusableInTouchMode(true);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate((R.layout.movie_card), this);

        mainImageView = (ImageView) findViewById(R.id.backdrop);
        _titleView = (TextView) findViewById(R.id.title);
        contentText = (TextView) findViewById(R.id.contentText);

        seasonAndEpisode = (TextView) findViewById(R.id.seasonAndEpisode);
    }

    /**
     * Sets the image drawable.
     */
    public void setMainImage(Drawable drawable)
    {
        mainImageView.setImageDrawable(drawable);
    }


    /**
     * Sets the title text.
     */
    public void setTitleText(CharSequence text) {
        if (_titleView == null) {
            return;
        }
        _titleView.setText(text);
    }

    public void setContentText(CharSequence text) {
        if (contentText == null) {
            return;
        }
        contentText.setText(text);
    }

    public void setSeasonAndEpisode(CharSequence text) {
        if (seasonAndEpisode == null) {
            return;
        }
        seasonAndEpisode.setText(text);
    }

    public ImageView getMainImageView() {
        return mainImageView;
    }

    /*public TextView get_titleView() {
        return _titleView;
    }

    public void setMainImageView(ImageView mainImageView) {
        this.mainImageView = mainImageView;
    }*/
}