package com.example.camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;


public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = "myLogs";
    private static final int REQUEST_CODE = 0;
    private static final String[] REQUEST_PERMISSIONS = {
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            setContentView(R.layout.activity_main_landscape);

        requestPermissions(REQUEST_PERMISSIONS, REQUEST_CODE);

        FragmentManager fragManager = getSupportFragmentManager();
        CameraFragment cameraFragment = (CameraFragment) fragManager.findFragmentById(R.id.camFragment);
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(LOG_TAG, "ReqestPermissions");
        for(int i = 0; i < grantResults.length - 1; i++){
            if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                finish();
            }
        }
        Log.i(LOG_TAG, "PermGranted");
    }
}