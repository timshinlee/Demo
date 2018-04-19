package com.timshinlee.imagecapturemanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.app.Activity.RESULT_OK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * 封装调用系统相机。
 * <p>
 * 步骤如下：
 * <pre>
 * 1. 使用Builder创建实例
 * 2. 调用实例的takePhoto()方法拍照
 * 3. 在Activity的onActivityResult()中调用实例的同名方法进行处理
 * 4. 如果涉及读写外部权限问题，可以在Activity的onRequestPermissionsResult()中调用实例的同名方法
 * </pre>
 */
public class ImageCaptureManager {
    private String imagePath;
    private String authority;
    private File storageDir;
    private boolean savePublic;
    private String fileNameFormat = "yyyyMMdd_HHmmss";
    private int requestCodeImageCapture;
    private int requestCodeWriteExternalPermission;
    private static final String TAG = "ImageCaptureManager";

    private ImageCaptureManager() {
        requestCodeImageCapture = new Random().nextInt(Short.MAX_VALUE);
        requestCodeWriteExternalPermission = new Random().nextInt(Short.MAX_VALUE);
    }

    public static final class Builder {
        private final ImageCaptureManager mManager;

        public Builder() {
            mManager = new ImageCaptureManager();
        }

        /**
         * 设置FileProvider权限字符串
         */
        public Builder setAuthority(String authority) {
            mManager.authority = authority;
            return this;
        }

        /**
         * 设置时间格式字符串
         */
        public Builder setFileNameFormat(String format) {
            mManager.fileNameFormat = format;
            return this;
        }

        /**
         * 设置保存到公共照片目录，还是应用私有照片目录，默认后者
         *
         * @param savePublic default false
         */
        public Builder setSavePublic(boolean savePublic) {
            mManager.savePublic = savePublic;
            return this;
        }

        public ImageCaptureManager build() {
            if (mManager.authority == null) {
                throw new IllegalArgumentException("You need to call setAuthority().");
            }
            return mManager;
        }
    }

    public void takePhoto(Activity activity) {
        if (ActivityCompat.checkSelfPermission(activity, WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{WRITE_EXTERNAL_STORAGE}, requestCodeWriteExternalPermission);
        } else {
            takePhotoWhenPermitted(activity, requestCodeImageCapture);
        }
    }

    private void takePhotoWhenPermitted(Activity activity, int requestCode) {
        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            File imageFile = createImageFile(activity);
            if (null != imageFile) {
                // 因为7.0开始跨应用传递file类型uri会导致FileUriExposedException，需要使用FileProvider
                final Uri photoUri = FileProvider.getUriForFile(activity, authority, imageFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                activity.startActivityForResult(intent, requestCode);
            } else {
                Toast.makeText(activity, "图片文件创建失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 检测拍照结果
     *
     * @return 图像路径
     */
    public String onActivityResult(int requestCode, int resultCode, Intent data) {
        String imagePath = null;
        if (requestCodeImageCapture == requestCode && RESULT_OK == resultCode) {
            imagePath = getImagePath();
        }
        return imagePath;
    }

    public void onRequestPermissionsResult(Activity activity, int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == requestCodeWriteExternalPermission && grantResults.length > 0) {
            if (grantResults[0] == PERMISSION_GRANTED) {
                takePhotoWhenPermitted(activity, requestCodeImageCapture);
            }
        }
    }

    /**
     * 创建空的图像文件
     */
    private File createImageFile(Context context) {
        File image = null;
        final String imageFileName = new SimpleDateFormat(fileNameFormat,
                Locale.SIMPLIFIED_CHINESE).format(new Date().getTime());
        if (storageDir == null) {
            if (savePublic) {
                storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            } else {
                storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            }
        }
        try {
            image = File.createTempFile(imageFileName, ".jpg", storageDir);
            imagePath = image.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "createImageFile: ", e);
        }
        return image;
    }

    /**
     * 获取当前所拍照片路径
     */
    public String getImagePath() {
        return imagePath;
    }

    /**
     * 将当前所拍照片加入到系统相册，注意照片必须在公共目录下才能被系统扫描到
     */
    public void addPhoto2Gallery(Context context) {
        final Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        final File file = new File(imagePath);
        final Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        context.sendBroadcast(intent);
    }
}
