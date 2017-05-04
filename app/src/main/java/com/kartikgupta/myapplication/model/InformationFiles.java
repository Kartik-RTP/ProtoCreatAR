package com.kartikgupta.myapplication.model;

import java.io.File;

/**
 * Created by kartik on 4/5/17.
 */

class InformationFiles {
    File mOBJFile ;
    File mMTLFile ;
    File mTextureDirectory;

    InformationFiles(){

    }

    public File getmOBJFile() {
        return mOBJFile;
    }

    public void setmOBJFile(File mOBJFile) {
        this.mOBJFile = mOBJFile;
    }

    public File getmMTLFile() {
        return mMTLFile;
    }

    public void setmMTLFile(File mMTLFile) {
        this.mMTLFile = mMTLFile;
    }

    public File getmTextureDirectory() {
        return mTextureDirectory;
    }

    public void setmTextureDirectory(File mTextureDirectory) {
        this.mTextureDirectory = mTextureDirectory;
    }
}
