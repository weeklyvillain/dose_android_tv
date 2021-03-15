package com.dose.dose;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.StyledPlayerView;


public class VideoActivity extends Activity {
    //private static VideoView videoview;
    private SimpleExoPlayer player;
    private MediaItem mediaItem;
    private TextView currentTime;
    private int timeAtSeek = 0;
    private Handler currentTimeHandler = new Handler();

    private Runnable currentTimeUpdater = new Runnable() {
        @Override
        public void run() {
            int playedInSeconds = Math.toIntExact(timeAtSeek + player.getCurrentPosition() / 1000);
            int hours = playedInSeconds / 60 / 60;
            int minutes = (playedInSeconds / 60) % 60;
            int seconds = playedInSeconds % 60;
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

        final SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                player.stop();
                mediaItem = MediaItem.fromUri("https://vnc.fgbox.appboxes.co/doseserver/api/video/280?type=movie&token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6IlZlemVsIiwidXNlcl9pZCI6IjEiLCJpYXQiOjE2MTU4MjMzMjIsImV4cCI6MTYxNjEyMzMyMn0.8MbofgLdiB6IEbJkJ3ej3ef0lb5ad2zjqeWtqUZAm8o&start=1000&quality=1080P");
                player.setMediaItem(mediaItem);
                player.prepare();
                player.play();
            }
        });
        //playVideo();

        player = new SimpleExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        playerView.setUseController(false);

        mediaItem = MediaItem.fromUri("https://vnc.fgbox.appboxes.co/doseserver/api/video/280?type=movie&token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6IlZlemVsIiwidXNlcl9pZCI6IjEiLCJpYXQiOjE2MTU4MjMzMjIsImV4cCI6MTYxNjEyMzMyMn0.8MbofgLdiB6IEbJkJ3ej3ef0lb5ad2zjqeWtqUZAm8o&start=0&quality=1080P");
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
        currentTimeHandler.post(currentTimeUpdater);



    }




}