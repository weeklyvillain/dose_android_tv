package com.dose.dose;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.leanback.app.GuidedStepSupportFragment;

import com.dose.dose.ApiClient.DoseAPIClient;
import com.dose.dose.SupportFragments.ContentServerSupportFragment;

import org.json.JSONObject;

public class LoginActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ContentServerSupportFragment contentServerSupportFragment = new ContentServerSupportFragment();
        GuidedStepSupportFragment.addAsRoot(this, contentServerSupportFragment, android.R.id.content);

    }
}