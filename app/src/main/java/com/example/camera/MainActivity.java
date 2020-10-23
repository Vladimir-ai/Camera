package com.example.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.CamcorderProfile;
import android.media.MediaCodec;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Menu;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity {

    Surface recorderSurface =null;
    public static final String LOG_TAG = "Cam/activity logs";
    private static final int REQUEST_CODE = 0;
    private static final String[] REQUEST_PERMISSIONS = {
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO};

    private CameraManager m_camManager;
    private CameraService m_camService;
    private Button m_buttonOpenCam;
    private Button m_buttonRecVideo;
    private Button m_buttonStopVideo;
    private HandlerThread m_backgroundThread;
    private Handler m_backgroundHandler;
    private File m_file;
    private TextureView m_textureView;

    private MediaRecorder m_mediaRecorder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            setContentView(R.layout.activity_main_landscape);

        requestPermissions(REQUEST_PERMISSIONS, REQUEST_CODE);

        FragmentManager fragManager = getSupportFragmentManager();
        //CameraFragment cameraFragment = (CameraFragment) fragManager.findFragmentById(R.id.camFragment);
        m_textureView = findViewById(R.id.textureView);

        m_buttonOpenCam = findViewById(R.id.openCam);
        m_buttonRecVideo = findViewById(R.id.startRec);
        m_buttonStopVideo = findViewById(R.id.stopRec);

        m_buttonOpenCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(m_camService != null){
                    if(!m_camService.isOpen()) m_camService.openCamera();
                }
            }
        });

        m_buttonRecVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((m_camService != null) && m_mediaRecorder != null) {
                    m_mediaRecorder.start();
                }
            }
        });

        m_buttonStopVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((m_camService != null) && m_mediaRecorder != null){
                    m_camService.stopRecording();
                }
            }
        });

        m_camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try{
            for(String camID : m_camManager.getCameraIdList()){
                if(m_camManager.getCameraCharacteristics(camID).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK){
                    m_camService = new CameraService(m_camManager, camID);
                }
            }
        } catch (CameraAccessException e) {
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        }

        setUpMediaRecorder();

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
        for(int i = 0; i < grantResults.length; i++){
            if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                finish();
            }
        }
        Log.i(LOG_TAG, "PermGranted");
    }

    private void startBackgroundThread(){
        m_backgroundThread = new HandlerThread("Camera background");
        m_backgroundThread.start();
        m_backgroundHandler = new Handler(m_backgroundThread.getLooper());
    }

    private void stopBackgroundThread(){
        m_backgroundThread.quitSafely();
        try{
            m_backgroundThread.join();
            m_backgroundThread = null;
            m_backgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setUpMediaRecorder(){
        m_mediaRecorder = new MediaRecorder();

        m_mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        m_mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        m_mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        m_file = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DCIM), Calendar.getInstance().getTime().toString());
        m_mediaRecorder.setOutputFile(m_file.getAbsoluteFile());
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P); // TO-DO remove hard code
        profile.videoFrameRate = 30; // TO-DO AS said above
        m_mediaRecorder.setVideoFrameRate(profile.videoFrameRate);
        m_mediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
        m_mediaRecorder.setVideoEncodingBitRate(profile.videoBitRate);
        m_mediaRecorder.setAudioEncodingBitRate(profile.audioBitRate);
        m_mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        m_mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        m_mediaRecorder.setAudioSamplingRate(profile.audioSampleRate);
    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
    }

    public class CameraService{

        private String m_camID;
        private CameraDevice m_camDevice = null;
        private CameraCaptureSession m_camCapSession;
        private CaptureRequest.Builder m_previewBuilder;

        public CameraService(CameraManager cameraManager, String cameraID){
            m_camManager = cameraManager;
            m_camID = cameraID;
        }

        private CameraDevice.StateCallback m_cameraCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice cameraDevice) {
                m_camDevice = cameraDevice;
                Log.i(LOG_TAG, "Opened cam with id: " + m_camID);

                startPreviewSession();
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice cameraDevice) {
                m_camDevice.close();

                Log.i(LOG_TAG, "Disconnected Cam with ID: " + m_camID);

                m_camDevice = null;
            }

            @Override
            public void onError(@NonNull CameraDevice cameraDevice, int i) {
                Log.i(LOG_TAG, "Error! " + i);
            }
        };

        private void startPreviewSession(){
            SurfaceTexture texture = m_textureView.getSurfaceTexture();
            texture.setDefaultBufferSize(1280, 720);
            Surface surface = new Surface(texture);

            try{
                m_previewBuilder = m_camDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                m_previewBuilder.addTarget(surface);
                recorderSurface= MediaCodec.createPersistentInputSurface();
                try {
                    m_mediaRecorder.prepare();
                    Log.i(LOG_TAG, "Started Media Recorder");

                } catch (Exception e) {
                    Log.i(LOG_TAG, "Media Recorder Stopped");
                }

                m_previewBuilder.addTarget(recorderSurface);

                m_camDevice.createCaptureSession(Arrays.asList(surface, recorderSurface), new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                        m_camCapSession = cameraCaptureSession;
                        try{
                            m_camCapSession.setRepeatingRequest(m_previewBuilder.build(), null, m_backgroundHandler);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                    }
                }, m_backgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        public void stopRecording(){
            MyTask mt = new MyTask();
            mt.execute();
        }

        public boolean isOpen(){
            if(m_camDevice == null){
                return false;
            }
            return true;
        }

        public void openCamera(){
            try{
                if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                    m_camManager.openCamera(m_camID, m_cameraCallback, m_backgroundHandler);
                }
            } catch (CameraAccessException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }



    }

    class MyTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(Void... params) {
            m_mediaRecorder.stop();
            m_mediaRecorder.release();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

        }
    }


}