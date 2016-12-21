package ldy.com.getyourscreen.utils;

import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import ldy.com.getyourscreen.R;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MEDIA_PROJECTION_SERVICE;

/**
 * Created by ldy on 16/12/21.
 */

public class ScreenRecordManager {
    private AppCompatActivity mActivity;
    private int RECORD_REQUEST_CODE = 100;
    private MediaProjection mediaProjection;
    private MediaProjectionManager projectionManager;
    private VirtualDisplay virtualDisplay;
    private MediaRecorder mediaRecorder;

    private String dir;
    private int width;
    private int height;
    private int dpi = 320;

    private boolean running = false;

    public ScreenRecordManager(AppCompatActivity activity) {
        mActivity = activity;
        init();
    }

    private void init() {
        projectionManager = (MediaProjectionManager) mActivity.getSystemService(MEDIA_PROJECTION_SERVICE);
        Intent captureIntent= projectionManager.createScreenCaptureIntent();
        mActivity.startActivityForResult(captureIntent, RECORD_REQUEST_CODE);
        mediaRecorder = new MediaRecorder();
        initRootDir();
    }

    private String getAppName() {
        String name;
        String[] strs = mActivity.getApplicationInfo().packageName.split("\\.");
        name = strs[strs.length-1];
        return name;
    }
    private void initRootDir() {
        dir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
                getAppName() + File.separator;

        File path = new File(dir);
        if (!path.exists()) {
            path.mkdir();
        }
    }

    private void initRecorder() {
        File file = new File(dir, getAppName()+"_"+System.currentTimeMillis() + ".mp4");
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(file.getAbsolutePath());
        mediaRecorder.setVideoSize(width, height);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setVideoEncodingBitRate(5 * 1024 * 1024);
        mediaRecorder.setVideoFrameRate(30);
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createVirtualDisplay() {
        virtualDisplay = mediaProjection.createVirtualDisplay(
                "MainScreen",
                width,
                height,
                dpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder.getSurface(),
                null, null);
    }

    public void forActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECORD_REQUEST_CODE && resultCode == RESULT_OK) {
            mediaProjection = projectionManager.getMediaProjection(resultCode, data);
        }
    }

    public void prepareScreenRecord(int width, int height) {
        this.width = width;
        this.height = height;
        initRecorder();
        createVirtualDisplay();
    }

    public boolean startRecord() {
        if (mediaProjection == null || running) {
            return false;
        }

        mediaRecorder.start();
        running = true;
        return true;
    }

    public void stopRecord() {
        mediaRecorder.stop();
        mediaProjection.stop();
        running = false;
    }

    public String getDir() {
        return dir;
    }
}
