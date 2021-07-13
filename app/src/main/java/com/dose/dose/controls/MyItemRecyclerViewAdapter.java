package com.dose.dose.controls;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dose.dose.R;
import com.dose.dose.dummy.DummyContent.DummyItem;
import com.dose.dose.interfaces.SelectedSetting;
import com.dose.dose.interfaces.VideoControlListInterface;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    private final List<ControlSetting> mValues;
    private VideoControlListInterface videoControlListInterface;
    // Indicates if this instance is changing audio, resolution or subtitle
    private SelectedSetting selectedSetting;

    public MyItemRecyclerViewAdapter(List<ControlSetting> items, SelectedSetting selectedSetting, VideoControlListInterface videoControlListInterface) {
        mValues = items;
        this.selectedSetting = selectedSetting;
        this.videoControlListInterface = videoControlListInterface;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_resolution, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        holder.mView.requestFocus();
        super.onViewAttachedToWindow(holder);
    }

    public ControlSetting getItem(int position) {
        return mValues.get(position);
    }

    public SelectedSetting getSelectedSetting() {
        return selectedSetting;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position).getValue();
        holder.mContentView.setText(mValues.get(position).getValue());
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final View mView;
        public final TextView mContentView;
        public String mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mView.setClickable(true);
            mView.setFocusable(true);
            mView.setOnClickListener(this);
            mContentView = (TextView) view.findViewById(R.id.content);
            mView.setOnFocusChangeListener((v, hasFocus) -> {

                if (hasFocus) {
                    mView.setBackgroundColor(Color.GREEN);
                    mContentView.setTextColor(Color.BLACK);
                    ((LinearLayout)view).setNextFocusDownId(view.getId());
                    Log.i("FOCUS", "CHAAAANGE");
                } else {
                    mView.setBackgroundColor(Color.TRANSPARENT);
                }
            });
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }

        @Override
        public void onClick(View v) {
            Log.i("KLICK", "HEJ");
            ControlSetting selectedValue = getItem(getAdapterPosition());
            videoControlListInterface.settingSelected(selectedValue, getSelectedSetting());
        }
    }
}