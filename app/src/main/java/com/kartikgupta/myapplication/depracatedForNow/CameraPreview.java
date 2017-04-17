package com.kartikgupta.myapplication.depracatedForNow;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.kartikgupta.myapplication.R;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by kartik on 15/1/17.
 */

class CameraPreview extends SurfaceView implements SurfaceHolder.Callback , Camera.PreviewCallback {

    private Future<WebSocket> mWebSocket;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private String TAG = CameraPreview.class.getSimpleName();
    private String mURL;
    private Context mContext;
    Camera.Parameters mParameters;
    int mformat;
    //private Display mDisplay;

    private int mCounter;


    public CameraPreview(Context context, Camera camera) {
        super(context);

        mCamera = camera;

        mContext = context;

        //mDisplay = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        mParameters = camera.getParameters();
        mformat = mParameters.getPreviewFormat();

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mURL = PreferenceManager.getDefaultSharedPreferences(mContext)
                .getString(getResources().getString(R.string.pref_url_key),getResources().getString(R.string.pref_url_default));

        initializeWebSocket();

    }

    private void initializeWebSocket() {
        mWebSocket =  AsyncHttpClient.getDefaultInstance().websocket(/*"ws://10.20.1.25:8080"*/mURL, null, new AsyncHttpClient.WebSocketConnectCallback() {
            @Override
            public void onCompleted(Exception ex, WebSocket webSocket) {
                if (ex != null) {
                    ex.printStackTrace();
                    return;
                }
                webSocket.send("a string");
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

    public void surfaceCreated(SurfaceHolder holder) {
        mCounter=1;

        Log.d(TAG,"entering: surfaceCreated");
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);

            //TODO: Check if the following line is required or not
            mCamera.setDisplayOrientation(90); //This is done to correct the orientation of the surfaceview. Which was otherwise
                                                //coming rotated by an angle of 90 degrees.
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
        mCamera.stopPreview();
        mCamera.setPreviewCallback(null);
        mCamera.release();
        mCamera = null;
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

            //TODO: Check if the following line is required or not
            mCamera.setDisplayOrientation(90); //This is done to correct the orientation of the surfaceview. Which was otherwise
                                                //coming rotated by an angle of 90 degrees.
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

        int width = mParameters.getPreviewSize().width;
        int height = mParameters.getPreviewSize().height;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        //TODO: find and use the height and width variable and remove the hard coded values
        YuvImage yuvImage = new YuvImage(frame, ImageFormat.NV21, width, height, null); //width : 768, height : 1280
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 50, out); //width : 768, height : 1280
        final byte[] imageBytes = out.toByteArray();

        if(mWebSocket==null){initializeWebSocket();}
        WebSocket webtemp = mWebSocket.tryGet();
        if(webtemp!=null){mWebSocket.tryGet().send(imageBytes);
        }


//        Log.d(TAG,"Size of the frame is : " + imageBytes.length);
  //      mWebSocket.send(imageBytes);



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