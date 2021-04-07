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
import com.dose.dose.R;

import java.util.List;

public class ContentServerSupportFragment extends GuidedStepSupportFragment {
    private static final int CONTENT_SERVER = 270;
    private static final int CONTINUE = 271;

    private GuidedAction mainServerAction;

    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {

        String title = getString(R.string.setup_content_server_title);
        String breadcrumb = getString(R.string.setup_content_server_breadcrumb);
        String description = getString(R.string.setup_content_server_description);
        Drawable icon = getActivity().getDrawable(R.drawable.dose);
        return new GuidanceStylist.Guidance(title, description, breadcrumb, icon);
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        SharedPreferences settings = getActivity().getSharedPreferences("UserInfo", 0);
        String mainServerURL = settings.getString("MainServerURL", "Content server");

        mainServerAction = new GuidedAction.Builder()
        .id(CONTENT_SERVER)
        .title(mainServerURL)
        .description("Insert content server IP/URL")
        .hasNext(true)
        .editable(true)
        .build();
        actions.add(mainServerAction);

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
            Log.i("CONTINUE: ", String.valueOf(mainServerAction.getTitle()));
            //GuidedStepSupportFragment.add(fm, new SecondStepFragment());
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean mainServerOK = DoseAPIClient.ping(String.valueOf(mainServerAction.getTitle()));
                    if (mainServerOK) {
                        SharedPreferences settings = getActivity().getSharedPreferences("UserInfo", 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("MainServerURL", String.valueOf(mainServerAction.getTitle()));
                        editor.commit();

                        Log.i("CONTINUE: ", "OK");
                        FragmentManager fm = getFragmentManager();
                        GuidedStepSupportFragment.add(fm, new ConnectSupportFragment());
                    } else {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getActivity(), "Couldn't connect to main server", Toast.LENGTH_LONG).show();
                        });
                    }
                }
            });
            thread.start();

        }
    }

    @Override
    public long onGuidedActionEditedAndProceed(GuidedAction action) {
        if (action.getId() == CONTENT_SERVER) {
            //this.mainServer = String.valueOf(action.getTitle());
        }
        return super.onGuidedActionEditedAndProceed(action);
    }
}
