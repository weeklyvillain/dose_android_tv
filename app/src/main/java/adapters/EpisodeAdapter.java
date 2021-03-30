package adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dose.dose.R;
import com.dose.dose.content.Episode;
import com.dose.dose.content.Season;

import java.util.List;

public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>  {
    private Context context;
    private final List<Episode> episodes;

    public EpisodeAdapter(Context context, List<Episode> episodes) {
        this.context = context;
        this.episodes = episodes;
    }

    @NonNull
    @Override
    public EpisodeAdapter.EpisodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.episode_backdrop_card, parent, false);
        return new EpisodeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EpisodeViewHolder holder, int position) {
        Episode currentEpisodeItem = getItem(position);
        Log.i("CURRENTEPISODE: ", currentEpisodeItem.toString());
        holder.episodeNumber.setText(Integer.toString(currentEpisodeItem.getEpisodeNumber()));
        holder.episodeName.setText(currentEpisodeItem.getTitle());

        Glide.with(context)
                .load(episodes.get(position).getCardImageUrl(true))
                .into(holder.episodeBackdrop);
    }

    @Override
    public int getItemCount() {
        return episodes.size();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public Episode getItem(int position) {
        return episodes.get(position);
    }


    public class EpisodeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final Context mContext;
        private final TextView episodeNumber;
        private final TextView episodeName;
        private final ImageView episodeBackdrop;

        public EpisodeViewHolder(View itemView) {
            super(itemView);
            itemView.setClickable(true);
            itemView.setOnClickListener(this);
            mContext = itemView.getContext();
            episodeNumber = itemView.findViewById(R.id.episodeNumber);
            episodeName = itemView.findViewById(R.id.episodeName);
            episodeBackdrop = itemView.findViewById(R.id.episodeBackdrop);

            itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {

                    if (hasFocus) {
                        episodeBackdrop.setColorFilter(Color.rgb(123, 123, 123), android.graphics.PorterDuff.Mode.MULTIPLY);
                    } else {
                        episodeBackdrop.clearColorFilter();
                    }
                }
            });
        }

        @Override
        public void onClick(View v) {
            if (getAdapterPosition() == RecyclerView.NO_POSITION) return;
            Episode episode = getItem(getAdapterPosition());
            Log.i("SELECTED: ", episode.toString());

        }
    }
}

