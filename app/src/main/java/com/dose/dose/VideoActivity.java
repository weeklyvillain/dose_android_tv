package com.dose.dose;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.dose.dose.ApiClient.DoseAPIClient;
import com.dose.dose.ApiClient.MovieAPIClient;
import com.dose.dose.ApiClient.ShowAPIClient;
import com.dose.dose.content.BaseContent;
import com.dose.dose.content.Episode;
import com.dose.dose.content.Movie;
import com.dose.dose.controls.AudioSetting;
import com.dose.dose.controls.ControlSetting;
import com.dose.dose.controls.SubtitleSetting;
import com.dose.dose.controls.VideoControlListInterfaceFragment;
import com.dose.dose.controls.VideoSeekFragment;
import com.dose.dose.interfaces.VideoControlListInterface;
import com.dose.dose.controls.ResolutionSetting;
import com.dose.dose.controls.VideoControlFragment;
import com.dose.dose.interfaces.SelectedSetting;
import com.dose.dose.interfaces.VideoControlInterface;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.LoadErrorHandlingPolicy;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;


public class VideoActivity extends FragmentActivity implements VideoControlListInterface, VideoControlInterface {
    //private static VideoView videoview;
    public static String TYPE = "Type";
    public static String MOVIE = "Movie";
    public static String CONTINUE_WATCHING = "ContinueWatching";
    public static String EPISODE = "Episode";
    public static String SHARED_ELEMENT_NAME ="hero";

    // Used to figure out if we are doing short/long press
    private boolean clickFlag = false;
    private boolean clickFlag2 = false;

    // Overlay
    private LinearLayout videoOverlayLayout;
    private ImageView pauseIcon;
    private LinearLayout videoInformationLayout;
    private TextView videoTitle;
    private TextView videoDesription;

    private BaseContent selectedContent;
    private Type selectedType;
    private boolean continueWatching;

    public enum Type {
        EPISODE,
        MOVIE
    };
    private DoseAPIClient apiClient;
    private SimpleExoPlayer player;
    private MediaSource videoMediaSource;
    private DefaultTrackSelector trackSelector;

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

    private int timeAtSeek = 0;
    private final Handler currentTimeHandler = new Handler();

    private static final int CURRENT_TIME_UPDATE_FREQ = 10;

    private VideoControlListInterfaceFragment settingFragment = null;
    private FragmentManager fragmentManager = null;
    private FragmentTransaction fragmentTransaction;
    private ResolutionSetting currentResolution;
    // Default from the server (not specified in the request)
    private AudioSetting currentAudioStream = new AudioSetting("Unknown", -1);

    private VideoControlFragment videoControlFragment = null;
    private VideoSeekFragment videoSeekFragment = null;

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

            /*
            // Seekbar changes
            if (!isSeeking) {
                seekBar.setProgress(playedInSeconds);
            } */

            /*
            currentTime.setText(String.format("%d:%d:%d", hours,minutes,seconds));
            currentTime.invalidate();
             */
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

        if (fragmentManager == null) {
            fragmentManager = getSupportFragmentManager();
        }

        try {
            HttpsURLConnection.setDefaultSSLSocketFactory(new CustomSSLSocketFactory());
        } catch(Exception e) {
            e.printStackTrace();
            Log.i("OH", "NOO");
        }

        playerView = findViewById(R.id.player_view);
        currentTime = findViewById(R.id.currentTime);
        durationTextView = findViewById(R.id.duration);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        //controlsLayout = findViewById(R.id.controlsLayout);
        videoOverlayLayout = findViewById(R.id.videoOverlayLayout);
        pauseIcon = findViewById(R.id.pauseIcon);
        videoInformationLayout = findViewById(R.id.videoInformation);
        videoTitle = findViewById(R.id.videoTitle);
        videoDesription = findViewById(R.id.videoDescription);

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

