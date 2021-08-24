package com.dose.dose;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.leanback.app.BackgroundManager;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.dose.dose.viewModels.SelectedViewModel;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class BrowseActivity extends FragmentActivity {
    private boolean fullscreen = true;
    private boolean hasPressedDown = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!loggedIn())
        {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(intent, 1);
        } else {
            setContentView(R.layout.activity_browse);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            hasPressedDown = true;
            BrowseContentFragment fragment = (BrowseContentFragment) getSupportFragmentManager().findFragmentById(R.id.contentFragment);
            fragment.onFirstPressDown();
        }
        if (fullscreen) {
            fullscreen = false;
            /*
            ConstraintLayout constraintLayout = findViewById(R.id.parent_layout);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            //constraintSet.clear(R.id.contentFragment);
            //constraintSet.connect(R.id.contentFragment, ConstraintSet.TOP, R.id.headersView, ConstraintSet.BOTTOM);
            constraintSet.setVerticalBias(R.id.contentFragment, 1.0f);
            constraintSet.setHorizontalBias(R.id.contentFragment, 1.0f);

            AutoTransition transition = new AutoTransition();
            transition.setDuration(1500);

            TransitionManager.beginDelayedTransition(constraintLayout, transition);
            constraintSet.applyTo(constraintLayout);
            */

            /*
            final float scale = getResources().getDisplayMetrics().density;
            int pixels = (int) (300 * scale + 0.5f);
            ValueAnimator anim = ValueAnimator.ofInt(findViewById(R.id.headersView).getMeasuredHeight(), pixels);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = findViewById(R.id.headersView).getLayoutParams();
                    layoutParams.height = val;
                    findViewById(R.id.headersView).setLayoutParams(layoutParams);
                }
            });
            anim.setDuration(1000);
            anim.start();*/

/*
            Animation a = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {

                    //ConstraintLayout constraintLayout = findViewById(R.id.parent_layout);
                    //ConstraintSet constraintSet = new ConstraintSet();
                    //constraintSet.clone(constraintLayout);
                    //constraintSet.connect(R.id.contentFragment, ConstraintSet.BOTTOM, R.id.parent_layout, ConstraintSet.BOTTOM);
                    //constraintSet.applyTo(findViewById(R.id.parent_layout));

                    View v = findViewById(R.id.headersView);
                    final float scale = getResources().getDisplayMetrics().density;
                    int pixels = (int) (300 * scale + 0.5f);
                    v.getLayoutParams().height = pixels;
                    v.requestLayout();
                }
            };
            a.setDuration(1000); // in ms
            a.setFillAfter(true);
            findViewById(R.id.headersView).startAnimation(a);
            */
        }


        return super.onKeyDown(keyCode, event);
    }

    private Boolean loggedIn() {
        SharedPreferences settings = getSharedPreferences("UserInfo", 0);
        String JWT = settings.getString("MainServerJWT", "").toString();
        String refreshToken = settings.getString("MainServerRefreshToken", "").toString();
        String mainServerURL = settings.getString("MainServerURL", "").toString();
        String contentServer = settings.getString("ContentServerURL", "").toString();
        String contentServerJWT = settings.getString("ContentServerJWT", "").toString();
        if(JWT.isEmpty()  || mainServerURL.isEmpty() || contentServer.isEmpty() || contentServerJWT.isEmpty() || refreshToken.isEmpty()) {
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setContentView(R.layout.activity_browse);
    }
}