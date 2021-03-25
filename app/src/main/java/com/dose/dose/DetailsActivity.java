package com.dose.dose;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;

import androidx.core.content.ContextCompat;
import androidx.leanback.app.BackgroundManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.dose.dose.content.Movie;
import com.dose.dose.content.Show;

/*
 * Details activity class that loads LeanbackDetailsFragment class
 */
public class DetailsActivity extends Activity {
    public static final String SHARED_ELEMENT_NAME = "hero";
    public static final String MOVIE = "Movie";

    private Drawable mDefaultBackground;
    private BackgroundManager mBackgroundManager;
    private DisplayMetrics mMetrics;

    private Movie mSelectedMovie;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        prepareBackgroundManager();
    }


    private void prepareBackgroundManager() {

        mBackgroundManager = BackgroundManager.getInstance(this);
        mBackgroundManager.attach(getWindow());

        mDefaultBackground = ContextCompat.getDrawable(this, R.drawable.default_background);
        mMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mMetrics);

        mSelectedMovie =
                (Movie) getIntent().getSerializableExtra(DetailsActivity.MOVIE);

        updateBackground(mSelectedMovie.getCardImageUrl(true));
    }

    private void updateBackground(String uri) {
        int width = mMetrics.widthPixels;
        int height = mMetrics.heightPixels;
        Glide.with(this)
                .load(uri)
                .centerCrop()
                .error(mDefaultBackground)
                .into(new SimpleTarget<GlideDrawable>(width, height) {
                    @Override
                    public void onResourceReady(GlideDrawable resource,
                                                GlideAnimation<? super GlideDrawable>
                                                        glideAnimation) {
                        mBackgroundManager.setDrawable(resource);
                    }
                });
    }
}