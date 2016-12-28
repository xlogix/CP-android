package com.elgo.android.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by Abhish3k on 7/26/2016. //Implemented 'on back' pressed
 */

public class LastActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private EditText mFeedBack;
    //private Boolean exit = false;
    private static final String TAG = "Storage";
    private ProgressDialog mProgressDialog;
    private Uri mFileUri = null;
    private StorageReference mStorageRef;
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private static final int REQUEST_EXTERNAL_STORAGE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.elgo.lego.R.layout.activity_last);
        mFeedBack = (EditText) findViewById(com.elgo.lego.R.id.feedback);

        Button sendFeedback = (Button) findViewById(com.elgo.lego.R.id.send_feedback);
        sendFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askPermission();
                uploadFromUri(mFileUri);
            }
        });

        mAuth = FirebaseAuth.getInstance();

        mUser = mAuth.getCurrentUser();

        mStorageRef = FirebaseStorage.getInstance().getReference();

    }

    // Start UI definitions
    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
    // End UI Definitions

    /*@Override
    public void onBackPressed() {
        if (exit) {
            finish();
        } else {
            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);
        }
    }*/

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
        writeToFile();
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
    public void askPermission() {
        if ((EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) && EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // Have permission, do the thing!
            writeToFile();
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(this, getString(com.elgo.lego.R.string.rationale_permission),
                    REQUEST_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            EasyPermissions.requestPermissions(this, getString(com.elgo.lego.R.string.rationale_permission),
                    REQUEST_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);

        }
    }

    public void writeToFile() {
        try {
            FileOutputStream fileOut = openFileOutput("user_" + mUser.getUid() + ".txt", MODE_PRIVATE);
            OutputStreamWriter outputWriter = new OutputStreamWriter(fileOut);
            outputWriter.write("Feedback : " + mFeedBack.getText().toString());
            outputWriter.write(getSharedPreferences("userData", MODE_PRIVATE).getString("Name : ", "Empty"));
            outputWriter.write(getSharedPreferences("userData", MODE_PRIVATE).getString("Registration No : ", "Empty"));
            outputWriter.close();
            mFileUri = Uri.fromFile(getFileStreamPath("user_" + mUser.getUid() + ".txt"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadFromUri(Uri fileUri) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        final StorageReference fileRef = mStorageRef.child("files")
                .child(fileUri.getLastPathSegment());

        // uploads file to Firebase Storage
        showProgressDialog();
        Log.d(TAG, "uploadFromUri:dst:" + fileRef.getPath());
        fileRef.putFile(fileUri)
                .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // uploads succeeded
                        Log.d(TAG, "uploadFromUri:onSuccess");
                        hideProgressDialog();
                        Toast.makeText(LastActivity.this, "Thank You!", Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // uploads failed
                        Log.w(TAG, "uploadFromUri:onFailure", exception);
                        hideProgressDialog();
                        Toast.makeText(LastActivity.this, "Error: upload failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
