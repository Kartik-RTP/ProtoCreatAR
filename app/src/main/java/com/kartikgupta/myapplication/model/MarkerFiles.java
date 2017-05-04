package com.kartikgupta.myapplication.model;

import java.io.File;

/**
 * Created by kartik on 4/5/17.
 */

public class MarkerFiles {

    File mMarkerFsetFile  ;
    File mMarkerIsetFile  ;
    File mMarkerFset3File ;
    InformationFiles mInformationFiles ;

    MarkerFiles(){

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
}
