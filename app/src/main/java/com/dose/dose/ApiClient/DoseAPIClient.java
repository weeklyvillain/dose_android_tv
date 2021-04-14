package com.dose.dose.ApiClient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.dose.dose.BrowseActivity;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.HttpsURLConnection;

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
    private static Lock lock = new ReentrantLock();
    // Keeps track of all instances of this class subclasses (to handle token changes)
    private static List<DoseAPIClient> instances = new ArrayList();
    private String mainJWT;
    protected String movieJWT;
    protected String mainServerURL;
    protected String mainServerValidTo;
    protected String mainServerRefreshToken;
    protected String contentServerValidTo;
    protected String movieServerURL;
    private Context context;

    public abstract String getPlaybackURL(String id, int startPos, String res);
    public abstract JSONArray getNewContent();
    public abstract JSONArray getOngoing();
    public abstract int getDuration(String id) throws Exception;
    public abstract JSONArray getWatchlist();
    public abstract void updateCurrentTime(String id, int time, int videoDuration);
    public abstract JSONArray getByGenre(String genre) throws JSONException;

    protected DoseAPIClient(String mainServerURL, String movieServerURL, String mainJWT, String mainServerRefreshToken, String movieJWT, String mainServerValidTo, String contentServerValidTo, Context context) {
        this.mainServerURL = mainServerURL;
        this.movieServerURL = movieServerURL;
        this.mainJWT = mainJWT;
        this.mainServerRefreshToken = mainServerRefreshToken;
        this.movieJWT = movieJWT;
        this.mainServerValidTo = mainServerValidTo;
        this.contentServerValidTo = contentServerValidTo;
        this.context = context;
        DoseAPIClient.instances.add(this);
    }

    private JSONObject getNewMainToken() {
        String url = String.format("%s/api/auth/refreshToken", this.mainServerURL);
        Log.i("URL: ", url);
        JSONObject body = new JSONObject();
        try {
            body.put("token", this.mainJWT);
            body.put("refreshToken", this.mainServerRefreshToken);
            return getNewToken(url, body);
        } catch (Exception e) {
            Log.i("TokenInformationError: ", "Couldn't put old token information to the parameters");
            e.printStackTrace();
        }
        return null;
    }

    private JSONObject getNewContentToken() {
        String url = String.format("%s/api/auth/validate", this.movieServerURL);
        JSONObject body = new JSONObject();
        try {
            body.put("token", this.mainJWT);
            return getNewToken(url, body);
        } catch (Exception e) {
            Log.i("TokenInformationError: ", "Couldn't put old token information to the parameters");
            e.printStackTrace();
        }
        return null;
    }

    private JSONObject getNewToken(String url, JSONObject body) {
        try {
            URL reqUrl = new URL(url);
            HttpsURLConnection conn = (HttpsURLConnection) reqUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept","application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);


            Log.i("JSON", body.toString());
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
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
            Log.i("RESPONSE: ", responseMsg);
            conn.disconnect();

            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setNewMainToken(String token, String refreshToken, String validTo) {
        this.mainJWT = token;
        this.mainServerRefreshToken = refreshToken;
        this.mainServerValidTo = validTo;
    }

    public void setNewContentToken(String token, String validTo) {
        this.movieJWT = token;
        this.contentServerValidTo = validTo;
    }

    public void getNewTokensIfNeeded() {
        DoseAPIClient.lock.lock();
        long currentTime = System.currentTimeMillis() / 1000;
        Log.i("currentTime: ", String.valueOf(currentTime));
        Log.i("tokenValidTo: ", String.valueOf(this.mainServerValidTo));
        Log.i("contentTokenValidTo: ", String.valueOf(this.contentServerValidTo));
        Log.i("TimeLeft: ", String.valueOf(currentTime - Long.parseLong(this.mainServerValidTo)));

        boolean newMainTokenNeeded = (currentTime - Integer.parseInt(this.mainServerValidTo)) <= 60;
        Log.i("newMainTokenNeeded: ", String.valueOf(newMainTokenNeeded));
        boolean newContentTokenNeeded = (currentTime - Integer.parseInt(this.contentServerValidTo)) <= 60;
        Log.i("newContentTokenNeeded: ", String.valueOf(newContentTokenNeeded));


        boolean failedRefreshingMainToken = true;
        boolean failedRefreshingContentToken = true;
        if (newMainTokenNeeded) {
            Log.i("TokenInformation: ", "Main token is expired");

            // Get new token
            JSONObject mainTokenInfo = this.getNewMainToken();
            if (mainTokenInfo != null) {
                try {
                    String status = mainTokenInfo.getString("status");
                    if (status.equals("success")) {
                        String token = mainTokenInfo.getString("token");
                        String refreshToken = mainTokenInfo.getString("refreshToken");
                        String validTo = mainTokenInfo.getString("validTo");

                        // Set the new token
                        for (DoseAPIClient instance : DoseAPIClient.instances) {
                            instance.setNewMainToken(token, refreshToken, validTo);
                        }

                        // Save the new token
                        SharedPreferences settings = context.getSharedPreferences("UserInfo", 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("MainServerJWT", token);
                        editor.putString("MainServerRefreshToken", refreshToken);
                        editor.putString("MainServerValidTo", validTo);
                        editor.commit();
                        failedRefreshingMainToken = false;

                        Log.i("TokenInformation: ", "Got new main server token");
                    }
                } catch (Exception e) {
                    Log.i("TokenInformationError: ", "Couldn't get data from new token");
                    e.printStackTrace();
                }
            } else {
                Log.i("TokenInformationError: ", "Couldn't do request to main server..");
            }
        } else {
            failedRefreshingMainToken = false;
        }

        if (newContentTokenNeeded) {
            Log.i("TokenInformation: ", "Content token is expired");

            // Get new token
            JSONObject contentTokenInfo = this.getNewContentToken();
            if (contentTokenInfo != null) {
                try {
                    String status = contentTokenInfo.getString("status");
                    if (status.equals("success")) {
                        String token = contentTokenInfo.getString("token");
                        String validTo = contentTokenInfo.getString("validTo");

                        // Set the new token
                        for (DoseAPIClient instance : DoseAPIClient.instances) {
                            instance.setNewContentToken(token, validTo);
                        }

                        // Save the new token
                        SharedPreferences settings = context.getSharedPreferences("UserInfo", 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("ContentServerJWT", token);
                        editor.putString("ContentServerValidTo", validTo);
                        editor.commit();
                        failedRefreshingContentToken = false;
                        Log.i("TokenInformation: ", "Got new content server token");
                    }
                } catch (Exception e) {
                    Log.i("TokenInformationError: ", "Couldn't get data from new token");
                    e.printStackTrace();
                }
            } else {
                Log.i("TokenInformationError: ", "Couldn't do request to content server..");
            }
        } else {
            failedRefreshingContentToken = false;
        }

        if (failedRefreshingContentToken || failedRefreshingMainToken) {
            SharedPreferences settings = context.getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.remove("MainServerJWT");
            editor.remove("ContentServerJWT");
            editor.remove("ContentServerValidTo");
            editor.remove("ContentServerURL");
            editor.remove("MainServerRefreshToken");
            editor.remove("MainServerValidTo");
            editor.apply();

            Intent intent = new Intent(context, BrowseActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        }
        DoseAPIClient.lock.unlock();
    }


    protected JSONObject customPost(String url, JSONObject headers, JSONObject body) {
        getNewTokensIfNeeded();
        try {
            Log.i("URL: ", url);
        URL reqUrl = new URL(url);
        HttpsURLConnection conn = (HttpsURLConnection) reqUrl.openConnection();
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

    protected JSONObject customGet(String url, JSONObject headers) {
        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        getNewTokensIfNeeded();
        try {
            URL reqUrl = new URL(url);
            HttpsURLConnection conn = (HttpsURLConnection) reqUrl.openConnection();
            conn.setRequestMethod("GET");
            String responseMsg = "";
            InputStream in = conn.getInputStream();

            StringBuffer respDataBuf = new StringBuffer();
            respDataBuf.setLength(0);
            int b = -1;

            while((b = in.read()) != -1) {
                respDataBuf.append((char)b);
            }
            responseMsg = respDataBuf.toString();
            JSONObject jsonObject = null;
            if (!responseMsg.isEmpty()) {
                jsonObject = new JSONObject(new JSONTokener(responseMsg));
            }
            Log.i("STATUS", String.valueOf(conn.getResponseCode()));
            Log.i("MSG" , conn.getResponseMessage());
            //Log.i("RESPONSE: ", jsonObject.toString());
            conn.disconnect();

            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    public List<String> getGenres() {
        String url = String.format("%s/api/genre/list?token=%s", this.movieServerURL, this.getMovieJWT());
        List<String> genres = new ArrayList<>();
        try {
            JSONArray result = customGet(url, new JSONObject()).getJSONArray("genres");
            for (int i = 0; i < result.length(); i++) {
                genres.add(result.getJSONObject(i).getString("name"));
            }

            Log.i("genres: ", genres.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return genres;
    }

    public static boolean ping(String url) {
        url += "/api/ping";
        try {
            URL reqUrl = new URL(url);
            HttpsURLConnection conn = (HttpsURLConnection) reqUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            return conn.getResponseCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getMovieJWT() {
        return movieJWT;
    }
}
