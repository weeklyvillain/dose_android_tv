package com.dose.dose.ApiClient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.dose.dose.BrowseActivity;
import com.dose.dose.token.Token;
import com.dose.dose.token.TokenHandler;
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

    protected String mainServerURL;
    protected String movieServerURL;
    protected final Context context;

    public abstract String getPlaybackURL(String id, int startPos, String res);
    public abstract JSONArray getNewContent();
    public abstract JSONArray getOngoing();
    public abstract int getDuration(String id) throws Exception;
    public abstract JSONArray getWatchlist();
    public abstract void updateCurrentTime(String id, int time, int videoDuration);
    public abstract JSONArray getByGenre(String genre) throws JSONException;

    protected DoseAPIClient(String mainServerURL, String movieServerURL, Context context) {
        this.mainServerURL = mainServerURL;
        this.movieServerURL = movieServerURL;
        this.context = context;
        DoseAPIClient.instances.add(this);
    }

    private JSONObject getNewMainToken() {
        Token token = TokenHandler.Tokenhandler(context).getMainToken();
        String url = String.format("%s/api/auth/refreshToken", this.mainServerURL);
        Log.i("URL: ", url);
        JSONObject body = new JSONObject();
        try {
            body.put("token", token.getToken());
            body.put("refreshToken", token.getRefreshToken());
            return getNewToken(url, body);
        } catch (Exception e) {
            Log.i("TokenInformationError: ", "Couldn't put old token information to the parameters");
            e.printStackTrace();
        }
        return null;
    }

    private JSONObject getNewContentToken() {
        Token token = TokenHandler.Tokenhandler(context).getMainToken();
        String url = String.format("%s/api/auth/validate", this.movieServerURL);
        JSONObject body = new JSONObject();
        try {
            body.put("token", token.getToken());
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

    public void setNewMainToken(Token token) {
        TokenHandler.Tokenhandler(context).setMainToken(token, context);
    }

    public void setNewContentToken(Token token) {
        TokenHandler.Tokenhandler(context).setContentToken(token, context);
    }

    public void getNewTokensIfNeeded() {
        DoseAPIClient.lock.lock();
        TokenHandler tokenHandler = TokenHandler.Tokenhandler(context);
        Token mainToken = tokenHandler.getMainToken();
        Token contentToken = tokenHandler.getContentToken();

        long currentTime = System.currentTimeMillis() / 1000;
        Log.i("currentTime: ", String.valueOf(currentTime));
        Log.i("tokenValidTo: ", String.valueOf(mainToken.getValidTo()));
        Log.i("contentTokenValidTo: ", String.valueOf(contentToken.getValidTo()));
        Log.i("TimeLeft: ", String.valueOf(currentTime - contentToken.getValidTo()));

        boolean newMainTokenNeeded = mainToken.isTokenValid();
        Log.i("newMainTokenNeeded: ", String.valueOf(newMainTokenNeeded));
        boolean newContentTokenNeeded = contentToken.isTokenValid();
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
                        Token newToken = new Token(mainTokenInfo.getString("token"),
                                                   mainTokenInfo.getString("refreshToken"),
                                                   mainTokenInfo.getDouble("validTo"));

                        // Set the new token
                        for (DoseAPIClient instance : DoseAPIClient.instances) {
                            instance.setNewMainToken(newToken);
                        }

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
                        Token newToken = new Token(contentTokenInfo.getString("token"),
                                                   contentTokenInfo.getDouble("validTo"));

                        // Set the new token
                        for (DoseAPIClient instance : DoseAPIClient.instances) {
                            instance.setNewContentToken(newToken);
                        }

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
            tokenHandler.setContentToken(null, context);
            tokenHandler.setMainToken(null, context);

            Intent intent = new Intent(context, BrowseActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        }
        DoseAPIClient.lock.unlock();
    }


    protected JSONObject customPost(String url, JSONObject headers, JSONObject body) {
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

    // TODO: Send authentication via bearer instead
    protected JSONObject contentServerRequest(String url) {
        getNewTokensIfNeeded();
        String token = TokenHandler.Tokenhandler(context).getContentToken().getToken();

        url = String.format("%s%s%s", this.movieServerURL, url, token);
        return customGet(url);
    }

    // TODO: Send authentication via bearer instead
    protected JSONObject mainServerRequest(String url) {
        getNewTokensIfNeeded();
        String token = TokenHandler.Tokenhandler(context).getMainToken().getToken();

        url = String.format("%s%s%s", this.mainServerURL, url, token);
        return customGet(url);
    }

    protected JSONObject customGet(String url) {
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
            conn.disconnect();

            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    public List<String> getGenres() {
        List<String> genres = new ArrayList<>();
        try {
            JSONArray result = contentServerRequest("/api/genre/list?token=").getJSONArray("genres");
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

    public JSONObject search(String query) {
        String url = String.format("/api/list/search?query=%s&token=", query);
        return this.contentServerRequest(url);
    }
}
