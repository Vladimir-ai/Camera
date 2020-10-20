package com.example.camera;

import android.hardware.camera2.CameraManager;

public class CameraService {
    private CameraManager cam;

    public CameraService(CameraManager cam, String camID){
        this.cam = cam;

    }
}
