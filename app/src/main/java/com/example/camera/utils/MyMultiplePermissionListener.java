package com.example.camera.utils;

import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

public class MyMultiplePermissionListener implements MultiplePermissionsListener {
    @Override
    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
    }

    @Override
    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
        if(permissionToken != null)
            permissionToken.continuePermissionRequest();
    }
}
