package com.kartikgupta.myapplication.helper;

import android.content.Context;
import android.util.Log;

import com.kartikgupta.myapplication.MagicData;
import com.kartikgupta.myapplication.model.MarkerFiles;

import org.artoolkit.ar.base.ARToolKit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okio.ByteString;

/**
 * Created by kartik on 1/5/17.
 */

public class AssetCacheHelper {
    /*
    This class object will help to add / remove  "marker" and "information" files from
    internal cache of the app at the runtime
     */

    private static final String TAG = AssetCacheHelper.class.getSimpleName();
    private Context mContext;

    public AssetCacheHelper(Context context){
        mContext = context;
    }
    public void copyMarkerFilesToAsset(MagicData.Marker marker) {

        //doSomeTestingStuff();
        try {
            Log.d(TAG,"copying new marker content to cache");
            writeFileToCache(marker.fset,marker.markerName+".fset");
            writeFileToCache(marker.fset3,marker.markerName+".fset3");
            writeFileToCache(marker.iset,marker.markerName+".iset");
//            doSomeTestingStuff();
    //        markerID = ARToolKit.getInstance().addMarker("nft;DataNFT/pinball");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    this method also returns the markerID
     */
    public int CopyAndAddMarker(MagicData.Marker marker) {

        int markerID=-1;
        //doSomeTestingStuff();
        try {
            Log.d(TAG,"copying new marker content to cache");
            writeFileToCache(marker.fset,marker.markerName+".fset");
            writeFileToCache(marker.fset3,marker.markerName+".fset3");
            writeFileToCache(marker.iset,marker.markerName+".iset");
//            doSomeTestingStuff();
             markerID = ARToolKit.getInstance().addMarker("nft;DataNFT/"+marker.markerName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return markerID;
    }

    private void writeFileToCache(ByteString fileData, String fileNameWithExtension) throws IOException {
        File cacheDirFile = mContext.getCacheDir();
        File DataNFTFile = null;
        for(File file : cacheDirFile.listFiles()){
            if(file.getName().toString().equals("DataNFT")){
                DataNFTFile = file;
            }
        }
        String path = DataNFTFile.getAbsolutePath()+File.separator+fileNameWithExtension;

        FileOutputStream fos = new FileOutputStream(path);
        fos.write(fileData.toByteArray());
        fos.close();
    }


    public void removeMarker(MarkerFiles value) {
        //TODO:implement it
    }
}
