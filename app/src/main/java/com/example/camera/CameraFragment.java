package com.example.camera;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.SessionConfiguration;
import android.os.Bundle;
import android.util.Log;
import android.util.Range;
import android.view.LayoutInflater;
import android.view.SurfaceControl;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.Arrays;

public class CameraFragment extends Fragment {
    private static final String LOG_TAG = "CameraFragment";
    private CameraCharacteristics camCharacterictics;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(LOG_TAG, "View Creating Started");
        return inflater.inflate(R.layout.camera_fragment, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "View created");
        FragmentManager fragManager = getFragmentManager();
        try{
            CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
            String[] idList = manager.getCameraIdList();

            CameraCharacteristics cameraCharacteristics;
            for(String camID : idList){
                cameraCharacteristics = manager.getCameraCharacteristics(camID);
                if(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK){
                    camCharacterictics = cameraCharacteristics;
                    break;
                }
            }


            SurfaceView camSurface = getView().findViewById(R.id.camSurface);
            SurfaceControl sc = camSurface.getSurfaceControl();
            //CaptureRequest.Builder builder = manager


        }catch (CameraAccessException e){
            Log.e(LOG_TAG, "Can't access to camera");
        }
        catch (NullPointerException e){
            Log.e(LOG_TAG, "Camera device is missing");
        }
    }



    public Range<Integer>[] getFPSRanges(){
        Range<Integer>[] result = camCharacterictics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
        assert result != null;
        for(Range<Integer> currRange : result){
            Log.i(LOG_TAG, "Range: " + currRange);
        }
        return result;
    }


}
