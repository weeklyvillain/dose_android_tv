package com.dose.dose;

import android.util.Log;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

public final class SendPostRequest {
    public final JSONObject sendPost() {
        final CountDownLatch latch = new CountDownLatch(1);
        final JSONObject[] retJson = {new JSONObject()};
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Retrive Token from main server
                    URL url = new URL("https://vnc.fgbox.appboxes.co/dose/api/auth/login");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept","application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("username", "filip");
                    jsonParam.put("password", "Filip");

                    Log.i("JSON", jsonParam.toString());
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                    os.writeBytes(jsonParam.toString());

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
                    Log.i("Token = " , jsonObject.getString("token"));
                    conn.disconnect();

                    String JWT = jsonObject.getString("token");

                    // get servers from main server
                    url = new URL("https://vnc.fgbox.appboxes.co/dose/api/servers/getServers");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept","application/json");
                    conn.addRequestProperty("Cookie", "token=" + JWT);

                    conn.setDoOutput(true);
                    conn.setDoInput(true);



                    //Log.i("JSON", jsonParam.toString());
                    //os = new DataOutputStream(conn.getOutputStream());
                    //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                    //os.writeBytes(jsonParam.toString());

                    //os.flush();
                    //os.close();

                    responseMsg = "";
                    in = conn.getInputStream();

                    respDataBuf = new StringBuffer();
                    respDataBuf.setLength(0);
                    b = -1;

                    while((b = in.read()) != -1) {
                        respDataBuf.append((char)b);
                    }
                    responseMsg = respDataBuf.toString();
                    jsonObject = new JSONObject(new JSONTokener(responseMsg));
                    Log.i("STATUS2", String.valueOf(conn.getResponseCode()));
                    Log.i("MSG2" , conn.getResponseMessage());
                    Log.i("Response " , responseMsg);
                    conn.disconnect();


                    // validate movie server
                    url = new URL("https://vnc.fgbox.appboxes.co/doseserver/api/auth/validate");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept","application/json");

                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    jsonParam = new JSONObject();
                    jsonParam.put("token", JWT);

                    Log.i("JSON", jsonParam.toString());
                    os = new DataOutputStream(conn.getOutputStream());
                    //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                    os.writeBytes(jsonParam.toString());

                    os.flush();
                    os.close();

                    responseMsg = "";
                    in = conn.getInputStream();

                    respDataBuf = new StringBuffer();
                    respDataBuf.setLength(0);
                    b = -1;

                    while((b = in.read()) != -1) {
                        respDataBuf.append((char)b);
                    }
                    responseMsg = respDataBuf.toString();
                    jsonObject = new JSONObject(new JSONTokener(responseMsg));
                    String MovieJWT = jsonObject.getString("token");
                    Log.i("STATUSMovie", String.valueOf(conn.getResponseCode()));
                    Log.i("MSGMOvie" , conn.getResponseMessage());
                    Log.i("ResponseMovie " , responseMsg);
                    conn.disconnect();

                    // get movies from movie server
                    url = new URL("https://vnc.fgbox.appboxes.co/doseserver/api/movies/list?orderby=release_date&limit=5&token=" + MovieJWT);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept","application/json");

                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    //jsonParam = new JSONObject();
                    //jsonParam.put("token", JWT);

                    //Log.i("JSON", jsonParam.toString());
                    //os = new DataOutputStream(conn.getOutputStream());
                    //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                    //os.writeBytes(jsonParam.toString());

                    //os.flush();
                    //os.close();

                    responseMsg = "";
                    in = conn.getInputStream();

                    respDataBuf = new StringBuffer();
                    respDataBuf.setLength(0);
                    b = -1;

                    while((b = in.read()) != -1) {
                        respDataBuf.append((char)b);
                    }
                    responseMsg = respDataBuf.toString();
                    jsonObject = new JSONObject(new JSONTokener(responseMsg));
                    Log.i("STATUSMovie", String.valueOf(conn.getResponseCode()));
                    Log.i("MSGMOvie" , conn.getResponseMessage());
                    Log.i("ResponseMovie " , responseMsg);
                    conn.disconnect();
                    jsonObject.accumulate("token", MovieJWT);


                    retJson[0] = jsonObject;
                    latch.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return retJson[0];
    }
}
