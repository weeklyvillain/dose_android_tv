package com.dose.dose;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.LoadErrorHandlingPolicy;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;


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

    private ImageButton playPauseButton;

    private int timeAtSeek = 0;
    private final Handler currentTimeHandler = new Handler();

    private final Runnable currentTimeUpdater = new Runnable() {
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

    private final Runnable seekRunnable = new Runnable() {
        @Override
        public void run() {
            seek(seekBar.getProgress());
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
        playPauseButton = findViewById(R.id.imageButton);

        mSelectedMovie =
                (Movie) getIntent().getSerializableExtra(DetailsActivity.MOVIE);

        SharedPreferences settings = getSharedPreferences("UserInfo", 0);
        String JWT = settings.getString("MainServerJWT", "").toString();
        String mainServerURL = settings.getString("MainServerURL", "").toString();
        String contentServerURL = settings.getString("ContentServerURL", "").toString();
        String contentServerJWT = settings.getString("ContentServerJWT", "").toString();

        movieAPIClient = new MovieAPIClient(mainServerURL, contentServerURL, JWT, contentServerJWT);


        movieAPIClient = new MovieAPIClient(mainServerURL,
                contentServerURL,
                JWT,
                contentServerJWT);

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
                    seekBar.setKeyProgressIncrement(10);
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
                if (fromUser) {
                    isSeeking = true;
                    currentTimeHandler.removeCallbacks(seekRunnable);
                    currentTimeHandler.postDelayed(seekRunnable, 2500);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });

        DefaultLoadControl loadControl = new DefaultLoadControl.Builder().setBufferDurationsMs(3000, 3600001, 2500, 2500).build();
        // TODO: We have to add error handler since we don't retry after timeout. App will just crash
        player = new SimpleExoPlayer.Builder(this)
                .setLoadControl(loadControl)
                .build();
        Player.EventListener eventListener = new Player.EventListener() {

            @Override
            public void onPlaybackStateChanged(int state) {
                Log.i("ONPLAYBACKCHANGE; ", String.format("NEW STATE: %d", state));
            }
        };
        player.addListener(eventListener);
        playerView.setPlayer(player);
        playerView.setUseController(false);
        playerView.getVideoSurfaceView().setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.i("KLICK", "KLIIICK");
                controlsLayout.setVisibility(View.VISIBLE);
                controlsVisible = true;
                playPauseButton.requestFocus();
            }
        });


        String userAgent = Util.getUserAgent(this, "Dose");

        DefaultHttpDataSourceFactory httpDataSourceFactory = new DefaultHttpDataSourceFactory(
                userAgent,
                null /* listener */,
                100000,
                100000,
                true /* allowCrossProtocolRedirects */
        );


        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, null, httpDataSourceFactory);
        MediaSource mediaSource = new DefaultMediaSourceFactory(dataSourceFactory).setLoadErrorHandlingPolicy(getMyErrorHandlingPolicy())
                .createMediaSource(
                        MediaItem.fromUri(
                                Uri.parse(
                                        movieAPIClient.getPlaybackURL(mSelectedMovie.getId(), 0, "1080P")
                                )
                        )
        );
        player.setMediaSource(mediaSource);
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
            player.stop();
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

    // @param seekTo -> seekTo in seconds
    private void seek(int seekTo) {
        Log.i("SeekTo: ", String.valueOf(seekTo));
        player.stop();

        String userAgent = Util.getUserAgent(this, "Dose");

        DefaultHttpDataSourceFactory httpDataSourceFactory = new DefaultHttpDataSourceFactory(
                userAgent,
                null /* listener */,
                100000,
                100000,
                true /* allowCrossProtocolRedirects */
        );


        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, null, httpDataSourceFactory);
        MediaSource mediaSource = new DefaultMediaSourceFactory(dataSourceFactory).setLoadErrorHandlingPolicy(getMyErrorHandlingPolicy())
                .createMediaSource(
                        MediaItem.fromUri(
                                Uri.parse(
                                        movieAPIClient.getPlaybackURL(mSelectedMovie.getId(), seekTo, "1080P")
                                )
                        )
                );



        player.setMediaSource(mediaSource);
        player.prepare();
        player.play();
        timeAtSeek = seekTo;
        isSeeking = false;
    }


    private static LoadErrorHandlingPolicy getMyErrorHandlingPolicy(){
//        LoadErrorHandlingPolicy loadErrorHandlingPolicy = new DefaultLoadErrorHandlingPolicy();

        return new LoadErrorHandlingPolicy() {

            @Override
            public long getRetryDelayMsFor(LoadErrorInfo loadErrorInfo) {
                return C.TIME_UNSET;
            }

            @Override
            public int getMinimumLoadableRetryCount(int dataType) {
                return 0;
            }
        };
    }

}