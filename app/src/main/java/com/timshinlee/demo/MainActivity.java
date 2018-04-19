package com.timshinlee.demo;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.timshinlee.imagecapturemanager.ImageCaptureManager;

public class MainActivity extends AppCompatActivity {

    private ImageCaptureManager mManager;
    private ImageView mImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImage = findViewById(R.id.image);

        mManager = new ImageCaptureManager.Builder()
                .setAuthority("com.timshinlee.demo.provider")
                .setSavePublic(true)
                .build();
    }

    public void takePhoto(View view) {
        mManager.takePhoto(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final String imagePath = mManager.onActivityResult(requestCode, resultCode, data);
        if (null != imagePath) {
            mImage.setImageBitmap(BitmapFactory.decodeFile(imagePath));
            mManager.addPhoto2Gallery(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        mManager.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
}
