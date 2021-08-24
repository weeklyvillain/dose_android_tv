package com.dose.dose;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BrowseMenuFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BrowseMenuFragment extends Fragment {

    public BrowseMenuFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BrowseMenuFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BrowseMenuFragment newInstance(String param1, String param2) {
        BrowseMenuFragment fragment = new BrowseMenuFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_browse_menu, container, false);
    }
}