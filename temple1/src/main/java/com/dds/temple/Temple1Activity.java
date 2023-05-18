package com.dds.temple;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class Temple1Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temple1);
    }

    // client start
    public void client(View view) {
        ConnectActivity.launchActivity(this);
    }

    // server start
    public void server(View view) {
        ConnectActivity.launchActivity(this);
    }
}