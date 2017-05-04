package com.kartikgupta.myapplication.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.opengl.GLES20;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.kartikgupta.myapplication.MagicData;
import com.kartikgupta.myapplication.helper.shader.SimpleFragmentShader;
import com.kartikgupta.myapplication.helper.shader.SimpleShaderProgram;
import com.kartikgupta.myapplication.helper.shader.SimpleVertexShader;

import org.artoolkit.ar.base.ARToolKit;
import org.artoolkit.ar.base.rendering.gles20.ARRendererGLES20;
import org.artoolkit.ar.base.rendering.gles20.CubeGLES20;
import org.artoolkit.ar.base.rendering.gles20.ShaderProgram;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by kartik on 17/4/17.
 */

public class SperoRenderer extends ARRendererGLES20 {


    static {
        System.loadLibrary("c++_shared");
        System.loadLibrary("ARWrapper");
        System.loadLibrary("magic");
    }

    private static final int NO_OF_FRAMES_TO_SKIP = 100; //this many frames get skipped before sending a frame to server
    private int markerID = -1;
    private CubeGLES20 cube;

    private final String TAG = SperoRenderer.class.getSimpleName();
    //from arbase lib

    private Context mContext;
    private byte[] mCameraData;
    private int mCameraWidth;
    private int mCameraHeight;
    private AssetCacheHelper mAssetCacheHelper ;
    private NetworkConnection mConnection;

    private static final String CAMERA_PREVIEW_FEED_INTENT = "camera_preview_feed_intent";
    private static final String CAMERA_FEED_DATA = "camera_feed_data";
    private static final String CAMERA_PARAM_HEIGHT = "camera_param_height";
    private static final String CAMERA_PARAM_WIDTH = "camera_param_width";
    private static final String RECIEVED_MAGIC_BYTES = "recieved_magic_bytes" ;
    private static final String MAGIC_DATA = "magic_data";

    //Declaring the native methods of lbmagic.so

    public static native void Initialise();
    public static native void Shutdown();
    public static native void SurfaceCreated();
    public static native void SurfaceChanged(int w, int h);
    public static native boolean DrawFrame();
    public static native int  AddMarkerAndModel(String modelFilePath , String markerConfigStatement);
    public static native void DeleteMarkerAndModel(int markerIndex);



    BroadcastReceiver mCameraFrameDataBroadcastRecever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mCameraData=intent.getByteArrayExtra(CAMERA_FEED_DATA);
            mCameraWidth = intent.getIntExtra(CAMERA_PARAM_WIDTH,0);
            mCameraHeight=intent.getIntExtra(CAMERA_PARAM_HEIGHT,0); //note that 0 will mean error eventualylly
            Log.d(TAG,"getting camera feed data");
        }
    };

    BroadcastReceiver mMagicDataBroadcastRecever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            processMagicDataBytes(intent.getByteArrayExtra(MAGIC_DATA));
        }
    };


    private int mCounter=0;

    public SperoRenderer(Context context) {
        mContext = context;
        mAssetCacheHelper = new AssetCacheHelper(mContext);
        mConnection = new NetworkConnection(mContext);
    }

    /**
     * This method gets called from the framework to setup the ARScene.
     * So this is the best spot to configure you assets for your AR app.
     * For example register used markers in here.
     */
    @Override
    public boolean configureARScene() {
        SperoRenderer.Initialise();
        return true;
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int w, int h) {
        super.onSurfaceChanged(unused, w, h);
        SperoRenderer.SurfaceChanged(w,h);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mCameraFrameDataBroadcastRecever,
                new IntentFilter(CAMERA_PREVIEW_FEED_INTENT));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMagicDataBroadcastRecever,
                new IntentFilter(RECIEVED_MAGIC_BYTES));
    }

    //Shader calls should be within a GL thread that is onSurfaceChanged(), onSurfaceCreated() or onDrawFrame()
    //As the cube instantiates the shader during setShaderProgram call we need to create the cube here.
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        super.onSurfaceCreated(unused, config);
        SperoRenderer.SurfaceCreated();
        mConnection.initializeConnection();


        ShaderProgram shaderProgram = new SimpleShaderProgram(new SimpleVertexShader(), new SimpleFragmentShader());
        cube = new CubeGLES20(40.0f, 0.0f, 0.0f, 20.0f);
        cube.setShaderProgram(shaderProgram);



    }

    private void processMagicDataBytes(byte[] byteArrayExtra) {
        try {
            MagicData.Marker marker = MagicData.Marker.ADAPTER.decode(byteArrayExtra);
            processMagicData(marker);
        } catch (IOException e) {
            Log.d(TAG,"unable to decode MagicData");
            e.printStackTrace();
        }
    }

    private void processMagicData(MagicData.Marker marker) {
        markerID = mAssetCacheHelper.CopyAndAddMarker(marker);
        if(markerID<0){Log.d(TAG,"unable to copy marker");}
    }




    private void sendFrameToServer(final byte[] frame) {
        byte[] imageBytes = getSuitableFormatBytes(frame);
        mConnection.sendFrameBytes(imageBytes);
    }

    private byte[] getSuitableFormatBytes(byte[] frame) {
        final byte[] temp=frame;
        int width = mCameraWidth;
        int height = mCameraHeight;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        //TODO: find and use the height and width variable and remove the hard coded values
        YuvImage yuvImage = new YuvImage(frame, ImageFormat.NV21, width, height, null); //width : 768, height : 1280
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 50, out); //width : 768, height : 1280
        final byte[] imageBytes = out.toByteArray();
        return  imageBytes;

    }


    /**
     * Override the render function from {@link ARRendererGLES20}.
     */
    @Override
    public void draw() {
        super.draw();
        if(SperoRenderer.DrawFrame()){
        //marker found and model drawn
        //reset the counter
            mCounter=0;
        }else if(mCounter>NO_OF_FRAMES_TO_SKIP){
            sendFrameToServer(mCameraData);
            mCounter=0;
        }else{
            //increase the counter
            mCounter++;
        }

        //depracatedDrawMethod(); //TODO : to be deleted later on
    }

    private void depracatedDrawMethod() {
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glFrontFace(GLES20.GL_CW);
        float[] projectionMatrix = ARToolKit.getInstance().getProjectionMatrix();

        // doSomeTestingStuff();//delete this line later on
        // If the marker is visible, apply its transformation, and render a cube
        if (ARToolKit.getInstance().queryMarkerVisible(markerID)) {
            cube.draw(projectionMatrix, ARToolKit.getInstance().queryMarkerTransformation(markerID));
            mCounter=0;
        }else if(mCounter>NO_OF_FRAMES_TO_SKIP){
            sendFrameToServer(mCameraData);
            mCounter=0;
        }else{
            mCounter++;
        }
    }

    private void doSomeTestingStuff() {
        Log.d(TAG,mContext.getCacheDir().toString());
        for(File file:mContext.getCacheDir().listFiles()){
            Log.d(TAG,file.getName().toString());
            if(file.getName().toString().equals("DataNFT")){
                for(File file1 : file.listFiles()){
                    Log.d(TAG,file1.getName().toString());
                    //getting the various marker file names , now I need to see f i can write a new file to it.
                }
            }
        }
    }
}
