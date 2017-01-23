package com.kartikgupta.myapplication;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

/**
 * Created by kartik on 15/1/17.
 */

class CameraPreview extends SurfaceView implements SurfaceHolder.Callback , Camera.PreviewCallback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private String TAG = CameraPreview.class.getSimpleName();

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG,"entering: surfaceCreated");
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
        //mCamera.release();
        //TODO:release the camera properly
        Log.d(TAG,"entering: surfaceDestroyed");
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        Log.d(TAG,"entering: surfaceChanged");
        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes

       /*
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

    */

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewCallback(this);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        Log.d(TAG,"entering the onPreviewFrameMehtod");
        //this byte[] array is basically frames which I need to send in realtime to server for recognition
        if(data!=null){ //without this conditional check , sometimes there is null data and app crashes probably because of it
            sendFrame(data);
        }

    }

    private void sendFrame(byte[] frame) {
        Log.d(TAG,"entering: sendFrame");
        OkHttpClient client = new OkHttpClient();
        //RequestBody requestBody = RequestBody.create(MEDI)



        Request request = new Request.Builder()
                .url("http://10.20.0.236/BTP/myresponse.php")
                .post( RequestBody.create(MediaType.parse("application/octet-stream"),frame)  )
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG,"Failure in sending frame");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG,"Success in sending frame"+"\n data recived is \n "+response.toString());
            }
        });
        //TODO: implement the volley here
        //implement volley here

    }

    public void pause() {
        mCamera.release();
    }

    public void resume(){
        try {
            mCamera.reconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}