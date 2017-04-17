package com.kartikgupta.myapplication.depracatedForNow;

import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

import  com.kartikgupta.myapplication.depracatedForNow.CameraPreview;
import com.kartikgupta.myapplication.R;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
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
