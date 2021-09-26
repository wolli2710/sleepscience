package com.science.babytracker;

import android.content.Context;
import android.media.MediaPlayer;
import android.widget.Toast;

public class AudioPlayer {

    private MediaPlayer mediaPlayer = null;

    public void startLoop(Context ctx, int group ){

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(ctx, group);
        }
        //Toast.makeText(ctx, "Playing sound",Toast.LENGTH_SHORT).show();
        mediaPlayer.start();
    }

    public void stopLoop(){
        mediaPlayer.stop();
    }

    public boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }
}
