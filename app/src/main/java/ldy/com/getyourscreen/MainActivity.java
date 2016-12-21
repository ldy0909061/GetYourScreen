package ldy.com.getyourscreen;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import ldy.com.getyourscreen.utils.ScreenRecordManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private String tag = MainActivity.class.getSimpleName();
    private Button btnStart;
    private Button btnFinish;
    private Button btnDir;
    private LinearLayout llTop;

    private ScreenRecordManager srm;

    private String [] mLocationNeededPermission = new String[] {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        llTop = (LinearLayout) findViewById(R.id.ll_top);
        btnDir = (Button) findViewById(R.id.btn_dir);
        btnStart = (Button) findViewById(R.id.btn_start);
        btnFinish = (Button) findViewById(R.id.btn_finish);
        btnStart.setOnClickListener(this);
        btnFinish.setOnClickListener(this);
        btnDir.setOnClickListener(this);

        requestPermission(mLocationNeededPermission);

        srm = new ScreenRecordManager(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        srm.forActivityResult(requestCode, resultCode, data);
    }

    private void opendir() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        File file = new File(srm.getDir());
        intent.setDataAndType(Uri.fromFile(file), "*/*");
        startActivity(Intent.createChooser(intent, "Open folder"));
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                srm.prepareScreenRecord(llTop.getRight(), llTop.getBottom());
                srm.startRecord();
                break;
            case R.id.btn_finish:
                srm.stopRecord();
                break;
            case R.id.btn_dir:
                opendir();
                break;
        }
    }

    private void requestPermission(String needPermission[]) {
        ArrayList<String> requestedPermission = new ArrayList<String>();
        for (int i = 0; i < needPermission.length; i++) {
            if (ContextCompat.checkSelfPermission(this, needPermission[i])
                    != PackageManager.PERMISSION_GRANTED) {
                requestedPermission.add(needPermission[i]);
            }
        }

        if (requestedPermission.size() > 0) {
            String permissions[] = new String[requestedPermission.size()];
            for (int i = 0; i < permissions.length; i++) {
                permissions[i] = requestedPermission.get(i);
            }

            ActivityCompat.requestPermissions(this, permissions,
                    1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        doNext(requestCode,grantResults);
    }

    protected void doNext(int requestCode, int[] grantResults) {
        if (requestCode == 1) {
            boolean hasDenied = false;
            if (grantResults.length <= 0) {
                hasDenied = true;
            }
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    hasDenied = true;
                    break;
                }
            }
            if (hasDenied) {
                Toast.makeText(this, "权限不足,可能影响某些功能使用",
                        Toast.LENGTH_LONG).show();
            }

        }
    }
}
