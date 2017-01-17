package camview.preview.com.camview;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Toast;

public class FloatingCamera_Front extends Service{

    WindowManager windowManager;
    ViewGroup vG;
    Context context;

    Camera mCamera;
    PreviewSelfie mPreview;
    Button capture, btnclose, sw;
    Spinner spinner;

    byte[] pictureBytes;

    String mCurrentPhotoPath;
    private static final String TAG = "Floating_Camera";

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        int H = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 260, this.getResources().getDisplayMetrics());
        int w = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 320, this.getResources().getDisplayMetrics());

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                w,
                H,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 10;
        params.y = 100;

        //Define View
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        vG = (ViewGroup)layoutInflater.inflate(R.layout.camera_front, null, false);
        windowManager.addView(vG, params);

        capture = (Button)vG.findViewById(R.id.capture);
        btnclose = (Button)vG.findViewById(R.id.close);
        sw = (Button)vG.findViewById(R.id.sw);

        mCamera = getCameraInstance();
        mPreview = new PreviewSelfie(this, mCamera);
        FrameLayout preview = (FrameLayout) vG.findViewById(R.id.previewFrame);
        preview.addView(mPreview);

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capture.setEnabled(false);

                Capture take = new Capture();
                take.execute();
            }
        });

        btnclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(getApplicationContext(), FloatingCamera_Front.class));
            }
        });

        sw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(getApplicationContext(), FloatingCamera_Front.class));

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent s = new Intent(FloatingCamera_Front.this, SwitchCamera.class);
                        s.putExtra("sw", "toBack");
                        s.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(s);
                    }
                }, 50);
            }
        });

        try {
            vG.setOnTouchListener(new View.OnTouchListener() {
                private WindowManager.LayoutParams paramsF = params;
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;

                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            System.out.println("Touch ACTION_DOWN");

                            initialX = paramsF.x;
                            initialY = paramsF.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_UP:
                            System.out.println("Touch ACTION_UP");

                            break;
                        case MotionEvent.ACTION_MOVE:
                            System.out.println("Touch ACTION_MOVE");

                            paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
                            paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
                            windowManager.updateViewLayout(vG, paramsF);
                            break;
                    }
                    return false;
                }
            });
        }
        catch (Exception e) {
            System.out.println(e);
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        System.out.println("FloatingCamera");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (vG != null) {
            windowManager.removeView(vG);
            stopService(new Intent(getApplicationContext(), FloatingCamera_Back.class));
            releaseCamera();

            System.out.println("Channel FloatingCamera Reset");
        }
    }
    /***********---------------------------------------------------------------------------------------*************/
    /***********---------------------------------------------------------------------------------------*************/
    /***********---------------------------------------------------------------------------------------*************/
    /***********---------------------------------------------------------------------------------------*************/
    /***********---------------------------------------------------------------------------------------*************/
    public static final int MEDIA_TYPE_IMAGE = 1;

    private File getOutputMediaFile(int type){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Floating_Camera");
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("CameraW", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String timeStampshort = new SimpleDateFormat("_HHmmss").format(new Date());
        File mediaFile = null;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp);
        }
        else {
            return null;
        }

        mCurrentPhotoPath = mediaFile.getAbsolutePath();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                galleryAddPic();
            }
        }, 300);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                galleryAddPicPort();
            }
        }, 300);

        return mediaFile;
    }

    private PictureCallback mPicture = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                //

                return;
            }

/***/  // NOTE: Handle the image rotation after saving the file.
///////////////////////////////////Portrait
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                //	 	BitmapFactory.Options options = new BitmapFactory.Options();
                //	 	options.inSampleSize = 2;

                Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
                int w = bm.getWidth();
                int h = bm.getHeight();

                Matrix mtx = new Matrix();
                mtx.postRotate(-90);
                mtx.setRotate(-90);
                bm = Bitmap.createBitmap(bm, 0, 0, w, h, mtx, true);

                //byte[] pictureBytes;
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bm.compress(CompressFormat.JPEG, 100, bos);
                pictureBytes = bos.toByteArray();

                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(pictureBytes);
                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.d(TAG, "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d(TAG, "Error accessing file: " + e.getMessage());
                } finally {
                    mCamera.startPreview();
                }
            }
/////////////////////////////////////Landscape
            else{
                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.d(TAG, "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d(TAG, "Error accessing file: " + e.getMessage());
                }finally {
                    mCamera.startPreview();
                }
            }
        }
    };

    AutoFocusCallback autoFocusCB = new AutoFocusCallback(){
        @Override
        public void onAutoFocus(boolean arg0, Camera arg1) {
            //  focus.setEnabled(true);
        }
    };

    public class Capture extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {

            System.out.println("Focused PreEXE");
        }

        @Override
        protected Void doInBackground(Void... params) {
            //Capture
            mCamera.takePicture(null, null, mPicture);

            System.out.println("Captured doInBackground");
            return null;
        }

        @Override
        protected void onPostExecute(Void res) {
            //startPreview
            try{
                mCamera.startPreview();
            }
            catch(Exception e){
                System.out.println("ERROR: Restart App\n" + e);
            }
            finally{
                //Notify
                Toast.makeText(getApplicationContext(), "CAPTURED", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Shutter Button
                        capture.setEnabled(true);
                    }
                }, 750);

                System.out.println("StartPre PostEXE");
            }
        }
    }
    /****************************************/
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(1);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return c;
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public void setRatio(Camera Cam, int P) throws Exception{
        Camera.Parameters params = Cam.getParameters();

        List<Camera.Size> picSizes = params.getSupportedPictureSizes();
        int H = picSizes.get(P).height;		System.out.println("Height: " + H);
        int W = picSizes.get(P).width;		System.out.println("Width: " + W);

        params.setPictureSize(W, H);
        Cam.setParameters(params);
    }

    private void galleryAddPic() {
        File temp = new File(mCurrentPhotoPath);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            String NewFile = mCurrentPhotoPath + ".JPEG";
            File New = new File(NewFile);

            temp.renameTo(New);

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(NewFile);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            getApplicationContext().sendBroadcast(mediaScanIntent);
        }
    }
    private void galleryAddPicPort(){
        File temp = new File(mCurrentPhotoPath);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            final String NewFile = mCurrentPhotoPath + ".JPEG";
            File New = new File(NewFile);

            temp.renameTo(New);

            Intent mediaScanIntentPort = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File fPort = new File(NewFile);
            Uri contentUriPort = Uri.fromFile(fPort);
            mediaScanIntentPort.setData(contentUriPort);
            getApplicationContext().sendBroadcast(mediaScanIntentPort);
        }
    }
}

