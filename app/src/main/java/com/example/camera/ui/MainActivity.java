package com.example.camera.ui;


import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.video.OnVideoSavedCallback;
import androidx.camera.view.video.OutputFileResults;
import androidx.core.content.ContextCompat;

import com.example.camera.R;
import com.example.camera.utils.MyMultiplePermissionListener;
import com.example.camera.utils.MyPermissionListener;

import com.example.camera.databinding.ActivityMainLandscapeBinding;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;


public class MainActivity extends AppCompatActivity {
    private ActivityMainLandscapeBinding binding;

    private final MultiplePermissionsListener multiplePermissionsListener = new MyMultiplePermissionListener(){
        public void onPermissionsChecked(MultiplePermissionsReport report) {
            if (report.areAllPermissionsGranted()) {
                onPermissionGranted();
            } else {
                onPermissionDenied();
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainLandscapeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupView();
        requestRuntimePermission();
    }

    private void setupView(){
        binding.buttonRecordVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRecordVideoClick();
            }
        });
    }

    private void requestRuntimePermission() {
        Dexter.withContext(this)
                .withPermissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                .withListener(multiplePermissionsListener)
                .check();
    }

    @SuppressLint("MissingPermission")
    private void bindCamera() {
        binding.cameraView.bindToLifecycle(this);
        binding.cameraView.setPinchToZoomEnabled(true);

       // Currently, there's no zoom and camera bound listener supported for CameraView
//         seekBarZoom.max = ((cameraView.maxZoomRatio - cameraView.minZoomRatio) * 10).toInt()
//         seekBarZoom.progress = (cameraView.zoomRatio * 10).toInt()
    }

    private void onPermissionGranted(){
        bindCamera();
    }

    private void onPermissionDenied(){
        showResultMessage(getString(R.string.permission_denied));
        finish();
    }

    private void showResultMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private void onRecordVideoClick(){
        if (binding.cameraView.isRecording()) {
            binding.cameraView.stopRecording();
            onStopVideoRecording();
        } else {
            startVideoRecording();
            File file = new File(getFilesDir().getAbsoluteFile(), "temp.mp4");
            binding.cameraView.startRecording(file, ContextCompat.getMainExecutor(this), videoSavedCallback);
        }
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private OnVideoSavedCallback videoSavedCallback = new OnVideoSavedCallback() {
        public void onVideoSaved(OutputFileResults outputFileResults) {
            showResultMessage(getString(R.string.video_record_success));
        }

        @SuppressLint("StringFormatInvalid")
        @Override
        public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
            showResultMessage(getString(R.string.video_record_error, message, videoCaptureError));
        }
    };

    private void onStopVideoRecording() {
        binding.buttonRecordVideo.setText(R.string.start_record_video);
    }

    private void startVideoRecording() {
        binding.buttonRecordVideo.setText(R.string.stop_record_video);
    }
}