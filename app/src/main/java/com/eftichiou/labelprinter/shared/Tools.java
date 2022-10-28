package com.eftichiou.labelprinter.shared;

import android.app.Activity;
import android.app.AlertDialog;
import android.media.MediaPlayer;

import com.bixolon.labelprinter.R;

public class Tools {

    private Activity activity;
    MediaPlayer player;

    public Tools(Activity myActivity){
        activity = myActivity;
    }


    public void playSound() {
        if(player == null){
            player = MediaPlayer.create(activity, R.raw.beep);
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    if(player != null){
                        player.release();
                        player=null;
                    }
                }
            });
        }
        player.start();
    }
    public void playFailSound() {
        if(player == null){
            player = MediaPlayer.create(activity, R.raw.fail);
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    if(player != null){
                        player.release();
                        player=null;
                    }
                }
            });
        }
        player.start();
    }
}
