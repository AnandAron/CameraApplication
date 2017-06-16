package com.example.anandaron.cameraapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class FirstPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_page);
    }

    public void takePicture(View view) {
        Intent i = new Intent(this,MainActivity.class);
        startActivity(i);
    }

    public void viewImages(View view) {
        Intent i = new Intent(this,RetrieveFromCloud.class);
        startActivity(i);
    }
}
