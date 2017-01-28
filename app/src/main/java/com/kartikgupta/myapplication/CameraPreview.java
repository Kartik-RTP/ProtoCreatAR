package com.kartikgupta.myapplication;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.WebSocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.R.attr.height;
import static android.R.attr.width;
import static android.content.ContentValues.TAG;

/**
 * Created by kartik on 15/1/17.
 */

class CameraPreview extends SurfaceView implements SurfaceHolder.Callback , Camera.PreviewCallback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private String TAG = CameraPreview.class.getSimpleName();

    private int mCounter;


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
        mCounter=1;
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
        mCamera.release();
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

        if(true) {
            if (data != null) { //without this conditional check , sometimes there is null data and app crashes probably because of it

                sendFrame(data);
            }
            mCounter++;
        }

    }


    private void sendFrame(byte[] frame) {
        sendFrameUsingSocket(frame);
    }

    private void sendFrameUsingSocket(final byte[] frame) {
        final byte[] temp=frame;

        AsyncHttpClient.getDefaultInstance().websocket("ws://10.20.1.25:8080", null, new AsyncHttpClient.WebSocketConnectCallback() {
            @Override
            public void onCompleted(Exception ex, WebSocket webSocket) {
                if (ex != null) {
                    ex.printStackTrace();
                    return;
                }
                webSocket.send("a string");
                webSocket.send(new byte[20]);
               // webSocket.send(new String(temp));

                webSocket.setStringCallback(new WebSocket.StringCallback() {
                    public void onStringAvailable(String s) {
                        System.out.println("I got a string: " + s);
                    }
                });
                webSocket.setDataCallback(new DataCallback() {
                    public void onDataAvailable(DataEmitter emitter, ByteBufferList byteBufferList) {
                        System.out.println("I got some bytes!");
                        // note that this data has been read
                        byteBufferList.recycle();
                    }
                });
            }
        });
    }



    private void sendFrameUsingHTTP(byte[] frame) {
      /*This method is not being used now ..it was used earlier for testing purposes*/

        Log.d(TAG,"entering: sendFrame");
        OkHttpClient client = new OkHttpClient();
      //  RequestBody requestBody = new FormBody.Builder().add("frame",encodedImage).build();


        Request request = new Request.Builder()
                .url("http://10.20.2.28/creatar/image.php")
                //.post(requestBody)
                //.method("POST",RequestBody.create(null,frame))
                    .post(RequestBody.create(MediaType.parse("TEXT_PLAIN_TYPE"),frame))
//                .post( RequestBody.create(null,frame)  )
                .build();
        Log.d(TAG,"sending the following data \n"+frame.length);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG,"Failure in sending frame");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                Log.d(TAG,"Success in sending frame"+"\n data recived is \n "+response.body().string());
            }
        });

    }

    public void pause() {
            //this method does nothing..maybe useful for dealing with camera release and so on
    }

    public void resume(){
        try {
            mCamera.reconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}