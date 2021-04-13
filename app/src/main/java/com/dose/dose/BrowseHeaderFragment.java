package com.dose.dose;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.palette.graphics.Palette;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.TransitionOptions;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.dose.dose.content.BaseContent;
import com.dose.dose.content.Movie;
import com.dose.dose.databinding.FragmentBrowseHeaderBinding;
import com.dose.dose.viewModels.SelectedViewModel;

import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class BrowseHeaderFragment extends Fragment {
    private BaseContent selected;
    private ImageView backdrop;
    private TextView title;
    private TextView description;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Create empty movie
        selected = new Movie();

        // Inflate the layout for this fragment
        FragmentBrowseHeaderBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_browse_header, container, false);
        View view = binding.getRoot();
        binding.setSelected(selected);

        backdrop = view.findViewById(R.id.header_backdrop);
        title = view.findViewById(R.id.header_title);
        description = view.findViewById(R.id.header_description);

        SelectedViewModel model = new ViewModelProvider(requireActivity()).get(SelectedViewModel.class);
        model.getSelected().observe(getViewLifecycleOwner(), item -> {
            selected.setTitle(item.getTitle());
            selected.setDescription(item.getDescription());

            Glide.with(getContext())
                    .load(item.getCardImageUrl(true))
                    .transition(DrawableTransitionOptions.withCrossFade(250))
                    .into(backdrop);
        });

        return view;
    }

}