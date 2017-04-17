package com.kartikgupta.myapplication;

import android.app.Application;

import org.artoolkit.ar.base.assets.AssetHelper;

/**
 * Created by kartik on 17/4/17.
 */

public class CreatARApplication extends Application {

    private static Application mInstance;

    // Anywhere in the application where an instance is required, this method
    // can be used to retrieve it.
    public static Application getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        ((CreatARApplication) mInstance).initializeInstance();
    }

    // Here we do one-off initialisation which should apply to all activities
    // in the application.
    protected void initializeInstance() {
        // Unpack assets to cache directory so native library can read them.
        // N.B.: If contents of assets folder changes, be sure to increment the
        // versionCode integer in the modules build.gradle file.
        AssetHelper assetHelper = new AssetHelper(getAssets());
        assetHelper.cacheAssetFolder(getInstance(), "Data");
        assetHelper.cacheAssetFolder(getInstance(), "DataNFT");


    }
}
