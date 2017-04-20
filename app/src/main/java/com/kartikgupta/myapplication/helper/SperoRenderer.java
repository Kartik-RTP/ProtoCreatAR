package com.kartikgupta.myapplication.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.opengl.GLES20;
import android.preference.PreferenceManager;
import android.support.annotation.UiThread;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.kartikgupta.myapplication.MagicData;
import com.kartikgupta.myapplication.NewMagicActivity;
import com.kartikgupta.myapplication.R;
import com.kartikgupta.myapplication.helper.shader.SimpleFragmentShader;
import com.kartikgupta.myapplication.helper.shader.SimpleShaderProgram;
import com.kartikgupta.myapplication.helper.shader.SimpleVertexShader;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import org.artoolkit.ar.base.ARToolKit;
import org.artoolkit.ar.base.camera.CameraPreferencesActivity;
import org.artoolkit.ar.base.rendering.gles20.ARRendererGLES20;
import org.artoolkit.ar.base.rendering.gles20.CubeGLES20;
import org.artoolkit.ar.base.rendering.gles20.ShaderProgram;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.content.ContentValues.TAG;

/**
 * Created by kartik on 17/4/17.
 */

public class SperoRenderer extends ARRendererGLES20 {


    private static final int NO_OF_FRAMES_TO_SKIP = 100; //this many frames get skipped before sending a frame to server
    private int markerID = -1;
    private CubeGLES20 cube;

    private final String TAG = SperoRenderer.class.getSimpleName();
    //from arbase lib

    private Context mContext;
    private byte[] mCameraData;
    private int mCameraWidth;
    private int mCameraHeight;
    private static final String CAMERA_PREVIEW_FEED_INTENT = "camera_preview_feed_intent";
    private static final String CAMERA_FEED_DATA = "camera_feed_data";
    private static final String CAMERA_PARAM_HEIGHT = "camera_param_height";
    private static final String CAMERA_PARAM_WIDTH = "camera_param_width";



    private Future<WebSocket> mWebSocket;
    private String mURL;

    BroadcastReceiver mBroadcastRecever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mCameraData=intent.getByteArrayExtra(CAMERA_FEED_DATA);
            mCameraWidth = intent.getIntExtra(CAMERA_PARAM_WIDTH,0);
            mCameraHeight=intent.getIntExtra(CAMERA_PARAM_HEIGHT,0); //note that 0 will mean error eventualylly
            Log.d(TAG,"getting camera feed data");
        }
    };
    private int mCounter=0;

    public SperoRenderer(Context context) {
        mContext = context;
    }

    /**
     * This method gets called from the framework to setup the ARScene.
     * So this is the best spot to configure you assets for your AR app.
     * For example register used markers in here.
     */
    @Override
    public boolean configureARScene() {
        markerID = ARToolKit.getInstance().addMarker("single;Data/hiro.patt;80");
        markerID = ARToolKit.getInstance().addMarker("nft;DataNFT/pinball");


        if (markerID < 0) return false;

        return true;
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int w, int h) {
        super.onSurfaceChanged(unused, w, h);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mBroadcastRecever,
                new IntentFilter(CAMERA_PREVIEW_FEED_INTENT));
    }

    //Shader calls should be within a GL thread that is onSurfaceChanged(), onSurfaceCreated() or onDrawFrame()
    //As the cube instantiates the shader during setShaderProgram call we need to create the cube here.
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        super.onSurfaceCreated(unused, config);

        ShaderProgram shaderProgram = new SimpleShaderProgram(new SimpleVertexShader(), new SimpleFragmentShader());
        cube = new CubeGLES20(40.0f, 0.0f, 0.0f, 20.0f);
        cube.setShaderProgram(shaderProgram);

        mURL = PreferenceManager.getDefaultSharedPreferences(mContext)
                .getString(mContext.getResources().getString(R.string.pref_url_key),mContext.getResources().getString(R.string.pref_url_default));

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
                        try {
                            MagicData magicData  = MagicData.ADAPTER.decode(byteBufferList.getAllByteArray());
                            processMagicData(magicData);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d(TAG,"unable to decode messageMagicData");
                        }
                        // note that this data has been read
                        byteBufferList.recycle();
                    }
                });
            }
        });
    }

    private void processMagicData(MagicData magicData) {
        //need to copy iset , fset and fset3 files to assets/DataNFT foler

        Log.d(TAG,"Recieved some magic ");
        copyMarkerFilesToAsset(magicData.marker);

        //Toast.makeText(mContext,"REcieved some magic :"+m.fset.toString(),Toast.LENGTH_LONG);
    }

    private void copyMarkerFilesToAsset(MagicData.Marker marker) {

    }

    private void sendFrameUsingSocket(final byte[] frame) {
        final byte[] temp=frame;


        int width = mCameraWidth;
        int height = mCameraHeight;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        //TODO: find and use the height and width variable and remove the hard coded values
        YuvImage yuvImage = new YuvImage(frame, ImageFormat.NV21, width, height, null); //width : 768, height : 1280
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 50, out); //width : 768, height : 1280
        final byte[] imageBytes = out.toByteArray();

        if(mWebSocket==null || mWebSocket.isCancelled()){initializeWebSocket();}

        WebSocket webtemp = mWebSocket.tryGet();
        if(webtemp!=null){mWebSocket.tryGet().send(imageBytes);
            Log.d(TAG,"trying to send bytes");
        }


//        Log.d(TAG,"Size of the frame is : " + imageBytes.length);
        //      mWebSocket.send(imageBytes);



    }




    /**
     * Override the render function from {@link ARRendererGLES20}.
     */
    @Override
    public void draw() {
        super.draw();

        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glFrontFace(GLES20.GL_CW);

        float[] projectionMatrix = ARToolKit.getInstance().getProjectionMatrix();

        doSomeTestingStuff();//delete this line later on
        // If the marker is visible, apply its transformation, and render a cube
        if (ARToolKit.getInstance().queryMarkerVisible(markerID)) {
            cube.draw(projectionMatrix, ARToolKit.getInstance().queryMarkerTransformation(markerID));
            mCounter=0;
        }else if(mCounter>NO_OF_FRAMES_TO_SKIP){
            sendFrameUsingSocket(mCameraData);
            mCounter=0;
        }else{
            mCounter++;
        }
    }

    private void doSomeTestingStuff() {
        Log.d(TAG,mContext.getFilesDir().toString());
        Log.d(TAG,mContext.fileList().toString());
    }
}
