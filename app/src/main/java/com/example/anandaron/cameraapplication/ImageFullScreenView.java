package com.example.anandaron.cameraapplication;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.net.URL;

public class ImageFullScreenView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_full_screen_view);
        Bundle bundle = getIntent().getExtras();

        String location = bundle.getString("Location");

        String tags = bundle.getString("Tags");
        String name=bundle.getString("Name");

        TextView tv1= (TextView) findViewById(R.id.name);
        TextView tv2= (TextView) findViewById(R.id.location);
        TextView tv3= (TextView) findViewById(R.id.tags);
        tv1.setText(name);
        tv2.setText(location);
        tv3.setText(tags);

        final ImageView imageView = (ImageView) findViewById(R.id.imageView);
        final StorageReference img = FirebaseStorage.getInstance().getReference().child("Images").child(name);
        img.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(getBaseContext()).load(uri.toString()).into(imageView);
            }
        });


    }
}
