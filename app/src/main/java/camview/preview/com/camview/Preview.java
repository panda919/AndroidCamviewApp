package camview.preview.com.camview;

import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class Preview extends SurfaceView implements SurfaceHolder.Callback{

    private static final String TAG = "Preview";
    private SurfaceHolder mHolder;
    private Camera mCamera;

    public Preview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        //    sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        /*****************************
         *
         * ****************************/
        // set preview size and make any resize, rotate or
        // reformatting changes here

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){

            int cameraId = 1;
            int displayRotation = 0;
            int displayOrientation = getCameraDisplayOrientation(displayRotation, cameraId);
            mCamera.setDisplayOrientation(displayOrientation);
        }
        else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){

            int cameraId = 1;
            int displayRotation = 90;
            int displayOrientation = getCameraDisplayOrientation(displayRotation, cameraId);
            mCamera.setDisplayOrientation(displayOrientation);
        }
        else {
            Toast.makeText(getContext(), "Unknown Orientation", Toast.LENGTH_SHORT).show();
        }

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            //
        }
    }

    /** Determine and return the current camera display orientation */
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
}
