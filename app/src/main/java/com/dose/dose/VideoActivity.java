package com.dose.dose;

import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.dose.dose.ApiClient.DoseAPIClient;
import com.dose.dose.ApiClient.MovieAPIClient;
import com.dose.dose.ApiClient.ShowAPIClient;
import com.dose.dose.content.BaseContent;
import com.dose.dose.content.Episode;
import com.dose.dose.content.Movie;
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
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.LoadErrorHandlingPolicy;
import com.google.android.exoplayer2.util.Util;


public class VideoActivity extends Activity {
    //private static VideoView videoview;
    public static String TYPE = "Type";
    public static String MOVIE = "Movie";
    public static String CONTINUE_WATCHING = "ContinueWatching";
    public static String EPISODE = "Episode";
    public static String SHARED_ELEMENT_NAME ="hero";

    private BaseContent selectedContent;
    private Type selectedType;
    private boolean continueWatching;
    public enum Type {
        EPISODE,
        MOVIE
    };
    private DoseAPIClient apiClient;
    private SimpleExoPlayer player;
    private MediaItem mediaItem;

    StyledPlayerView playerView;
    private boolean fetchedDuration = false;

    // TextViews
    private TextView currentTime;
    private TextView durationTextView;

    // Seekbar
    private SeekBar seekBar;
    private boolean isSeeking = false;

    // Next episode
    private ConstraintLayout nextEpisodeLayout;
    private TextView nextEpisodeSecondsLeft;
    private ImageButton nextEpisodeButton;
    private boolean nextEpisodeVisible = false;
    private Episode nextEpisode;
    private boolean foundNextEpisode;

    // Controls
    private ConstraintLayout controlsLayout;
    private boolean controlsVisible = false;

    private ImageButton playPauseButton;

    private int timeAtSeek = 0;
    private final Handler currentTimeHandler = new Handler();

    private static final int CURRENT_TIME_UPDATE_FREQ = 10;

    private final Runnable currentTimeUpdater = new Runnable() {
        @Override
        public void run() {
            int playedInSeconds = Math.toIntExact(timeAtSeek + player.getCurrentPosition() / 1000);
            int hours = playedInSeconds / 60 / 60;
            int minutes = (playedInSeconds / 60) % 60;
            int seconds = playedInSeconds % 60;

            int contentDuration = selectedContent.getDuration();
            // Next episode checks
            if (playedInSeconds >= contentDuration - 40 && foundNextEpisode && fetchedDuration) {
                updateNextEpisodeBox(selectedContent.getDuration() - playedInSeconds);
                if (selectedContent.getDuration() - playedInSeconds == 0) {
                    playNextEpisode();
                }
                if (!nextEpisodeVisible) {
                    showNextEpisodeBox();
                    nextEpisodeVisible = true;
                }
            }

            // Update current time on the server
            if (playedInSeconds % CURRENT_TIME_UPDATE_FREQ == 0 && player.isPlaying()) {
                selectedContent.setWatchTime(playedInSeconds);
                // Run on seperate thread
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        apiClient.updateCurrentTime(selectedContent.getId(), playedInSeconds, selectedContent.getDuration());
                    }
                });
                thread.start();
            }

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

        playerView = findViewById(R.id.player_view);
        currentTime = findViewById(R.id.currentTime);
        durationTextView = findViewById(R.id.duration);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        controlsLayout = findViewById(R.id.controlsLayout);
        playPauseButton = findViewById(R.id.imageButton);

        // Next episode
        nextEpisodeLayout = findViewById(R.id.nextEpisodeLayout);
        nextEpisodeSecondsLeft = findViewById(R.id.nextEpisodeSecondsLeft);
        nextEpisodeButton = findViewById(R.id.playNextEpisodeBtn);
        nextEpisodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNextEpisode();
            }
        });

        selectedType = (Type) getIntent().getSerializableExtra(VideoActivity.TYPE);
        continueWatching = (boolean) getIntent().getSerializableExtra(VideoActivity.CONTINUE_WATCHING);
        if (selectedType == Type.MOVIE) {
            selectedContent =
                    (Movie) getIntent().getSerializableExtra(VideoActivity.MOVIE);
            apiClient = MovieAPIClient.newInstance(this);
        } else {
            selectedContent = (Episode) getIntent().getSerializableExtra(VideoActivity.EPISODE);
            apiClient = ShowAPIClient.newInstance(this);
        }

        setDuration();
        if (selectedType == Type.EPISODE) {
            setNextEpisode();
        }
        setupSeekbar();
        setupVideo();
        timeAtSeek = continueWatching ? selectedContent.getWatchTime() : 0;
        // Seek starts the video
        seek(timeAtSeek);
        currentTimeHandler.post(currentTimeUpdater);
    }

    @Override
    public void onBackPressed() {
        if (controlsVisible) {
            controlsLayout.setVisibility(View.INVISIBLE);
            controlsVisible = false;
        } else {
            if (selectedType == Type.EPISODE) {
                MainFragment.updateOngoingEpisode((Episode)selectedContent);
            } else {
                MainFragment.updateAllMovieInfo((Movie)selectedContent);
            }
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
                                        apiClient.getPlaybackURL(selectedContent.getId(), seekTo, "1080P")
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

    private void setupSeekbar() {
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
    }

    private void setupVideo() {
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
    }

    private void setDuration() {
        fetchedDuration = false;
        // Get the duration of the video and the next episode (if we are playing a episode)
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int duration = apiClient.getDuration(selectedContent.getId());
                    selectedContent.setDuration(duration);
                    int hours = duration / 60 / 60;
                    int minutes = (duration / 60) % 60;
                    int seconds = duration % 60;
                    seekBar.setMax(duration);
                    seekBar.setKeyProgressIncrement(10);
                    durationTextView.setText(String.format("%d:%d:%d", hours,minutes,seconds));
                    durationTextView.invalidate();
                    fetchedDuration = true;
                } catch (Exception e) {
                    Log.i("GetDurationError: ", e.toString());
                    e.printStackTrace();
                    // Do something
                }

            }
        });
        thread.start();
    }

    private void setNextEpisode() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                nextEpisode = ((ShowAPIClient) apiClient).getNextEpisode((Episode) selectedContent);
                foundNextEpisode = nextEpisode != null;
            }
        });
        thread.start();
    }

    private void showNextEpisodeBox() {
        nextEpisodeLayout.setVisibility(View.VISIBLE);
    }

    private void hideNextEpisodeBox() {
        nextEpisodeLayout.setVisibility(View.INVISIBLE);
    }

    private void updateNextEpisodeBox(int secondsLeft) {
        nextEpisodeSecondsLeft.setText(String.format("%d seconds", secondsLeft));
    }

    private void playNextEpisode() {
        player.stop();
        MainFragment.removeEpisodeFromOngoing((Episode)selectedContent);
        selectedContent = nextEpisode;

        hideNextEpisodeBox();
        setDuration();
        setNextEpisode();
        timeAtSeek = 0;
        // Seek starts the video and since we changes selectedContent it will start the new episode
        seek(timeAtSeek);
    }

}