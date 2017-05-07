package com.kartikgupta.myapplication.helper;

import android.content.Context;
import android.util.Log;

import com.kartikgupta.myapplication.MagicData;
import com.kartikgupta.myapplication.model.InformationFiles;
import com.kartikgupta.myapplication.model.MarkerFiles;

import org.artoolkit.ar.base.ARToolKit;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okio.ByteString;

/**
 * Created by kartik on 1/5/17.
 */

public class AssetCacheHelper {
    /**
    This class object will help to add / remove  "marker" and "information" files from
    internal cache of the app at the runtime

     Internal cache structure is as below : -

     CacheDirectory
         |
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

    public MarkerFiles copyMarkerFilesAndInformationFilesToAssetAndReturnMarkerFiles(MagicData markerWithInformationData) throws Exception {

        boolean markerCopied = copyMarkerFilesToAsset(markerWithInformationData.marker);
        boolean informationCopied = copyInformationFilesToAsset(
                                              markerWithInformationData.information,
                                              markerWithInformationData.marker.markerName
                                                                                );
        if(markerCopied&&informationCopied){
            Log.d(TAG,"Successfully copied Markerfile and InformationFiles");

           MarkerFiles markerFiles =  generateMarkerFile(
                                     markerWithInformationData.marker.markerName
                                                                        );
           return markerFiles;
        }else{
            throw new Exception("marker and information files couldn't be generated");
        }

        //return null;
    }

    private MarkerFiles generateMarkerFile(String markerName) throws Exception {
        MarkerFiles markerFiles = new MarkerFiles();

        markerFiles.setmMarkerNFTFilesDirectoryFile(getDataNFTDirectoryFile());
        markerFiles.setmMarkerFset3File(getFileFromDataNFTDirectory(markerName+".fset3"));
        markerFiles.setmMarkerFsetFile(getFileFromDataNFTDirectory(markerName+".fset"));
        markerFiles.setmMarkerIsetFile(getFileFromDataNFTDirectory(markerName+".iset"));
        markerFiles.setmInformationFiles(getInformationFileFromDataDirectoryInCache(markerName));

        return markerFiles;
    }

    private InformationFiles getInformationFileFromDataDirectoryInCache(String markerName) throws Exception {
        InformationFiles informationFiles = new InformationFiles();
        File modelDirectoryInCache = getModelDirectoryFileFromCache();
        File markerModelDirectoryInCacheFile=null;
        for(File file : modelDirectoryInCache.listFiles()){
            if(file.getName().toString().equals(markerName)){
                markerModelDirectoryInCacheFile=file;
            }
        }

        if(markerModelDirectoryInCacheFile.exists()){
            informationFiles.setmInformationDirectory(markerModelDirectoryInCacheFile);
        }

        for(File file : markerModelDirectoryInCacheFile.listFiles()){
            if(file.getName().toString().contains(".obj")){
                informationFiles.setmOBJFile(file);
            }else if(file.getName().toString().contains(".mtl")){
                informationFiles.setmMTLFile(file);
            }else if(file.getName().toString().equals("texture")&&file.isDirectory()){
                informationFiles.setmTextureDirectory(file);
            }
        }


        return  informationFiles;
    }

    private File getFileFromDataNFTDirectory(String fileNameWithExtension) throws Exception {
        File dataNFTDirectoryFile = getDataNFTDirectoryFile();
        File searchFile = new File(dataNFTDirectoryFile.getAbsolutePath()+
                                   File.separator+fileNameWithExtension);
        if(!searchFile.exists()){
            try {
                throw new Exception(fileNameWithExtension+"doesn't exist");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(searchFile==null){
            Log.d(TAG,fileNameWithExtension+" not found in DataNFT");
        }

        return  searchFile;
    }

    private boolean copyInformationFilesToAsset(MagicData.Information information , String markerName) {

        try{
            writeFileToDataModelDirectoryInCache(information.mtl,markerName,markerName+".mtl");
            writeFileToDataModelDirectoryInCache(information.obj,markerName,markerName+".obj");
            writeFileToDataModelDirectoryInCache(information.image,markerName);
        }catch (IOException e){
            e.printStackTrace();
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void writeFileToDataModelDirectoryInCache(List<MagicData.Images> images, String markerName) throws Exception {
        File dataModelDirectoryFile = getModelDirectoryFileFromCache();
        File modelForMarkerDirectoryFile = new File(dataModelDirectoryFile.getAbsolutePath()
                +File.separator+markerName);
        if(!modelForMarkerDirectoryFile.exists()){
            modelForMarkerDirectoryFile.mkdirs(); //create that folder
        }
        File textureDirectoryFile = new File(modelForMarkerDirectoryFile+File.separator+
                                        "texture");
        if(!textureDirectoryFile.exists()){
            textureDirectoryFile.mkdirs(); //create that folder
        }
        for(MagicData.Images image:images){
            HelperUtilities.writeDataToFile(
                    textureDirectoryFile.getAbsolutePath()+File.separator+image.imageNameWithExtension
                    ,image.imagebytes.toByteArray());
        }



    }

    private void writeFileToDataModelDirectoryInCache(ByteString fileData, String markerName , String fileNameWithExtension) throws Exception {
        File dataModelDirectoryFile = getModelDirectoryFileFromCache();
        File modelForMarkerDirectoryFile = new File(dataModelDirectoryFile.getAbsolutePath()
                                                    +File.separator+markerName);
        if(!modelForMarkerDirectoryFile.exists()){
            modelForMarkerDirectoryFile.mkdirs(); //create that folder
        }else{
            Log.d(TAG,"unable to find modelForMarkerDirectory");
        }

        String path = modelForMarkerDirectoryFile+File.separator+fileNameWithExtension;
        HelperUtilities.writeDataToFile(path, fileData.toByteArray());
    }

    private File getModelDirectoryFileFromCache() throws Exception {

        File dataDirectoryFile = getDataDirectoryFile();
        File dataModelDirectoryFile=null;
        for(File file : dataDirectoryFile.listFiles()){
            if(file.getName().toString().equals("models")){
                dataModelDirectoryFile=file;
                Log.d(TAG,"found the Data/models directory");
            }
        }
        if(dataModelDirectoryFile==null){
            dataModelDirectoryFile=createNewModelDirectory();
        }
        if(dataModelDirectoryFile==null){
            Log.d(TAG,"Error in retreving \'Data/models\' directory");
            HelperUtilities.ListFilesInDirectoryFiles(dataDirectoryFile);
            throw new Exception("Error in retreving \'Data/models\' directory");
        }
        return dataModelDirectoryFile;
    }

    private File getDataDirectoryFile() throws Exception {
        File cacheDirFile = mContext.getCacheDir();
        File dataDirectoryFile = null;
        File dataModelDirectoryFile = null;
        for(File file : cacheDirFile.listFiles()){
            if(file.getName().toString().equals("Data")){
                dataDirectoryFile   = file;
            }
        }
        if(dataDirectoryFile==null){
            Log.d(TAG,"Error in retrieving the \'Data\' directory from cache");
            HelperUtilities.ListFilesInDirectoryFiles(cacheDirFile);
            throw new Exception("Error in retrieving the \'Data\' directory from cache");
        }
        return dataDirectoryFile;
    }

    private File createNewModelDirectory() throws Exception {
        File dataDirecoryFile = getDataDirectoryFile();
        File modelDirectory = new File(dataDirecoryFile.getAbsolutePath()+File.separator+"models");
        if(!modelDirectory.exists()){
            modelDirectory.mkdir();
        }
        return modelDirectory;
    }

    public boolean copyMarkerFilesToAsset(MagicData.Marker marker) throws Exception {

        //doSomeTestingStuff();
        try {
            Log.d(TAG,"copying new marker content to cache");
            try {
                writeFileToDataNFTDirectoryInCache(marker.fset,marker.markerName+".fset");
                writeFileToDataNFTDirectoryInCache(marker.fset3,marker.markerName+".fset3");
                writeFileToDataNFTDirectoryInCache(marker.iset,marker.markerName+".iset");
            } catch (Exception e) {
                Log.d(TAG,"unable to write Files to DataNFT directory in Cache");
                e.printStackTrace();
                throw new Exception("unable to write Files to DataNFT directory in Cache");
            }

//            doSomeTestingStuff();
    //        markerID = ARToolKit.getInstance().addMarker("nft;DataNFT/pinball");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /*
    this method also returns the markerID.
    this function is depracated , no longer used
     */
    public int CopyAndAddMarker(MagicData.Marker marker) throws Exception {

        int markerID=-1;
        //doSomeTestingStuff();
        try {
            Log.d(TAG,"copying new marker content to cache");
            writeFileToDataNFTDirectoryInCache(marker.fset,marker.markerName+".fset");
            writeFileToDataNFTDirectoryInCache(marker.fset3,marker.markerName+".fset3");
            writeFileToDataNFTDirectoryInCache(marker.iset,marker.markerName+".iset");
//            doSomeTestingStuff();
             markerID = ARToolKit.getInstance().addMarker("nft;DataNFT/"+marker.markerName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return markerID;
    }

    private void writeFileToDataNFTDirectoryInCache(ByteString fileData, String fileNameWithExtension) throws Exception {
        Log.d(TAG,"copying "+fileNameWithExtension+"in DataNFT");
        File dataNFTDirectoryFile = getDataNFTDirectoryFile();
        if(dataNFTDirectoryFile==null){
            throw  new Exception("unable to get DataNFTDirectoy file");
        }
        String path = dataNFTDirectoryFile.getAbsolutePath()+File.separator+fileNameWithExtension;
        HelperUtilities.writeDataToFile(path,fileData.toByteArray());
        Log.d(TAG,"Successfully copyied "+fileNameWithExtension+"in DataNFT");
    }

    private File getDataNFTDirectoryFile() throws Exception {
        File cacheDirFile = mContext.getCacheDir();
        File DataNFTFile = null;
        for(File file : cacheDirFile.listFiles()){
            if(file.getName().toString().equals("DataNFT") && file.isDirectory()){
                DataNFTFile = file;
            }
        }
        if(DataNFTFile==null){
            Log.d(TAG,"DataNFT file not found");
            HelperUtilities.ListFilesInDirectoryFiles(cacheDirFile);
            throw new Exception("unable to find DataNFT directory in Cache");
        }
        return DataNFTFile;
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
