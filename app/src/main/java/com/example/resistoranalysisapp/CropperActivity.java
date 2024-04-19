package com.example.resistoranalysisapp;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.UUID;

public class CropperActivity extends AppCompatActivity {

    String result;
    Uri fileUri;
    View overlayView;
    int bandNum;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cropper);

        readIntent();

        // Ask user for permission for overlay
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 500);
        }

        // Setup and options for UCrop Activity
        String dest_uri=new StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString();
        UCrop.Options options=new UCrop.Options();
        options.setSaturationEnabled(false);
        options.setBrightnessEnabled(false);
        options.setContrastEnabled(false);
        options.setSharpnessEnabled(false);
        options.setFreeStyleCropEnabled(true);

        // Start Instance of UCrop Activity
        UCrop.of(fileUri, Uri.fromFile(new File(getCacheDir(), dest_uri)))
                .withOptions(options)
                .withMaxResultSize(2000, 2000)
                .start(CropperActivity.this);

        // Inflate and set parameters for cropper overlay
        if (bandNum == 5) {
            overlayView = LayoutInflater.from(this).inflate(R.layout.cropper_overlay, null);
        }
        else if (bandNum == 6) {
            overlayView = LayoutInflater.from(this).inflate(R.layout.cropper_6_overlay, null);
        }
        else {
            overlayView = LayoutInflater.from(this).inflate(R.layout.cropper_4_overlay, null);
        }

        WindowManager windowManager = getWindowManager();
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT
        );

        // Set the translucency of the overlay:
        layoutParams.alpha = 0.7f;

        // Wait for cropper to come up before displaying overlay
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            // Add view to WindowManager
            public void run() {
                windowManager.addView(overlayView, layoutParams);
            }
        }, 1000);
    }

    private RelativeLayout findRelativeLayout(ViewGroup viewGroup) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof RelativeLayout) {
                return (RelativeLayout) child;
            } else if (child instanceof ViewGroup) {
                RelativeLayout relativeLayout = findRelativeLayout((ViewGroup) child);
                if (relativeLayout != null) {
                    return relativeLayout;
                }
            }
        }
        return null;
    }

    private void readIntent() {
        Intent intent=getIntent();
        if(intent.getExtras() != null)
        {
            result=intent.getStringExtra("DATA");
            fileUri = Uri.parse(result);
            bandNum = intent.getIntExtra("bandNum", 5);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Remove overlay view when activity is destroyed
        getWindowManager().removeView(overlayView);

        // Wait for overlay to be destroyed before shutting down UCrop instance
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            // Add view to WindowManager
            public void run() {

                if(resultCode==RESULT_OK && requestCode==UCrop.REQUEST_CROP) {
                    final Uri resultUri=UCrop.getOutput(data);
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("RESULT", resultUri+"");
                    setResult(-1,returnIntent);
                    finish();
                }
                else if (resultCode==UCrop.RESULT_ERROR) {
                    Log.d("Cropper Error", String.valueOf(UCrop.getError(data)));
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("RESULT", "");
                    setResult(-2, returnIntent);
                    finish();
                }
                else {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("RESULT", "");
                    setResult(-2, returnIntent);
                    finish();
                }
            }
        }, 500);

    }
}