        videoTitle.setText(selectedContent.getTitle());
        videoDesription.setText(selectedContent.getDescription());

        //setupSeekbar();
        setupVideo();
        getAudioStreams();
        //getSubtitles();
        currentTimeHandler.post(currentTimeUpdater);

        getResolutionAndStartVideo();
        setDuration();
        if (selectedType == Type.EPISODE) {
            setNextEpisode();
        }

    }

    @Override
    public void onBackPressed() {
        if (anyFragmentOpen()) {
            if (settingFragment != null) {
                fragmentManager.beginTransaction().remove(settingFragment).commit();
                settingFragment = null;
            } else if (videoControlFragment != null) {
                fragmentManager.beginTransaction().remove(videoControlFragment).commit();
                videoControlFragment = null;
                if (player.isPlaying()) {
                    videoOverlayLayout.setVisibility(View.INVISIBLE);
                    videoInformationLayout.setVisibility(View.INVISIBLE);
                }
            } else if (videoSeekFragment != null) {
                fragmentManager.beginTransaction().remove(videoSeekFragment).commit();
                videoSeekFragment = null;
                if (player.isPlaying()) {
                    videoOverlayLayout.setVisibility(View.INVISIBLE);
                    videoInformationLayout.setVisibility(View.INVISIBLE);
                }
            }
        } else if (controlsVisible) {
            controlsLayout.setVisibility(View.INVISIBLE);
            controlsVisible = false;
        } else {
            if (selectedType == Type.EPISODE) {
                //MainFragment.updateOngoingEpisode((Episode)selectedContent);
            } else {
                //MainFragment.updateAllMovieInfo((Movie)selectedContent);
            }
            player.stop();
            super.onBackPressed();
        }
    }

    private boolean anyFragmentOpen() {
        return settingFragment != null ||
                videoControlFragment != null ||
                videoSeekFragment != null;
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
                0,
                0,
                true /* allowCrossProtocolRedirects */
        );

        ArrayList<MediaItem.Subtitle> subtitleMediaItems = new ArrayList<>();
        List<ControlSetting> availableSubtitles = selectedContent.getAvailableSubtitles();
        for (int i = 0; i < availableSubtitles.size(); i++) {
            Log.i("URL: ", apiClient.getSubtitleUrl(availableSubtitles.get(i).getId(), seekTo));
            subtitleMediaItems.add(
                    new MediaItem.Subtitle(
                            Uri.parse(apiClient.getSubtitleUrl(availableSubtitles.get(i).getId(), seekTo)),
                            MimeTypes.TEXT_VTT,
                            "en",
                            C.SELECTION_FLAG_DEFAULT,
                            0,
                            String.valueOf(availableSubtitles.get(i).getId())
                    )
            );
        }

        /*
        subtitleMediaItems.add(
                new MediaItem.Subtitle(
                        Uri.parse("https://pastebin.com/raw/XYaWd0z6"),
                        MimeTypes.TEXT_VTT,
                        "en",
                        C.SELECTION_FLAG_DEFAULT
                )
        );*/
        Log.i("subs: ", subtitleMediaItems.toString());


        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, null, httpDataSourceFactory);
        this.videoMediaSource = new DefaultMediaSourceFactory(dataSourceFactory).setLoadErrorHandlingPolicy(getMyErrorHandlingPolicy())
                .createMediaSource(
                        new MediaItem.Builder()
                                .setUri(apiClient.getPlaybackURL(selectedContent.getId(), seekTo, currentResolution.getValue(), currentAudioStream.getId()))
                                .setSubtitles(subtitleMediaItems)
                                .build()
                        /*
                        MediaItem.fromUri(
                                Uri.parse(
                                        apiClient.getPlaybackURL(selectedContent.getId(), seekTo, currentResolution.getValue(), currentAudioStream.getId())
                                )
                        )
                        */
                );



        player.setMediaSource(this.videoMediaSource);
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

    /*
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
     */

    private void setupVideo() {
        DefaultLoadControl loadControl = new DefaultLoadControl.Builder().setBufferDurationsMs(2147483647, 2147483647, 2500, 2500).build();
        /*
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory());
        trackSelector.setParameters(new DefaultTrackSelector.ParametersBuilder()
                .setRendererDisabled(C.TRACK_TYPE_VIDEO, false)
                .build()
        );
        */

        trackSelector = new DefaultTrackSelector(this);
        trackSelector.setParameters(trackSelector.buildUponParameters().setRendererDisabled(C.TRACK_TYPE_VIDEO, false).setPreferredTextLanguage("en").build());
        // TODO: We have to add error handler since we don't retry after timeout. App will just crash
        player = new SimpleExoPlayer.Builder(this)
                .setLoadControl(loadControl)
                .setTrackSelector(trackSelector)
                .build();
        Player.EventListener eventListener = new Player.EventListener() {

            @Override
            public void onPlaybackStateChanged(int state) {
                Log.i("ONPLAYBACKCHANGE; ", String.format("NEW STATE: %d", state));
            }
        };
        player.addListener(eventListener);
        player.addTextOutput(cues -> {
            Log.i("INNE I ", "CUES");
            for (int i = 0; i < cues.size(); i++) {
                Log.i("CUE ITEM: ", cues.get(i).text.toString());
            }
            //Log.d("subtitles", cues.get(0).text.toString());
            playerView.getSubtitleView().setCues(cues);
            playerView.getSubtitleView().setVisibility(View.VISIBLE);
            playerView.getSubtitleView().onCues(cues);
        });
        playerView.setPlayer(player);
        playerView.setUseController(false);
        playerView.getSubtitleView().setVisibility(View.VISIBLE);
        /*
        playerView.getVideoSurfaceView().setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.i("KLICK", "KLIIICK");
                controlsLayout.setVisibility(View.VISIBLE);
                controlsVisible = true;
                playPauseButton.requestFocus();
            }
        });
        */
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if( keyCode == KeyEvent.KEYCODE_DPAD_CENTER ) {
            event.startTracking();
            if (clickFlag2) {
                clickFlag = false;
            } else {
                clickFlag = true;
                clickFlag2 = false;
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if (!isVideoControlsOpen()) {
                Log.i("RIGHT", "CLICKED");

                videoSeekFragment = new VideoSeekFragment();
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.controlFragment, videoSeekFragment).commit();
                videoOverlayLayout.setVisibility(View.VISIBLE);
                videoInformationLayout.setVisibility(View.VISIBLE);
            }
        }
        return super.onKeyDown( keyCode, event );
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if( keyCode == KeyEvent.KEYCODE_DPAD_CENTER ) {
            event.startTracking();

            // Short click
            if (clickFlag) {
                if (player.isPlaying()) {
                    videoOverlayLayout.setVisibility(View.VISIBLE);
                    player.pause();
                    pauseIcon.setVisibility(View.VISIBLE);
                    videoInformationLayout.setVisibility(View.VISIBLE);
                } else {
                    videoOverlayLayout.setVisibility(View.INVISIBLE);
                    player.play();
                    pauseIcon.setVisibility(View.INVISIBLE);
                    videoInformationLayout.setVisibility(View.INVISIBLE);
                }
            }
            clickFlag = true;
            clickFlag2 = false;

            return true;
        }
        return super.onKeyUp( keyCode, event );
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        Log.i("LONG", "PRESS");
        if( keyCode == KeyEvent.KEYCODE_DPAD_CENTER ) {
            clickFlag = false;
            clickFlag2 = true;
            //Handle what you want in long press.
            if (fragmentManager.findFragmentById(R.id.controlFragment) == null) {
                Log.i("INNE", "NUUUUUUUUU");
                videoControlFragment = new VideoControlFragment(this);
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.controlFragment, videoControlFragment).commit();
                videoOverlayLayout.setVisibility(View.VISIBLE);
                videoInformationLayout.setVisibility(View.VISIBLE);
            }
            return true;
        }
        return super.onKeyLongPress( keyCode, event );
    }

    private boolean isVideoControlsOpen() {
        return fragmentManager.findFragmentById(R.id.controlFragment) != null;
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

    private void getAudioStreams() {
        new Thread(() -> {
            JSONArray result = apiClient.getAudio(selectedContent.getId());
            try {
                List<ControlSetting> audioStreams = new ArrayList<>();
                for (int i = 0; i < result.length(); i++) {
                    JSONObject audioObject = result.getJSONObject(i);
                    audioStreams.add(
                            new AudioSetting(
                                    audioObject.getString("longName"),
                                    audioObject.getInt("stream_index")
                            )
                    );
                }

                selectedContent.setAvailableAudioStreams(audioStreams);
            } catch (Exception e) {
                e.printStackTrace();;
            }
        }).start();
    }

    private void getSubtitles() {
        new Thread(() -> {
            JSONObject result = apiClient.getSubtitles(selectedContent.getId());
            try {
                JSONArray receivedSubtitles = result.getJSONArray("subtitles");
                List<ControlSetting> subtitles = new ArrayList<>();
                for (int i = 0; i < receivedSubtitles.length(); i++) {
                    JSONObject subtitleObject = receivedSubtitles.getJSONObject(i);
                    subtitles.add(
                            new SubtitleSetting(
                                    subtitleObject.getString("language"),
                                    subtitleObject.getInt("id")
                            )
                    );
                }

                selectedContent.setAvailableSubtitles(subtitles);

                // Start video
                timeAtSeek = continueWatching ? selectedContent.getWatchTime() : 0;
                // Seek starts the video
                runOnUiThread(() -> {
                    seek(timeAtSeek);
                });
            } catch (Exception e) {
                e.printStackTrace();;
            }
        }).start();
    }

    private void getResolutionAndStartVideo() {
        new Thread(() -> {
            JSONObject result = apiClient.getResolution(selectedContent.getId());
            try {
                boolean directPlay = result.getBoolean("directplay");
                JSONArray received_resolutions = result.getJSONArray("resolutions");
                List<ControlSetting> resolutions = new ArrayList<>();
                if (directPlay) {
                    resolutions.add(new ResolutionSetting("directplay"));
                }
                for (int i = 0; i < received_resolutions.length(); i++) {
                    resolutions.add(new ResolutionSetting(received_resolutions.get(i).toString()));
                }
                currentResolution = (ResolutionSetting) resolutions.get(0);
                selectedContent.setAvailableResolutions(resolutions);

                getSubtitles();
            } catch (Exception e) {
                e.printStackTrace();;
            }
        }).start();
    }

    public void openResolutions() {
        if (settingFragment == null) {
            settingFragment = new VideoControlListInterfaceFragment(selectedContent.getAvailableResolutions(), SelectedSetting.RESOLUTION, this);
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.contentFragment, settingFragment).commit();
            findViewById(R.id.contentFragment).requestFocus();
        }
    }

    public void openAudioStreams() {
        if (settingFragment == null) {
            settingFragment = new VideoControlListInterfaceFragment(selectedContent.getAvailableAudioStreams(), SelectedSetting.AUDIO, this);
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.contentFragment, settingFragment).commit();
            findViewById(R.id.contentFragment).requestFocus();
        }
    }

    public void openSubtitles() {
        if (settingFragment == null) {
            settingFragment = new VideoControlListInterfaceFragment(selectedContent.getAvailableSubtitles(), SelectedSetting.SUBTITLE, this);
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.contentFragment, settingFragment).commit();
            findViewById(R.id.contentFragment).requestFocus();
        }
    }

    private void setNextEpisode() {
        Thread thread = new Thread(() -> {
            nextEpisode = ((ShowAPIClient) apiClient).getNextEpisode((Episode) selectedContent);
            foundNextEpisode = nextEpisode != null;
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
        //MainFragment.removeEpisodeFromOngoing((Episode)selectedContent);
        selectedContent = nextEpisode;

        hideNextEpisodeBox();
        setDuration();
        setNextEpisode();
        timeAtSeek = 0;
        // Seek starts the video and since we changes selectedContent it will start the new episode
        seek(timeAtSeek);
    }

    private void setSelectedSubtitle(SubtitleSetting subtitleSetting) {
        DefaultTrackSelector.Parameters parameters = trackSelector.getParameters();
        DefaultTrackSelector.ParametersBuilder builder = parameters.buildUpon();
        int rendererIndexToUse = -1;
        int groupIndexToUse = -1;

        Log.i("Looking for: ", String.format("Id: %d, language: %s", subtitleSetting.getId(), subtitleSetting.getValue()));

        int renderers = trackSelector.getCurrentMappedTrackInfo().getRendererCount();
        Log.i("Num of renderers: ", String.format("%d", renderers));
        for (int renderer = 0; renderer < renderers; renderer++) {
            TrackGroupArray trackGroups = trackSelector.getCurrentMappedTrackInfo().getTrackGroups(renderer);
            Log.i("Num of groups: ", String.format(" %s", trackGroups.length));
            for (int group = 0; group < trackGroups.length; group++) {
                String format = trackGroups.get(group).getFormat(0).sampleMimeType;
                String lang = trackGroups.get(group).getFormat(0).language;
                String label = trackGroups.get(group).getFormat(0).label;
                String id = trackGroups.get(group).getFormat(0).id;

                Log.i("TRACKS: ", String.format("Format: %s lang: %s label: %s id: %s", format, lang, label, id));
                if (format.contains("text/vtt") && subtitleSetting.getId() == Integer.parseInt(label)) {
                    Log.i("FOUND SUBTITLE ID: ", String.format("Id: %d", subtitleSetting.getId()));
                    groupIndexToUse = group;
                    break;
                }
            }
            if (groupIndexToUse != -1) {
                rendererIndexToUse = renderer;
                break;
            }
        }

        int[] tracks = {0};
        DefaultTrackSelector.SelectionOverride override = new DefaultTrackSelector.SelectionOverride(groupIndexToUse, 0);
        builder.setSelectionOverride(rendererIndexToUse, trackSelector.getCurrentMappedTrackInfo().getTrackGroups(rendererIndexToUse), override);
        trackSelector.setParameters(builder.build());
    }


    @Override
    public void settingSelected(ControlSetting setting, SelectedSetting selectedSetting) {
        int playedInSeconds = Math.toIntExact(timeAtSeek + player.getCurrentPosition() / 1000);
        if (selectedSetting.equals(SelectedSetting.RESOLUTION)) {
            currentResolution = (ResolutionSetting) setting;
            Log.i("Resolution", "Resolution changed to " + currentResolution.getValue());
            seek(playedInSeconds);
        } else if (selectedSetting.equals(SelectedSetting.AUDIO)) {
            currentAudioStream = (AudioSetting) setting;
            Log.i("Audio", "Audio changed to " + currentAudioStream.getValue());
            seek(playedInSeconds);
        } else if (selectedSetting.equals(SelectedSetting.SUBTITLE)) {
            setSelectedSubtitle((SubtitleSetting) setting);
        }

    }

    @Override
    public void openSetting(SelectedSetting selectedSetting) {
        if (selectedSetting.equals(SelectedSetting.RESOLUTION)) {
            openResolutions();
        } else if (selectedSetting.equals(SelectedSetting.AUDIO)) {
            openAudioStreams();
        } else if (selectedSetting.equals(SelectedSetting.SUBTITLE)) {
            openSubtitles();
        }
    }

}