package com.entage.nrd.entage.utilities_1;

import android.os.Environment;

public class FilePath {

    // "storage/emulated/0"
    public String ROOT_DIR = Environment.getExternalStorageDirectory().getPath();

    public String PICTURES = ROOT_DIR + "/DCIM" ;
    public String CAMERA = ROOT_DIR + "/DCIM/camera";
    public String FIRBASE_IMAGE_STORAGE = "photos/";

}
