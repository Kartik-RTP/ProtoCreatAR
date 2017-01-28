package com.kartikgupta.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import static android.support.v7.appcompat.R.id.info;

public class MagicActivity extends AppCompatActivity {
    private final String TAG = MagicActivity.class.getSimpleName();

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"entering : onResume");

        mCameraPreview.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"entering : onPause");
        mCameraPreview.pause();
    }

    Camera mCamera;
    CameraPreview mCameraPreview;

    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0

                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MagicActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);


     /*   ActivityCompat.requestPermissions(MagicActivity.this,
                                          new String[]{Manifest.permission.CAMERA},
                                          1);
*/

        mCamera = getRearCameraInstance();

        //this will start camera preview
        //start sending frames continously to the server
        //on reciveing , draw that object using opengl

        mCameraPreview = new CameraPreview(this,mCamera);
        FrameLayout previewFrameLayout = (FrameLayout) findViewById(R.id.camera_preview);
        previewFrameLayout.addView(mCameraPreview);

    }

    public static Camera getRearCameraInstance(){
        Camera c = null;
        Camera.CameraInfo info = new Camera.CameraInfo();
        try {
            // Try to find a front-facing camera (e.g. for videoconferencing).
            int numCameras = Camera.getNumberOfCameras();
            for (int i = 0; i < numCameras; i++) {
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) { //ensures that rear camera is opened
                    c = Camera.open(i);
                    break;
                }
            }
        }
        catch (Exception e){
            //TODO:write a log statement here
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }
}
