package adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.dose.dose.R;
import com.dose.dose.content.Season;
import com.dose.dose.details.MovieDetailsActivity;
import com.dose.dose.details.SeasonDetailsActivity;

import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class SeasonAdapter extends RecyclerView.Adapter<SeasonAdapter.SeasonViewHolder> {
    private AdapterView.OnItemClickListener listener;
    private Context context;
    private final List<Season> seasons;
    int selectedPos = 0;

    public SeasonAdapter(Context context, List<Season> seasons) {
        this.context = context;
        this.seasons = seasons;
    }

    @NonNull
    @Override
    public SeasonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.season_poster_card, parent, false);
        return new SeasonViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SeasonViewHolder holder, int position) {
        Season currentSeasonItem = getItem(position);
        holder.seasonTextView.setText(currentSeasonItem.getTitle());
        Glide.with(context)
                .load(seasons.get(position).getPosterImage(true))
                .into(holder.seasonPoster);

    }

    public Season getItem(int position) {
        return seasons.get(position);
    }


    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return seasons.size();
    }



    public class SeasonViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Context mContext;
        private TextView seasonTextView;
        private ImageView seasonPoster;
        private ConstraintLayout layout;

        public SeasonViewHolder(View itemView) {
            super(itemView);
            itemView.setClickable(true);
            itemView.setOnClickListener(this);
            mContext = itemView.getContext();
            seasonTextView = itemView.findViewById(R.id.seasonText);
            seasonPoster = itemView.findViewById(R.id.poster);
            layout = itemView.findViewById(R.id.layout);

            itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {

                    if (hasFocus) {
                        seasonTextView.setVisibility(View.VISIBLE);
                        seasonPoster.setColorFilter(Color.rgb(123, 123, 123), android.graphics.PorterDuff.Mode.MULTIPLY);
                    } else {
                        seasonTextView.setVisibility(View.INVISIBLE);
                        seasonPoster.clearColorFilter();
                    }
                }
            });
        }


        @Override
        public void onClick(View v) {
            // Below line is just like a safety check, because sometimes holder could be null,
            // in that case, getAdapterPosition() will return RecyclerView.NO_POSITION
            if (getAdapterPosition() == RecyclerView.NO_POSITION) return;

            Season season = getItem(getAdapterPosition());
            Log.i("SELECTED: ", season.toString());

            Intent intent = new Intent(context, SeasonDetailsActivity.class);
            intent.putExtra(SeasonDetailsActivity.SEASON, season);
            context.startActivity(intent);

            // Do your another stuff for your onClick
        }
    }
}
