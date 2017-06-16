package com.example.anandaron.cameraapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DisplayOutput extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_output);
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(MainActivity.c);
        EditText loc = (EditText) findViewById(R.id.location);
        loc.setText(p.getString("Address","Location Unavailable, pls retry!!"));
        EditText tag1 = (EditText) findViewById(R.id.tag1);
        tag1.setText(p.getString("Tag1","Tag Unavailable, pls retry!!"));
        EditText tag2 = (EditText) findViewById(R.id.tag2);
        tag2.setText(p.getString("Tag2","Tag Unavailable, pls retry!!"));
        EditText tag3 = (EditText) findViewById(R.id.tag3);
        tag3.setText(p.getString("Tag3","Tag Unavailable, pls retry!!"));
        EditText tag4 = (EditText) findViewById(R.id.tag4);
        tag4.setText(p.getString("Tag4","Tag Unavailable, pls retry!!"));

    }

    public void save(View view) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Images");
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
        myRef.child(p.getString("ts"+"_","null")).child("Location").setValue(p.getString("Address","Location Unavailable"));
        myRef.child(p.getString("ts"+"_","null")).child("URL").setValue(MainActivity.downloadUrl);
        myRef.child(p.getString("ts"+"_","null")).child("Tags").setValue(p.getString("Tag1","Tag Unavailable")+" , "
                +p.getString("Tag2","Tag Unavailable")+" , "
                +p.getString("Tag3","Tag Unavailable")+" , "
                +p.getString("Tag4","Tag Unavailable"));
        Toast.makeText(this,"Data will be updated!",Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this,FirstPage.class);
        startActivity(i);
    }

    public void retry(View view) {
        /*Retry from the beginning*/
        Intent i = new Intent(this,MainActivity.class);
        startActivity(i);
    }
}
