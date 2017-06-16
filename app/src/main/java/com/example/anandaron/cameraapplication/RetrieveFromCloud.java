package com.example.anandaron.cameraapplication;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class RetrieveFromCloud extends AppCompatActivity {
private static int n;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.retrive_from_cloud);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("Images");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                n = (int)dataSnapshot.getChildrenCount();

                final Iterator<DataSnapshot> children = dataSnapshot.getChildren().iterator();



                while(children.hasNext()){

                    final String imgName = children.next().getKey().toString();
                    ImageInfo imgInfo = dataSnapshot.getValue(ImageInfo.class);
                    final String location = imgInfo.Location;
                    final String tags  = imgInfo.Tags;
                    final StorageReference img = FirebaseStorage.getInstance().getReference().child("Images").child(imgName);
                    img.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(final Uri uri) {
                            // Got the download URL for 'users/me/profile.png'
                            // Pass it to Picasso to download, show in ImageView and caching
                            ImageView imageView = new ImageView(getBaseContext());
                            imageView.getLayoutParams().height=20;
                            imageView.getLayoutParams().width=20;
                            Picasso.with(getBaseContext()).load(uri.toString()).into(imageView);
                            LinearLayout horizontalLayout = new LinearLayout(getBaseContext());
                            horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
                            horizontalLayout.getLayoutParams().height= ViewGroup.LayoutParams.WRAP_CONTENT;
                            horizontalLayout.addView(imageView);
                            LinearLayout verticalLayout = new LinearLayout(getBaseContext());
                            TextView tv1 = new TextView(getBaseContext());
                            TextView tv2 = new TextView(getBaseContext());
                            TextView tv3 = new TextView(getBaseContext());
                            tv1.setText(imgName);
                            tv1.setTextSize(10);
                            tv2.setText(location);
                            tv3.setText(tags);
                            verticalLayout.addView(tv1);
                            verticalLayout.addView(tv2);
                            verticalLayout.addView(tv3);
                            LinearLayout fnlAdapter = (LinearLayout) findViewById(R.id.fnl_adapter);
                            fnlAdapter.addView(horizontalLayout);
                            horizontalLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent i = new Intent(getBaseContext(),ImageFullScreenView.class);
                                    Bundle bundle = new Bundle();

                                    bundle.putString("Name",imgName);
                                    bundle.putString("Location",location);
                                    bundle.putString("Tags",tags);
                                    i.putExtras(bundle);
                                    startActivity(i);
                                }
                            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
