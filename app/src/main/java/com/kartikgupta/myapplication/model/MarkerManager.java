package com.kartikgupta.myapplication.model;

/**
 * Created by kartik on 4/5/17.
 */


import android.util.Log;

import com.kartikgupta.myapplication.MagicData;
import com.kartikgupta.myapplication.helper.AssetCacheHelper;
import com.kartikgupta.myapplication.model.MarkerFiles;

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

    private AssetCacheHelper mAssetCacheHelper;


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
        mAssetCacheHelper.removeMarker(eldest.getValue());
        return size() > mSize;
    }

    public void addMarker(MagicData magicData){
        com.kartikgupta.myapplication.model.MarkerFiles markerFiles = generateMarkerFiles(magicData.marker);
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



    private InformationFiles generateInformationFiles(MagicData.Information information) {
        //TODO:implement it
        return  new InformationFiles();
    }

    private com.kartikgupta.myapplication.model.MarkerFiles generateMarkerFiles(MagicData.Marker marker) {
        //TODO:implement it
        return new com.kartikgupta.myapplication.model.MarkerFiles();
    }

}