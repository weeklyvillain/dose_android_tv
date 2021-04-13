package com.dose.dose.details;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.leanback.app.BackgroundManager;

import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.dose.dose.R;
import com.dose.dose.content.Movie;
import com.dose.dose.details.ui.main.MovieDetailsFragment;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.gpu.BrightnessFilterTransformation;

public class MovieDetailsActivity extends FragmentActivity {
    public static final String SHARED_ELEMENT_NAME = "hero";
    public static final String MOVIE = "Movie";

    private Drawable mDefaultBackground;
    private BackgroundManager mBackgroundManager;
    private DisplayMetrics mMetrics;

    private Movie movie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_details_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MovieDetailsFragment.newInstance())
                    .commitNow();
        }

        movie =  (Movie) getIntent().getSerializableExtra(MovieDetailsActivity.MOVIE);
        mBackgroundManager = BackgroundManager.getInstance(this);
        mBackgroundManager.attach(getWindow());

        mDefaultBackground = ContextCompat.getDrawable(this, R.drawable.default_background);
        mMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
        updateBackground(movie.getCardImageUrl(true));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        updateBackground(movie.getCardImageUrl(true));

    }

    private void updateBackground(String uri) {
        int width = mMetrics.widthPixels;
        int height = mMetrics.heightPixels;
        Glide.with(this)
                .asBitmap()
                .load(uri)
                .transform(new BlurTransformation(), new CenterCrop())
                .error(mDefaultBackground)
                .into(new CustomTarget<Bitmap>(width, height) {

                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        mBackgroundManager.setBitmap(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }
}