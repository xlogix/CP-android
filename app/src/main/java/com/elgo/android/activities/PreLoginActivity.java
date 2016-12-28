package com.elgo.android.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

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

public class PreLoginActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final String TAG = "PreLogin";
    private static final int REQUEST_EXTERNAL_STORAGE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.elgo.lego.R.layout.activity_pre_login);

        ImageButton imgButton = (ImageButton) findViewById(com.elgo.lego.R.id.image_button);
        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askPermissionVideo();
            }
        });

        FloatingActionButton freeResources = (FloatingActionButton) findViewById(com.elgo.lego.R.id.free_resources);
        freeResources.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askPermissionPDF();
            }
        });

        FloatingActionButton continueLogin = (FloatingActionButton) findViewById(com.elgo.lego.R.id.continue_login);
        continueLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(PreLoginActivity.this, AuthActivity.class);
                startActivity(i);
                finish();
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
        openPDF();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        // Some permissions have been denied
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        // (Optional) Check whether the user denied permissions and checked NEVER ASK AGAIN.
        // This will display a dialog directing them to enable the permission in app settings.
        EasyPermissions.checkDeniedPermissionsNeverAskAgain(this,
                getString(com.elgo.lego.R.string.rationale_ask_again),
                com.elgo.lego.R.string.setting, com.elgo.lego.R.string.cancel, perms);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @AfterPermissionGranted(REQUEST_EXTERNAL_STORAGE)
    public void askPermissionPDF() {
        if ((EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) && EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // Have permission, do the thing!
            openPDF();
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(this, getString(com.elgo.lego.R.string.rationale_permission),
                    REQUEST_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            EasyPermissions.requestPermissions(this, getString(com.elgo.lego.R.string.rationale_permission),
                    REQUEST_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);

        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @AfterPermissionGranted(REQUEST_EXTERNAL_STORAGE)
    public void askPermissionVideo() {
        if ((EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) && EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // Have permission, do the thing!
            playVideo();
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(this, getString(com.elgo.lego.R.string.rationale_permission),
                    REQUEST_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            EasyPermissions.requestPermissions(this, getString(com.elgo.lego.R.string.rationale_permission),
                    REQUEST_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);

        }
    }

    private void openPDF() {
        final InputStream in = getResources().openRawResource(com.elgo.lego.R.raw.sample_pdf);
        new AsyncTask<Void, Void, Void>() {
            File outDir = new File(Environment.getExternalStorageDirectory() + "/" + getPackageName() + "/.temp");
            File outFile = new File(outDir, "lego_pdf.pdf");

            @Override
            protected void onPostExecute(Void result) {
                Uri uri = Uri.fromFile(outFile);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setType("application/pdf");
                PackageManager packageManager = getPackageManager();
                List list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                if (list.size() > 0 && outFile.isFile()) {
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, "application/pdf");
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.d(TAG, "Error : " + e.getMessage());
                        Toast.makeText(PreLoginActivity.this, "Error opening the file", Toast.LENGTH_LONG).show();
                    }
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

    // Code for playing video
    private void playVideo() {
        final InputStream in = getResources().openRawResource(com.elgo.lego.R.raw.sample_video);
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
                    Toast.makeText(PreLoginActivity.this, "Error playing the video", Toast.LENGTH_LONG).show();
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
    // End Play video code
}
