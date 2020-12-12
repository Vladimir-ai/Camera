package com.example.camera.utils;

import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class MyPermissionListener implements PermissionListener {
    @Override
    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
    }

    @Override
    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
    }

    @Override
    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
        if(permissionToken != null)
            permissionToken.continuePermissionRequest();
    }
}
