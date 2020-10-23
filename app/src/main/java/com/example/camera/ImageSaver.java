package com.example.camera;

import android.media.Image;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ImageSaver implements Runnable {

    private final Image m_image;
    private final String LOG_TAG = "ImageSaver";
    private final File m_file;

    public ImageSaver(Image image, File file){
        m_file = file;
        m_image = image;
    }

    @Override
    public void run() {
        if(m_file == null)
            return;
        ByteBuffer buffer = m_image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(m_file);
            output.write(bytes);
            Log.i(LOG_TAG, "Image saved");
        } catch (IOException e) {
            Log.e(LOG_TAG, "IO_EXCEPTION");
            e.printStackTrace();
        }finally {
            m_image.close();
            if(output != null){
                try {
                    output.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Can't close output");
                    e.printStackTrace();
                }
            }
        }
    }
}
