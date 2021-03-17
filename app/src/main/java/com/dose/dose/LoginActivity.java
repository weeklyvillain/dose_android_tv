package com.dose.dose;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button loginBtn = findViewById(R.id.login);
        TextView serverURL = findViewById(R.id.serverUrl);
        TextView username = findViewById(R.id.usernameInput);
        TextView password = findViewById(R.id.passwordInput);

        loginBtn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Log.i("YAYAYAYAYAY BUTTON CLICKEd ", "YAYAYAYAYAY");
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Retrive Token from main server
                            URL url = new URL("https://vnc.fgbox.appboxes.co/dose/api/auth/login");
                            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                            conn.setRequestMethod("POST");
                            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                            conn.setRequestProperty("Accept", "application/json");
                            conn.setDoOutput(true);
                            conn.setDoInput(true);

                            JSONObject jsonParam = new JSONObject();
                            jsonParam.put("username", username.getText().toString());
                            jsonParam.put("password", password.getText().toString());

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

                            while ((b = in.read()) != -1) {
                                respDataBuf.append((char) b);
                            }
                            responseMsg = respDataBuf.toString();
                            JSONObject jsonObject = new JSONObject(new JSONTokener(responseMsg));
                            Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                            Log.i("MSG", conn.getResponseMessage());
                            Log.i("Token = ", jsonObject.getString("token"));
                            conn.disconnect();

                            String JWT = jsonObject.getString("token");

                            SharedPreferences settings = getSharedPreferences("UserInfo", 0);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString("JWT", JWT);
                            editor.putString("ServerURL", serverURL.getText().toString());
                            editor.commit();

                            finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });

                thread.start();
            }
        });
    }
}