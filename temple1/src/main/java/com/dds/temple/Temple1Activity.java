package com.dds.temple;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;

public class Temple1Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temple1);
    }

    // client start
    public void client(View view) {
        EditText editText = findViewById(R.id.editTextText);
        Editable text = editText.getText();
        ConnectActivity.launchActivity(this, ConnectActivity.TYPE_CLIENT, text.toString());
    }

    // server start
    public void server(View view) {
        ConnectActivity.launchActivity(this, ConnectActivity.TYPE_SERVER, "0.0.0.0");
    }
}