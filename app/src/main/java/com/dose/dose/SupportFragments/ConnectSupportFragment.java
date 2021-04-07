package com.dose.dose.SupportFragments;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;

import com.dose.dose.ApiClient.DoseAPIClient;
import com.dose.dose.ApiClient.RequestNoAuth;
import com.dose.dose.R;

import org.json.JSONObject;

import java.util.List;

public class ConnectSupportFragment extends GuidedStepSupportFragment {
    private static final int CONNECT = 917;
    private static final int CONTINUE = 707;
    private GuidedAction codeAction;
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        String title = getString(R.string.connect_title);
        String breadcrumb = getString(R.string.connect_breadcrumb);
        String description = getString(R.string.connect_description);
        Drawable icon = getActivity().getDrawable(R.drawable.dose);
        return new GuidanceStylist.Guidance(title, description, breadcrumb, icon);
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        codeAction = new GuidedAction.Builder()
                .id(CONNECT)
                .title("Code")
                .description("Insert code from the main server")
                .hasNext(true)
                .editable(true)
                .build();
        actions.add(codeAction);

        actions.add(new GuidedAction.Builder()
                .id(CONTINUE)
                .title("Continue")
                .description("Continue to next step")
                .hasNext(true)
                .editable(false)
                .build());
        super.onCreateActions(actions, savedInstanceState);
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        super.onGuidedActionClicked(action);

        if (action.getId() == CONTINUE) {
            SharedPreferences settings = getActivity().getSharedPreferences("UserInfo", 0);
            String mainServerURL = settings.getString("MainServerURL", "").toString();
            String code = String.valueOf(codeAction.getTitle());
            String url = String.format("%s/api/auth/tv/registerCode?code=%s", mainServerURL, code);

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    JSONObject authResult = RequestNoAuth.get(url);
                    try {
                        if (authResult.getString("status").equals("success")) {
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString("MainServerJWT", authResult.getString("token"));
                            editor.putString("MainServerRefreshToken", authResult.getString("refreshToken"));
                            editor.putString("MainServerValidTo", authResult.getString("validTo"));
                            editor.commit();

                            FragmentManager fm = getFragmentManager();
                            GuidedStepSupportFragment.add(fm, new ServerChoiceSupportFragment());
                        } else {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getActivity(), "Invalid code", Toast.LENGTH_LONG).show();
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            });
            thread.start();
        }
    }
}
