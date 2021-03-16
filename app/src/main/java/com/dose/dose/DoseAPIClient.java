package com.dose.dose;

import android.util.Log;

import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class DoseAPIClient {
/*
    Login (har implementation)
 - ABSTRACT getPlaybackUrl()
 - ABSTRACT getNewContent()
 - ABSTRACT getOngoing()
 - Search(query) (har implementation)
            - GetServers() (har implementation)
            - GetMainJWT() (har implementation)
            - GetMovieJWT() (har implementation)
            - RefreshMainJWT() (har implementation)
            - RefreshMovieJWT() (har implementation)
            - CustomPost() (har implementation)
            - CustomGet() (har implementation)
*/
    private String userName;
    private String password;
    private String mainJWT;
    private String movieJWT;
    private String mainServerURL;

    public abstract String getPlaybackURL(String id, String startPos, String res);
    public abstract JsonObject getNewContent();
    public abstract JsonObject getOngoing();

    protected DoseAPIClient(String url) {
        this.mainServerURL = url;
    }

    protected DoseAPIClient(String url, String mainJWT, String movieJWT) {
        this.mainServerURL = url;
        this.mainJWT = mainJWT;
        this.movieJWT = movieJWT;
    }

    public void login(String username, String password) {
        JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("username", username);
            jsonParam.put("password", password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject response = customPost(mainServerURL + "/api/auth/login", new JSONObject(), jsonParam);
        try {
            mainJWT = response.getString("token");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject customPost(String url, JSONObject headers, JSONObject body) {
        try {
        URL reqUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) reqUrl.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        conn.setRequestProperty("Accept","application/json");
        conn.setDoOutput(true);
        conn.setDoInput(true);


        Log.i("JSON", body.toString());
        DataOutputStream os = new DataOutputStream(conn.getOutputStream());
        //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
        os.writeBytes(body.toString());

        os.flush();
        os.close();

        String responseMsg = "";
        InputStream in = conn.getInputStream();

        StringBuffer respDataBuf = new StringBuffer();
        respDataBuf.setLength(0);
        int b = -1;

        while((b = in.read()) != -1) {
            respDataBuf.append((char)b);
        }
        responseMsg = respDataBuf.toString();
        JSONObject jsonObject = new JSONObject(new JSONTokener(responseMsg));
        Log.i("STATUS", String.valueOf(conn.getResponseCode()));
        Log.i("MSG" , conn.getResponseMessage());
        conn.disconnect();

        return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    public JSONObject customPost(String url, JSONObject body) {

        return new JSONObject();
    }

    public JSONObject customGet(String url, JSONObject headers) {

        return new JSONObject();
    }


    public String getMovieJWT() {
        return movieJWT;
    }
}
