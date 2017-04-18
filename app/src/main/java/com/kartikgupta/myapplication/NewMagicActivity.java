package com.kartikgupta.myapplication;

import android.os.Bundle;
import android.widget.FrameLayout;

import com.kartikgupta.myapplication.helper.BaseMagicActivity;
import com.kartikgupta.myapplication.helper.SperoRenderer;

import org.artoolkit.ar.base.ARActivity;
import org.artoolkit.ar.base.rendering.ARRenderer;

/**
 * Created by kartik on 17/4/17.
 */

public class NewMagicActivity  extends ARActivity{


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    /**
     *
     * Tell the ARToolKit which renderer to use. In this case we provide a subclass of
     * {@link org.artoolkit.ar.base.rendering.gles20.ARRendererGLES20} renderer.
     */
    @Override
    protected ARRenderer supplyRenderer() {
        return new SperoRenderer();
    }

    /**
     * Use the FrameLayout in this Activity's UI.
     */
    @Override
    protected FrameLayout supplyFrameLayout() {
        return (FrameLayout) this.findViewById(R.id.mainLayout);
    }
}
