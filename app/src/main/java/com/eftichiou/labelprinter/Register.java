package com.eftichiou.labelprinter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bixolon.labelprinter.R;
import com.eftichiou.labelprinter.shared.LoadingDialog;
import com.eftichiou.labelprinter.shared.Tools;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class Register extends Activity {
    SharedPreferences sp;
    private LoadingDialog loadingDialog;
    private Tools tools;
    private TextView responseTextView;
    private RequestQueue mQueue;
    private Button registerBtn;
    private EditText theKey;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        loadingDialog = new LoadingDialog(Register.this);
        tools = new Tools(this);
        responseTextView = findViewById(R.id.response);
        mQueue = Volley.newRequestQueue(this);
        registerBtn = findViewById(R.id.registerBtn);
        theKey = findViewById(R.id.key);
        sp = getSharedPreferences("LabelPrinter", Context.MODE_PRIVATE);
        SharedPreferences mySp = getApplicationContext().getSharedPreferences("LabelPrinter", Context.MODE_PRIVATE);


        String retrievedLicense = mySp.getString("myLicense", "");
        if(retrievedLicense != ""){
            startActivity(new Intent(Register.this, MainActivity.class));
            finish();
        }

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });


    }

    private void register() {

        final String key = theKey.getText().toString();
        String url = "https://api.nextjs-shop.com/cypos/label-register?key="+key;
        loadingDialog.startLoadingDialog();
        tools.playSound();
        try {
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    String myJson = response.toString();
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("myLicense", key);
                    editor.commit();
                    Intent intent = new Intent(Register.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if(error.networkResponse.statusCode == 400){
                        displayMessage("No key found", true);
                    }else if(error.networkResponse.statusCode == 401){
                        displayMessage("Invalid Key", true);
                    }else if(error.networkResponse.statusCode == 402){
                        displayMessage("Key has been used",true);
                    }
                    error.printStackTrace();
                }
            });
            mQueue.add(request);
        } catch (Exception e) {
            displayMessage("Something Went Wrong", true);
        }
    }

    private void displayMessage(String message, Boolean isError) {
        tools.playSound();
        if (isError) {
            responseTextView.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));

        } else {
            responseTextView.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        }
        responseTextView.setText(message);
        loadingDialog.dismissDialog();
    }


}


