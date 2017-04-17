package com.kartikgupta.myapplication.helper;

import android.widget.FrameLayout;

import org.artoolkit.ar.base.ARActivity;
import org.artoolkit.ar.base.rendering.ARRenderer;

/**
 * Created by kartik on 17/4/17.
 */

public class BaseMagicActivity extends ARActivity {
    @Override
    protected ARRenderer supplyRenderer() {
        return new SperoRenderer();
    }

    @Override
    protected FrameLayout supplyFrameLayout() {
        return new FrameLayout(this);
    }
}
