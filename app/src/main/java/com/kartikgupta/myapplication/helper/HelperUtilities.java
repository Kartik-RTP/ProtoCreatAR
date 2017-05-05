package com.kartikgupta.myapplication.helper;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by kartik on 6/5/17.
 */

public class HelperUtilities {

    public static void writeDataToFile(String pathIncludingFileName , byte[] fileData) throws IOException {
        FileOutputStream fos = new FileOutputStream(pathIncludingFileName);
        fos.write(fileData);
        fos.close();
    }

}
