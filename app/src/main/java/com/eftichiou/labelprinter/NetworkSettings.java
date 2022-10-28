package com.eftichiou.labelprinter;

import android.app.Activity;
import android.content.Context;
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


public class NetworkSettings extends Activity {

    SharedPreferences sp;
    private TextView responseTextView;
    private RequestQueue mQueue;
    private LoadingDialog loadingDialog;
    private Button testConnectionBtn;
    private Button loadDataBtn;
    private Button saveBtn;
    private EditText ip;
    private EditText port;
    private Tools tools;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.network_settings);

        tools  = new Tools(this);
        testConnectionBtn = findViewById(R.id.test_connection);
        loadDataBtn = findViewById(R.id.load_data);
        saveBtn = findViewById(R.id.save_ip_port);
        ip = findViewById(R.id.server_ip);
        port = findViewById(R.id.server_port);
        responseTextView = findViewById(R.id.response);
        mQueue = Volley.newRequestQueue(this);
        loadingDialog = new LoadingDialog(NetworkSettings.this);
        sp = getSharedPreferences("LabelPrinter", Context.MODE_PRIVATE);

        SharedPreferences mySp = getApplicationContext().getSharedPreferences("LabelPrinter", Context.MODE_PRIVATE);
        String retrievedIp = mySp.getString("myIp", "");
        String retrievedPort = mySp.getString("myPort", "");
        ip.setText(retrievedIp);
        port.setText((retrievedPort));

        testConnectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testConnection(ip.getText().toString(), port.getText().toString());
            }
        });



        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String myIp = ip.getText().toString();
                String myPort = port.getText().toString();
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("myIp", myIp);
                editor.putString("myPort", myPort);
                editor.commit();
                displayMessage("Server Saved", false);

            }
        });


        loadDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String myIp = ip.getText().toString();
                String myPort = port.getText().toString();
                loadData(myIp, myPort);
            }
        });

    }

    private void testConnection(String ip, String port) {
        String url = "http://" + ip + ":" + port + "/test_network_connection";
        loadingDialog.startLoadingDialog();
        tools.playSound();
        try {
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    displayMessage("Connection Success", false);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    displayMessage("Connection Failed", true);
                    error.printStackTrace();
                }
            });
            mQueue.add(request);
        } catch (Exception e) {
            displayMessage("Something Went Wrong", true);
        }
    }

    private void loadData(String ip, String port) {
        String url = "http://" + ip + ":" + port + "/get_data";
        loadingDialog.startLoadingDialog();
        tools.playSound();
        try {
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String myJson = response.toString();
                        writeToFile("data.json", myJson);
                        displayMessage("Data Stored", false);
                    } catch (Exception e) {
                        displayMessage("Something Went Wrong", true);
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    displayMessage("Something Went Wrong", true);
                    error.printStackTrace();
                }
            });
            mQueue.add(request);
        } catch (Exception e) {
            displayMessage("Something Went Wrong", true);
            e.printStackTrace();
        }
    }

    private void writeToFile(String fileName, String content) {
        FileOutputStream fileOutputStream = null;
        try {
            File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(folder, fileName);
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(content.getBytes());
            Toast.makeText(this, "Done" + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "File was not saved!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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


