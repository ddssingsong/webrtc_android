package com.dds.temple;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ConnectActivity extends AppCompatActivity {


    public static void launchActivity(Activity activity) {
        Intent intent = new Intent(activity, ConnectActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect2);
    }
}