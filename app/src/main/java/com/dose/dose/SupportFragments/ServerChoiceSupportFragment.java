package com.dose.dose.SupportFragments;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;

import com.dose.dose.ApiClient.DoseAPIClient;
import com.dose.dose.ApiClient.RequestNoAuth;
import com.dose.dose.R;
import com.dose.dose.token.Token;
import com.dose.dose.token.TokenHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ServerChoiceSupportFragment extends GuidedStepSupportFragment {
    private List<JSONObject> servers = new ArrayList<>();
    private static final int SERVER_CHOICE = 319;
    private String mainServerURL;
    private String mainServerJWT;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        SharedPreferences settings = getActivity().getSharedPreferences("UserInfo", 0);

        mainServerURL = settings.getString("MainServerURL", "").toString();
        mainServerJWT = settings.getString("MainServerJWT", "").toString();

        super.onCreate(savedInstanceState);
    }

    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        String title = getString(R.string.server_choice_title);
        String breadcrumb = getString(R.string.server_choice_breadcrumb);
        String description = getString(R.string.server_choice_description);
        Drawable icon = getActivity().getDrawable(R.drawable.dose);

        return new GuidanceStylist.Guidance(title, description, breadcrumb, icon);
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {

        String url = String.format("%s/api/servers/getServers?token=%s", mainServerURL, mainServerJWT);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject result = RequestNoAuth.get(url);
                Log.i("SERVERS: ", result.toString());
                JSONArray fetchedServers;
                try {
                    fetchedServers = result.getJSONArray("servers");
                } catch (Exception e) {
                    fetchedServers = new JSONArray();
                    e.printStackTrace();
                }

                List<GuidedAction> subActions = new ArrayList<GuidedAction>();
                try {
                    int id = 0;
                    for (int i = 0; i < fetchedServers.length(); i++) {
                        fetchedServers.getJSONObject(i).put("id", id);

                        servers.add(fetchedServers.getJSONObject(i));

                        subActions.add(new GuidedAction.Builder()
                                .id(id)
                                .title(fetchedServers.getJSONObject(i).getString("server_name"))
                                .description(fetchedServers.getJSONObject(i).getString("server_ip"))
                                .build());
                        id++;
                    }

                    actions.add(new GuidedAction.Builder()
                            .id(SERVER_CHOICE)
                            .title("Choose content server")
                            .description("Please select a server")
                            .subActions(subActions)
                            .build());

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setActions(actions);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Override
    public boolean onSubGuidedActionClicked(GuidedAction action) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < servers.size(); i++) {
                        if (action.getId() == servers.get(i).getInt("id")) {
                            Log.i("SELECTED", servers.get(i).toString());
                            String url = String.format("%s/api/auth/validate", servers.get(i).getString("server_ip"));

                            JSONObject jsonParam = new JSONObject();
                            try {
                                jsonParam.put("token", mainServerJWT);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            JSONObject result = RequestNoAuth.post(url, jsonParam);
                            Log.i("CONNECT: ", result.toString());

                            if (result.getString("status").equals("success")) {
                                Token token = new Token(result.getString("token"), result.getDouble("validTo"));
                                TokenHandler.Tokenhandler(requireContext()).setContentToken(token, requireActivity());

                                SharedPreferences settings = requireActivity().getSharedPreferences("UserInfo", 0);
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putString("ContentServerURL", servers.get(i).getString("server_ip"));
                                editor.apply();
                                finishGuidedStepSupportFragments();
                            } else {
                                requireActivity().runOnUiThread(() -> {
                                    Toast.makeText(getActivity(), "Couldn't validate towards server", Toast.LENGTH_LONG).show();
                                });
                            }
                        }
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

        return true;
    }
}
