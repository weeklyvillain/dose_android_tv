package com.dose.dose;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.StyledPlayerView;


public class VideoActivity extends Activity {
    //private static VideoView videoview;
    private Movie mSelectedMovie;
    private MovieAPIClient movieAPIClient;
    private SimpleExoPlayer player;
    private MediaItem mediaItem;
    // TextViews
    private TextView currentTime;
    private TextView durationTextView;

    // Seekbar
    private SeekBar seekBar;
    private boolean isSeeking = false;

    // Controls
    private ConstraintLayout controlsLayout;
    private boolean controlsVisible = false;

    private int timeAtSeek = 0;
    private Handler currentTimeHandler = new Handler();

    private Runnable currentTimeUpdater = new Runnable() {
        @Override
        public void run() {
            int playedInSeconds = Math.toIntExact(timeAtSeek + player.getCurrentPosition() / 1000);
            int hours = playedInSeconds / 60 / 60;
            int minutes = (playedInSeconds / 60) % 60;
            int seconds = playedInSeconds % 60;

            // Seekbar changes
            if (!isSeeking) {
                seekBar.setProgress(playedInSeconds);
            }

            currentTime.setText(String.format("%d:%d:%d", hours,minutes,seconds));
            currentTime.invalidate();
            currentTimeHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        StyledPlayerView playerView = findViewById(R.id.player_view);
        currentTime = findViewById(R.id.currentTime);
        durationTextView = findViewById(R.id.duration);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        controlsLayout = findViewById(R.id.controlsLayout);

        mSelectedMovie =
                (Movie) getIntent().getSerializableExtra(DetailsActivity.MOVIE);
        movieAPIClient = new MovieAPIClient("https://vnc.fgbox.appboxes.co/dose",
                "https://vnc.fgbox.appboxes.co/doseserver",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiI0IiwiZW1haWwiOiJ0ZXN0QGdtYWlsLmNvbSIsInVzZXJuYW1lIjoiVmV6ZWwiLCJpYXQiOjE2MTU4MDUwNTgsImV4cCI6MTYxNjEwNTA1OH0.-ZNHi4sDi9926SeqApe5hf6NmqPEbN3jp5UBP3OdoOg",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6IlZlemVsIiwidXNlcl9pZCI6IjEiLCJpYXQiOjE2MTU5MTQxODUsImV4cCI6MTYxNjIxNDE4NX0.97PMAZ3lfGD5e0JtsE49O6pgsPisMoka_Dpf033QVzY");

        // Get the duration of the video
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int duration = movieAPIClient.getDuration(mSelectedMovie.getId());
                    int hours = duration / 60 / 60;
                    int minutes = (duration / 60) % 60;
                    int seconds = duration % 60;
                    seekBar.setMax(duration);
                    durationTextView.setText(String.format("%d:%d:%d", hours,minutes,seconds));
                    durationTextView.invalidate();
                } catch (Exception e) {
                    Log.i("GetDurationError: ", e.toString());
                    // Do something
                }

            }
        });
        thread.start();



        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                player.stop();
                mediaItem = MediaItem.fromUri("https://vnc.fgbox.appboxes.co/doseserver/api/video/280?type=movie&token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6IlZlemVsIiwidXNlcl9pZCI6IjEiLCJpYXQiOjE2MTU4MjMzMjIsImV4cCI6MTYxNjEyMzMyMn0.8MbofgLdiB6IEbJkJ3ej3ef0lb5ad2zjqeWtqUZAm8o&start=1000&quality=1080P");
                player.setMediaItem(mediaItem);
                player.prepare();
                player.play();
                isSeeking = false;
            }
        });
        //playVideo();

        player = new SimpleExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        playerView.setUseController(false);
        playerView.getVideoSurfaceView().setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                controlsLayout.setVisibility(View.VISIBLE);
                controlsVisible = true;
            }
        });

        mediaItem = MediaItem.fromUri("https://vnc.fgbox.appboxes.co/doseserver/api/video/280?type=movie&token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6IlZlemVsIiwidXNlcl9pZCI6IjEiLCJpYXQiOjE2MTU5MTQxODUsImV4cCI6MTYxNjIxNDE4NX0.97PMAZ3lfGD5e0JtsE49O6pgsPisMoka_Dpf033QVzY&start=0&quality=1080P");
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
        currentTimeHandler.post(currentTimeUpdater);



    }

    @Override
    public void onBackPressed() {
        if (controlsVisible) {
            controlsLayout.setVisibility(View.INVISIBLE);
            controlsVisible = false;
        } else {
            super.onBackPressed();
        }
    }

    public void togglePlay(View view) {
        if (player.isPlaying()) {
            player.pause();
        } else {
            player.play();
        }
    }




}