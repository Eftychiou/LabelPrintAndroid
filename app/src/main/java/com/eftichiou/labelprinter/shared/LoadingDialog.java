package com.eftichiou.labelprinter.shared;

import android.app.Activity;
import android.app.AlertDialog;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.bixolon.labelprinter.R;

public class LoadingDialog {

    private Activity activity;
    private AlertDialog dialog;



    public LoadingDialog(Activity myActivity){
        activity = myActivity;
    }

    public void startLoadingDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater= activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.custom_dialog,null));
        builder.setCancelable(false);
        dialog = builder.create();
        dialog.show();

    }
    public void dismissDialog(){
        if(dialog != null){
            dialog.dismiss();
        }

    }


}
