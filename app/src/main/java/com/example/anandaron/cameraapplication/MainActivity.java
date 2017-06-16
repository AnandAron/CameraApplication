package com.example.anandaron.cameraapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.input.image.ClarifaiImage;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;
import okhttp3.OkHttpClient;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;


public class MainActivity extends AppCompatActivity implements LocationListener {
    private static String imgPath;
    public static String timeStamp;
    private Camera mCamera;
    private CameraPreview mPreview;
    public static Uri file;
    public static String downloadUrl;
    public static String output;
    public static Context c;
    SharedPreferences p;
    boolean isClicked;
    private StorageReference mStorageRef;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = (float) 30.0;
    Location location;

    LocationManager locationManager;
    boolean isGPSEnabled;
    boolean isNetworkEnabled;
    boolean canGetLocation;
    static public double lat;
    static public double lng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        c=this;
        p = PreferenceManager.getDefaultSharedPreferences(this);
        isClicked = p.getBoolean("clicked", false);

        if(isClicked)
        {
            TextView tv =(TextView) findViewById(R.id.test);
            tv.setText("Address:"+p.getString("Address","Location Unavailable")+"\n"+
                        "Tags: "+p.getString("Tag1","Tag Unavailable")+" , "+p.getString("Tag2","Tag Unavailable")+" , "+p.getString("Tag3","Tag Unavailable")+" , "+p.getString("Tag4","Tag Unavailable"));
            tv.setVisibility(View.VISIBLE);
            p.edit().putBoolean("clicked",false).apply();
            Intent i = new Intent(this,DisplayOutput.class);
            startActivity(i);
        }
        if(checkCameraHardware(getApplicationContext()))
        {
            mCamera = getCameraInstance();


            // Create our Preview view and set it as the content of our activity.
           mPreview = new CameraPreview(this, mCamera);
            final FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
           preview.addView(mPreview);

            preview.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                                public void onAutoFocus(boolean success, Camera camera) {
                                    if(success){


                                        camera.takePicture(null,null,mPicture);
                                        Location l = getLocation();
                                        p.edit().putFloat("lat",(float) l.getLatitude()).apply();
                                        p.edit().putFloat("lng",(float) l.getLongitude()).apply();
                                        new locationTask().execute();

                                        Intent i = new Intent(getBaseContext(),ProgressActivity.class);  //This (Intent)Activity is useless
                                        p.edit().putBoolean("clicked",true).apply();                  //Which displays just a progress
                                        startActivity(i);                                             //Bar, I used it to handle the
                                                                                                      //Handle the crash caused by the clarifai
                                    }
                                }
                            });
                        }
                    }
            );

        }
        else{
            Toast.makeText(getApplicationContext(),"Camera Not Found",Toast.LENGTH_LONG);
        }
    }



    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {

            return true;
        } else {

            return false;
        }
    }
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open();

        }
        catch (Exception e){
            Log.e("CameException",e.toString());
        }
        return c; // returns null if camera is unavailable
    }
    private static File getOutputMediaFile(int type){

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "MyCameraApp");

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
         timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            imgPath=mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg";

            SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(MainActivity.c);
            p.edit().putString("ts","IMG_"+timeStamp).apply();
            mediaFile = new File(imgPath);
        } else {
            return null;
        }

        return mediaFile;
    }
    public Location getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2, MIN_DISTANCE_CHANGE_FOR_UPDATES,this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.

                        }
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            lat = location.getLatitude();
                            lng = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 50000, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                lat = location.getLatitude();
                                lng = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
return location;

    }

    //Camer call back
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile =getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d("", "Error creating media file, check storage permissions: ");
                return;
            }

            try {

                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d("", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("", "Error accessing file: " + e.getMessage());
            }
            mStorageRef = FirebaseStorage.getInstance().getReference();

            file = Uri.fromFile(pictureFile);
            StorageReference riversRef = mStorageRef.child("images/IMG_"+timeStamp+".jpg");

            riversRef.putFile(file)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            downloadUrl = taskSnapshot.getDownloadUrl().toString();
                             new queryTask().execute();


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            // ...
                        }
                    });


        }
    };

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
// API query to Clarifai
class queryTask extends AsyncTask<URL,Void ,String> {
    public static List<ClarifaiOutput<Concept>> predictionResults;
    private ClarifaiClient client;

    @ NonNull
    @Override

    protected String doInBackground(URL... urls) {
       try{


           client = new ClarifaiBuilder("Z-xfSJQXxAQqkW0cnkB7ArMbIBpktI-zMvHdxUSX","CcFeCjFlh2_BR7vEIn5Bl8G55RSMQDTPQih2NSGM").client(new OkHttpClient()).buildSync();
           Log.d("doInBack","clientCreated");
           SharedPreferences p= PreferenceManager.getDefaultSharedPreferences(MainActivity.c);
           p.edit().putString("Tag1","Unavailable,Please Retry!!").apply();
           p.edit().putString("Tag2","Unavailable,Please Retry!!").apply();
           p.edit().putString("Tag3","Unavailable,Please Retry!!").apply();
           p.edit().putString("Tag4","Unavailable,Please Retry!!").apply();
           Log.d("doInBack","TagsInitiated");
            predictionResults =
                   client.getDefaultModels().generalModel() // You can also do Clarifai.getModelByID("id") to get custom models
                           .predict()
                           .withInputs(
                                   ClarifaiInput.forImage(ClarifaiImage.of((MainActivity.downloadUrl))
                                           ))
                           .executeSync() // optionally, pass a ClarifaiClient parameter to override the default client instance with another one
                           .get();
           Log.d("yeaaay","Results loaded");
       }
       catch (Exception e){
           Log.e("Error at API response",e.toString());
       }

        //The first four tags were loaded into the preferences
        SharedPreferences p= PreferenceManager.getDefaultSharedPreferences(MainActivity.c);
        p.edit().putString("Tag1",predictionResults.get(0).data().get(0).name()).apply();
        p.edit().putString("Tag2",predictionResults.get(0).data().get(1).name()).apply();
        p.edit().putString("Tag3",predictionResults.get(0).data().get(2).name()).apply();
        p.edit().putString("Tag4",predictionResults.get(0).data().get(3).name()).apply();


        return null;
    }

    @Override
    protected void onPostExecute(String s) {

    }


}
//Location to Address resolver
class locationTask extends AsyncTask<URL,Void ,String> {
    public static String fnialAddress;
    SharedPreferences p;
    @Override
    protected String doInBackground(URL... urls) {
        Geocoder geoCoder = new Geocoder(MainActivity.c, Locale.getDefault());
        StringBuilder builder = new StringBuilder();
        try {

            p = PreferenceManager.getDefaultSharedPreferences(MainActivity.c);
            List<Address> address = geoCoder.getFromLocation(p.getFloat("lat",0 ), p.getFloat("lng",0 ), 1);
            int maxLines = address.get(0).getMaxAddressLineIndex();
            for (int i=0; i<maxLines; i++) {
                String addressStr = address.get(0).getAddressLine(i);
                builder.append(addressStr);
                builder.append(" ");
            }

            fnialAddress = builder.toString(); //This is the complete address.

        } catch (IOException e) {}
        p.edit().putString("Address",fnialAddress).apply();




        return fnialAddress;
    }
}