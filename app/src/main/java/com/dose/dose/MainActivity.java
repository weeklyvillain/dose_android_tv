package com.dose.dose;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/*
 * Main Activity class that loads {@link MainFragment}.
 */
public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*SharedPreferences settings = getSharedPreferences("UserInfo", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("Username",txtUname.getText().toString());
        editor.putString("Password",txtPWD.getText().toString());
        editor.commit();*/

        if(!loggedIn())
        {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
            startActivity(intent);
        }
    }

    private Boolean loggedIn() {
        SharedPreferences settings = getSharedPreferences("UserInfo", 0);
        String JWT = settings.getString("MainServerJWT", "").toString();
        String mainServerURL = settings.getString("MainServerURL", "").toString();
        String contentServer = settings.getString("ContentServerURL", "").toString();
        String contentServerJWT = settings.getString("ContentServerJWT", "").toString();

        if(JWT == ""  || mainServerURL == "" || contentServer == "" || contentServerJWT == ""){
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}