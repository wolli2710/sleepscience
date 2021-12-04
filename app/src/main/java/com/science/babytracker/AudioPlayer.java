package com.science.babytracker;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.widget.Toast;

public class AudioPlayer {

    private MediaPlayer mediaPlayer = null;

    public void startLoop(Context ctx, int group ){

        if (mediaPlayer == null) {
            init(ctx, group);
        }
        //Toast.makeText(ctx, "Playing sound",Toast.LENGTH_SHORT).show();
        mediaPlayer.start();
    }

    public void stopLoop(){
        mediaPlayer.stop();
    }

    private void init(Context ctx, int group ) {
        mediaPlayer = MediaPlayer.create(ctx, group);
        AudioManager audio = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);

        double maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        Double percent = 1.0d;
        double twintyVolume = (maxVolume * percent);

        audio.setStreamVolume(AudioManager.STREAM_MUSIC,(int)twintyVolume,0);
    }

    public boolean isPlaying(Context ctx, int group ){
        if (mediaPlayer == null) {
            init(ctx, group);
        }
        return mediaPlayer.isPlaying();
    }
}
