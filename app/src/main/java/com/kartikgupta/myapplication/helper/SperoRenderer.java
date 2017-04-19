package com.kartikgupta.myapplication.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.opengl.GLES20;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.FrameLayout;

import com.kartikgupta.myapplication.NewMagicActivity;
import com.kartikgupta.myapplication.helper.shader.SimpleFragmentShader;
import com.kartikgupta.myapplication.helper.shader.SimpleShaderProgram;
import com.kartikgupta.myapplication.helper.shader.SimpleVertexShader;

import org.artoolkit.ar.base.ARToolKit;
import org.artoolkit.ar.base.rendering.gles20.ARRendererGLES20;
import org.artoolkit.ar.base.rendering.gles20.CubeGLES20;
import org.artoolkit.ar.base.rendering.gles20.ShaderProgram;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.content.ContentValues.TAG;

/**
 * Created by kartik on 17/4/17.
 */

public class SperoRenderer extends ARRendererGLES20 {

    private int markerID = -1;
    private CubeGLES20 cube;

    private final String TAG = SperoRenderer.class.getSimpleName();
    //from arbase lib

    private Context mContext;
    private byte[] mCameraData;
    private static final String CAMERA_PREVIEW_FEED_INTENT = "camera_preview_feed_intent";
    private static final String CAMERA_FEED_DATA = "camera_feed_data";

    BroadcastReceiver mBroadcastRecever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mCameraData=intent.getByteArrayExtra(CAMERA_FEED_DATA);
            Log.d(TAG,"getting camera feed data");
        }
    };

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

        // If the marker is visible, apply its transformation, and render a cube
        if (ARToolKit.getInstance().queryMarkerVisible(markerID)) {
            cube.draw(projectionMatrix, ARToolKit.getInstance().queryMarkerTransformation(markerID));
        }
    }
}
