package com.dose.dose;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
}