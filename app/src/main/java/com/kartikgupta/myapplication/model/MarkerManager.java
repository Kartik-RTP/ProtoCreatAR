package com.kartikgupta.myapplication.model;

/**
 * Created by kartik on 4/5/17.
 */


import android.util.Log;

import com.kartikgupta.myapplication.MagicData;
import com.kartikgupta.myapplication.helper.AssetCacheHelper;
import com.kartikgupta.myapplication.helper.SperoRenderer;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;


public class MarkerManager extends LinkedHashMap<Integer,MarkerFiles> {

    private static final int MAX_SIZE = 10;
    private static final String TAG = MarkerManager.class.getSimpleName();
    /**
     * This class will be used for managing markers , storing markers using markerIDs
     * as hash keys in a LinkedListHashMap , thereby acting as a LRU cache
     */


    private int mSize; //atmax can be 10 since in libmagic.so , it has been defined so
    // if taken more than that , then error will come up



    public MarkerManager(int size) throws Exception {
        super(size, 0.75f, true); //0.75f is the load factor...
                                  //check the meaning of loadFactor
        if(size > MAX_SIZE){
            throw new Exception("exceeded the allowable cache size which is "+MAX_SIZE);
        }//need to deal with this exception

        mSize = size;

    }

    public MarkerManager() {
        super(10, 0.75f, true); //0.75f is the load factor...
        //check the meaning of loadFactor
        mSize = 10; //putting the default size at 10 which is the max allowable size
    }



    @Override
    protected boolean removeEldestEntry(Map.Entry<Integer, MarkerFiles> eldest) {
        // so eldest represents the Least recently used item
        // before returning true , I have to delete the files representing
        // the eldest marker files from internal cache as well
        SperoRenderer.DeleteMarkerAndModel(eldest.getKey());
        DeleteFiles(eldest.getValue()); // check if this works
        return size() > mSize;

    }

    private void DeleteFiles(MarkerFiles markerFiles) {

            markerFiles.getmMarkerIsetFile().delete();
            markerFiles.getmMarkerFset3File().delete();
            markerFiles.getmMarkerFsetFile().delete();
            markerFiles.getmInformationFiles().getmMTLFile().delete();
            markerFiles.getmInformationFiles().getmOBJFile().delete();
            File temp = markerFiles.getmInformationFiles().getmTextureDirectory();
            for(File file : temp.listFiles()){
                file.delete();
            }
            temp.delete();

        //TODO:implement proper logging
    }

/*
    public void addMarker(MagicData magicData){
        //needs an overhaul
        //TODO
        MarkerFiles markerFiles = generateMarkerFiles(magicData.marker);
        InformationFiles informationFiles = generateInformationFiles(magicData.information);
        markerFiles.setmInformationFiles(informationFiles);
        int markerID = mAssetCacheHelper.CopyAndAddMarker(magicData.marker);
        if(markerID>-1){
            //means successfully added in internal ARModelArray
            put(markerID,markerFiles);
        }else{
            Log.d(TAG,"Some error in adding marker");
        }
    }
*/




}