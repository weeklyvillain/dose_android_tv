package com.dose.dose.search;

import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;

import com.dose.dose.R;

public class SearchActivity extends FragmentActivity {
    public static final String SHARED_ELEMENT_NAME = "Hero";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

    }
}