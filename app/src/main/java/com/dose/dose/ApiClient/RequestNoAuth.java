package com.dose.dose.ApiClient;

import android.util.Log;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class RequestNoAuth {

    public static JSONObject get(String url) {
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

    public static JSONObject post(String url, JSONObject body) {
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
}
