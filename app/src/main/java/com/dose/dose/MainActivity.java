package com.dose.dose;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/*
 * Main Activity class that loads {@link MainFragment}.
 */
public class MainActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!loggedIn())
        {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(intent, 1);
        } else {
            setContentView(R.layout.activity_main);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setContentView(R.layout.activity_main);
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