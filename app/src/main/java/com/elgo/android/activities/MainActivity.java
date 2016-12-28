package com.elgo.android.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.elgo.lego.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by Abhish3k on 7/24/2016.
 */

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static final int REQUEST_EXTERNAL_STORAGE = 0;
    private static final String TAG = "Main";
    Button enter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final String secret = "123";

        final EditText secretCode = (EditText) findViewById(R.id.secret_code);
        enter = (Button) findViewById(R.id.enter);
        ImageButton imgButton = (ImageButton) findViewById(R.id.image_button);

        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (secretCode.getText().toString().equals(secret)) {
                    getSharedPreferences("userData", MODE_PRIVATE).edit().putBoolean("secret_code_entered", true).commit();

                    Intent i = new Intent(MainActivity.this, PreLoginActivity.class);
                    // Closing all the Activities
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    // Add new Flag to start new Activity
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Incorrect secret code. Try Again", Toast.LENGTH_LONG).show();
                }
            }
        });

        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askPermission();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        // Some permissions have been granted
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
        playVideo();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        // Some permissions have been denied
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        // (Optional) Check whether the user denied permissions and checked NEVER ASK AGAIN.
        // This will display a dialog directing them to enable the permission in app settings.
        EasyPermissions.checkDeniedPermissionsNeverAskAgain(this,
                getString(R.string.rationale_ask_again),
                R.string.setting, R.string.cancel, perms);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @AfterPermissionGranted(REQUEST_EXTERNAL_STORAGE)
    public void askPermission() {
        if ((EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) && EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // Have permission, do the thing!
            playVideo();
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_permission),
                    REQUEST_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_permission),
                    REQUEST_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);

        }
    }

    // Code for playing video
    private void playVideo() {
        final InputStream in = getResources().openRawResource(R.raw.sample_video);
        new AsyncTask<Void, Void, Void>() {
            File outDir = new File(Environment.getExternalStorageDirectory() + "/" + getPackageName() + "/.temp");
            File outFile = new File(outDir, "lego_video.mp4");

            @Override
            protected void onPostExecute(Void result) {
                Uri uri = Uri.fromFile(outFile);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "video/mp4");
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Log.d(TAG, "Error : " + e.getMessage());
                    Toast.makeText(MainActivity.this, "Error playing the video", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            protected Void doInBackground(Void... params) {
                if (!outFile.exists()) {
                    outDir.mkdirs();
                    OutputStream out;
                    try {
                        out = new FileOutputStream(outFile);
                        copyFile(in, out);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        }.execute();

    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    // End Play video code
}
