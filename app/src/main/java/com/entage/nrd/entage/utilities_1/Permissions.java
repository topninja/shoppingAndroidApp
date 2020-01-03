package com.entage.nrd.entage.utilities_1;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import android.util.Log;

public class Permissions {
    private static final String TAG = "Permissions";

    // Manifest.permission.CAMERA,
    public static final String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static final String[] PERMISSIONS_LOCATIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private static final int VERIFY_PERMISSIONS_REQUEST = 1 ;
    //public static final String CAMERA_PERMISSION = Manifest.permission.CAMERA ;
    public static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE ;
    public static final String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE ;

    public static boolean checkPermissionsArray(String[] permissions , Context context) {
        Log.d(TAG, "checkPermissionsArray: checking Permissions array.");

        for(int i = 0 ; i < permissions.length ; i++){
            String check = permissions[i];
            if(!checkPermissions(check, context)){
                return false;
            }
        }

        return true;
    }

    public static boolean checkPermissions(String permission, Context context) {
        Log.d(TAG, "checkPermissions: checking permission: "+ permission);

        int permissionRequest = ActivityCompat.checkSelfPermission(context, permission);

        if(permissionRequest != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "checkPermissions: permission was not granted for: "+ permission);
            return false;
        }else {
            return true;
        }
    }

    public static  void verifyPermissions(String[] permissions , Context context) {
        Log.d(TAG, "verifyPermissions: verifying Permissions.");

        ActivityCompat.requestPermissions((Activity) context, permissions, VERIFY_PERMISSIONS_REQUEST);
    }

    public static int checkPermission(String permission, Context context) {
        Log.d(TAG, "checkPermissions: checking permission: "+ permission);

        return ActivityCompat.checkSelfPermission(context, permission);
    }

}
