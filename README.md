# ImageCaptureManager

封装系统相机的调用

encapsulates the operations on system's default camera app.

# Install

1. add jitpack as a repository:

```gradle
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```

2. add dependency in module's build.gradle:

```gradle
dependencies {
    compile 'com.github.timshinlee:ImageCaptureManager:1.0.0'
}
```

# Preparation

1. require permission if needed

```xml
<manifest>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    <application></application>
</manifest>
```

2. create a path file in res/xml folder to set the path shown to other apps. For example, res/xml/file_paths.xml:

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <external-path
        name="my_images"
        path="."/>
</paths>
```

3. add a file provider in the manifest, set the `authorities` attribute to your own package, and set the path file as its resource:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="com.timshinlee.demo">
    
    <application>
        <provider
            android:name="android.support.v4.content.FileProvider"
            android:authorities="com.timshinlee.demo.provider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths"/>
        </provider>
    </application>
</manifest>
```

# Usage

1. init manager, remember to set the authority to access the file provider

```java
mManager = new ImageCaptureManager.Builder()
        .setAuthority("com.timshinlee.demo.provider")
        .setSavePublic(true)
        .build();
```

2. take photo when needed

```java
mManager.takePhoto(this);
```

3. deal with the result

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    final String imagePath = mManager.onActivityResult(requestCode, resultCode, data);
}
```

4. deal with permission if needed

```java
@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    mManager.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
}
```
