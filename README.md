#AndroidCamviewApp
This is android floating camera view service application.
=====================
<p>
Camera surface view is showing on Android Home screen as service.
</p>
  - [Features](#features)
  
  - [Getting Started](#getting-started)
 
  - [Important](#important)
  
  - [Dependencies](#dependencies)

### Features
	-Camera Service (FloatingCamera_Back,FloatingCamera_Front)
	-Camera SurfaceView(SurfaceView,PreviewSelfie)
### Getting started
    You need to start  **FloatingCamera_Back**Service.
```java
    Intent s = new Intent(MainActivity.this, FloatingCamera_Back.class);
        s.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(s);
```
### Important
    -FloatingCamera_Back,FloatingCamera_Front as camera service
```java
    Camera mCamera;
    Preview mPreview;
    mCamera = getCameraInstance();
    mPreview = new Preview(this, mCamera);
    FrameLayout preview = (FrameLayout) vG.findViewById(R.id.previewFrame);
    preview.addView(mPreview);
```
    -Preview,PreviewSelfie as Camera SurfaceView
```java
	 private static int getCameraDisplayOrientation(int displayRotation, int cameraId) {
        Log.d(TAG, "getCameraDisplayOrientation");
        CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + displayRotation) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - displayRotation + 360) % 360;
        }
        return result;
    }
```
### Dependencies
    compile fileTree(dir: 'libs', include: ['*.jar'])
    testCompile 'junit:junit:4.12'
    compile 'com.android.support:appcompat-v7:23.4.0'
