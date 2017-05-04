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
    /**
    This class object will help to add / remove  "marker" and "information" files from
    internal cache of the app at the runtime

     Internal cache structure is as below : -

     InternalCache
     |------------Data
                    |-------------
                    |-------------
                    |-------------models
                                  |---------------cube.obj
                                  |---------------cube.mtl
                                  |---------------textures
                                                  |------------------a.png
     |------------DataNFT
                    |-------------pinball.iset
                    |-------------pinball.fset
                    |-------------pinball.fset3

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


    public void DeleteMarkerFilesFromCache(MarkerFiles markerFiles) {
        try {
            deleteFileFromStorage(markerFiles.getmMarkerFsetFile());
            deleteFileFromStorage(markerFiles.getmMarkerIsetFile());
            deleteFileFromStorage(markerFiles.getmMarkerFset3File());
            deleteFileFromStorage(markerFiles.getmInformationFiles().getmMTLFile());
            deleteFileFromStorage(markerFiles.getmInformationFiles().getmOBJFile());
            deleteDirectoryAndFiles(markerFiles.getmInformationFiles().getmTextureDirectory());
        }catch (Exception e){
            Log.d(TAG,"unable to delete markerFiles for"+markerFiles.getMarkerName());
            e.printStackTrace();
        }
    }

    /**
     * Note that the following function uses file.getName() even in case the files has
     * been deleted ...This can be a source of potential error
     * TODO: check out the above doubt
     * @param file
     * @return
     * @throws Exception
     */
    private boolean deleteFileFromStorage(File file) throws Exception {
        if(!file.exists()){
            return true;
            //since file isn't there , we can consider delete to be a success
        }
        boolean result =  file.delete();
        if(result){
            Log.d(TAG,file.getName()+"is deleted");
        }else {
            Log.d(TAG,file.getName()+"is NOT deleted");
            throw new Exception("unable to delete the "+file.getName());
        }

        return result;
    }

    private boolean deleteDirectoryAndFiles(File directoryFile) throws Exception {
        if(directoryFile.isDirectory()){
            for (File file : directoryFile.listFiles()){
                deleteFileFromStorage(file);
            }
        }
        boolean result = directoryFile.delete();
        return result;
    }



}
