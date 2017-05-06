package com.kartikgupta.myapplication.helper;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by kartik on 6/5/17.
 */

public class HelperUtilities {


    private static final String TAG = HelperUtilities.class.getSimpleName();

    public static void writeDataToFile(String pathIncludingFileName , byte[] fileData) throws IOException {
        FileOutputStream fos = new FileOutputStream(pathIncludingFileName);
        fos.write(fileData);
        fos.close();
    }
    
    public static void ListFilesInDirectoryFiles(File directoryFile){
        if(directoryFile.isDirectory()){
            for(File file:directoryFile.listFiles()){
                System.out.print(file.getName()+"\n");
            }
        }else{
            Log.d(TAG,directoryFile.getName().toString()+"isn't a directory");
        }
    }

}
