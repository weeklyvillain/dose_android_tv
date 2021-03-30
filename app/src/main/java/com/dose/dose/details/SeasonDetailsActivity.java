package com.dose.dose.details;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.leanback.app.BackgroundManager;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.dose.dose.R;
import com.dose.dose.content.Season;
import com.dose.dose.content.Show;
import com.dose.dose.details.ui.main.SeasonDetailsFragment;
import com.dose.dose.details.ui.main.ShowDetailsFragment;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class SeasonDetailsActivity extends FragmentActivity {
    public static final String SHARED_ELEMENT_NAME = "hero";
    public static final String SEASON = "Season";

    private Drawable mDefaultBackground;
    private BackgroundManager mBackgroundManager;
    private DisplayMetrics mMetrics;

    private Season season;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.season_details_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, SeasonDetailsFragment.newInstance())
                    .commitNow();
        }

        season = (Season) getIntent().getSerializableExtra(SeasonDetailsActivity.SEASON);
        mBackgroundManager = BackgroundManager.getInstance(this);
        mBackgroundManager.attach(getWindow());

        mDefaultBackground = ContextCompat.getDrawable(this, R.drawable.default_background);
        mMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
        updateBackground(season.getCardImageUrl(true));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        updateBackground(season.getCardImageUrl(true));

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