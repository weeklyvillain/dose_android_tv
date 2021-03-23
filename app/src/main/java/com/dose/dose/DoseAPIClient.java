package com.dose.dose;

import android.util.Log;

import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
    private String userName;
    private String password;
    private String mainJWT;
    protected String movieJWT;
    private String mainServerURL;

    public abstract String getPlaybackURL(String id, int startPos, String res);
    public abstract JSONArray getNewContent();
    public abstract JSONArray getOngoing();
    public abstract int getDuration(String id) throws Exception;
    public abstract JSONArray getWatchlist();

    protected DoseAPIClient(String url) {
        this.mainServerURL = url;
    }

    protected DoseAPIClient(String url, String JWT) {
        this.mainJWT = JWT;
        this.mainServerURL = url;
    }

    protected DoseAPIClient(String url, String mainJWT, String movieJWT) {
        this.mainServerURL = url;
        this.mainJWT = mainJWT;
        this.movieJWT = movieJWT;
    }

    public static JSONObject login(String username, String password, String mainServerUrl) {
        JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("username", username);
            jsonParam.put("password", password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject response = customPost(mainServerUrl + "/api/auth/login", new JSONObject(), jsonParam);
        Log.i("RESPONSE: ", response.toString());
        return response;
    }

    public static JSONObject getContentServers(String mainServerURL, String mainServerJWT) {
        /*
        JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("username", username);
            jsonParam.put("password", password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject response = customPost(mainServerUrl + "/api/auth/login", new JSONObject(), jsonParam);
        return response;
        */
        JSONObject result = new JSONObject();
        try {
            result.put("server", "https://vnc.fgbox.appboxes.co/doseserver");
            //result.put("server", "http://10.0.2.2:3001");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String getContentServerJWT(String contentServer, String mainJWT) {
        JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("token", mainJWT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject response = customPost(contentServer + "/api/auth/validate", new JSONObject(), jsonParam);
        Log.i("CONTENTJWT", response.toString());

        String result;
        try {
            if (response.getString("status").equals("success")) {
                result = response.getString("token");
            } else {
                result = "";
            }
        } catch(Exception e) {
            e.printStackTrace();
            result = "";
        }

        return result;
    }

    public static JSONObject customPost(String url, JSONObject headers, JSONObject body) {
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


    public JSONObject customPost(String url, JSONObject body) {

        return new JSONObject();
    }

    public JSONObject customGet(String url, JSONObject headers) {
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


    public String getMovieJWT() {
        return movieJWT;
    }
}
