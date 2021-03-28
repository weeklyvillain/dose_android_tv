package adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dose.dose.R;
import com.dose.dose.content.Season;

import java.util.List;

public class SeasonAdapter extends RecyclerView.Adapter<SeasonAdapter.SeasonViewHolder> {
    private AdapterView.OnItemClickListener listener;
    private Context context;
    private final List<Season> seasons;

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


    public class SeasonViewHolder extends RecyclerView.ViewHolder {
        private Context mContext;
        private TextView seasonTextView;
        private ImageView seasonPoster;

        public SeasonViewHolder(View itemView) {
            super(itemView);
            mContext = itemView.getContext();
            seasonTextView = itemView.findViewById(R.id.seasonText);
            seasonPoster = itemView.findViewById(R.id.poster);

            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        //listener.onItemClick(getItem(position));
                    }
                }
            });
        }

    }
}
