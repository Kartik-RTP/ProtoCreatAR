package com.kartikgupta.myapplication.model;

import java.io.File;

/**
 * Created by kartik on 4/5/17.
 */

public class MarkerFiles {

    File mMarkerFsetFile  ;
    File mMarkerIsetFile  ;
    File mMarkerFset3File ;
    File mMarkerNFTFilesDirectoryFile;
    InformationFiles mInformationFiles ;
    private String mMarkerName;

    public MarkerFiles(){

    }

    public File getmMarkerNFTFilesDirectoryFile() {
        return mMarkerNFTFilesDirectoryFile;
    }

    public void setmMarkerNFTFilesDirectoryFile(File mMarkerNFTFilesDirectoryFile) {
        this.mMarkerNFTFilesDirectoryFile = mMarkerNFTFilesDirectoryFile;
    }

    public File getmMarkerFsetFile() {
        return mMarkerFsetFile;
    }

    public void setmMarkerFsetFile(File mMarkerFsetFile) {
        this.mMarkerFsetFile = mMarkerFsetFile;
    }

    public File getmMarkerIsetFile() {
        return mMarkerIsetFile;
    }

    public void setmMarkerIsetFile(File mMarkerIsetFile) {
        this.mMarkerIsetFile = mMarkerIsetFile;
    }

    public File getmMarkerFset3File() {
        return mMarkerFset3File;
    }

    public void setmMarkerFset3File(File mMarkerFset3File) {
        this.mMarkerFset3File = mMarkerFset3File;
    }

    public InformationFiles getmInformationFiles() {
        return mInformationFiles;
    }

    public void setmInformationFiles(InformationFiles mInformationFiles) {
        this.mInformationFiles = mInformationFiles;
    }

    public String getMarkerName() {
        return mMarkerName;
    }

    public String getmMarkerName() {
        return mMarkerName;
    }

    public void setmMarkerName(String mMarkerName) {
        this.mMarkerName = mMarkerName;
    }
}
