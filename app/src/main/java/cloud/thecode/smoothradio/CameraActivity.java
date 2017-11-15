package cloud.thecode.smoothradio;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOError;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.security.acl.Permission;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class CameraActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION_RESULT = 0;
    private static final int REQUEST_STORAGE_PERMISSION_RESULT = 1;
    private TextureView mTextureView;
    // Since textureViews take time to inflate I'm going to add
    // a textureview listener to get notified when its ready
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
           setupCamera(i, i1);
           connectCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    private CameraDevice mCameraDevice;
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method returns a cameraDevice which we will assign to our
            // Camera device we created
            mCameraDevice = cameraDevice;


            // Since connected we can now open camera and recieve preview
            startPreview();
        }
        @TargetApi(21)
        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            // Finished with the camera
            cameraDevice.close();
            mCameraDevice = null;
        }
        @TargetApi(21)
        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            mCameraDevice = null;
        }
    };

    private String mCameraId;
    private HandlerThread mBackgroundHadlerThread;
    private Handler mBackgroundHandler;
    private CaptureRequest.Builder mCaptureRequestBuiler;

    private ImageButton mRecord;
    private boolean mIsRecording = false;

    private File mVideoFolder;
    private String mVideoFileName;


    Vibrator v;

    /* APPLICATION METHODS */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);


        mTextureView = (TextureView) findViewById(R.id.textureView);
        mRecord = (ImageButton) findViewById(R.id.video_capture);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        mRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mIsRecording) {
                    mIsRecording = false;
                    //Change image to available
                    mRecord.setImageResource(R.drawable.not_recording);
                } else {
                    checkWriteStoragePermission();
                }
                // Vibrate
                v.vibrate(30);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        startBackgroundThread();

        if(mTextureView.isAvailable()) {
            // Get width and height
            setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
            connectCamera();
            startPreview();
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    protected void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();

    }

    int w, h;
    @TargetApi(21)
    private void setupCamera(int width, int height) {
        w = width;
        h = height;
        // GET THE CAMERA ID FOR US TO BE ABLE TO CONNECT TO THE RIGHT CAMERA
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        // Loop throught the cameras available
        try {
            for(String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                // check if it's back facing camera skip it, since we want the from facing one
                if(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING ) == CameraCharacteristics.LENS_FACING_BACK) {
                    continue;
                }
                // Else we found what we want so assign it to the camera id
                mCameraId = cameraId;
                return;
            }
        } catch(CameraAccessException camAccessEx) {
            // Couldn't access camera
        }


    }

    @TargetApi(21)
    private void connectCamera  () {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            // Check if it's marshmallow or later to access camera
            // If not just access camera without asking user for permissions
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                // Setup a permission check
                if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted
                    cameraManager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
                } else {
                    // popup permission
                    if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        Toast.makeText(this, "This feature requires access to camera", Toast.LENGTH_SHORT).show();
                    }
                    // The following will ask for permission and a callback will fire to return result using Overrided method
                    // OnRequestPermissionsResult below
                    requestPermissions(new String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_RESULT);
                }

            } else {
                cameraManager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
            }


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    @TargetApi(21)
    private void startPreview() {
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(w, h);
        Surface previewSurface = new Surface(surfaceTexture);

        try {
            mCaptureRequestBuiler = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuiler.addTarget(previewSurface);

            // Set up the capture session
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            try {
                                cameraCaptureSession.setRepeatingRequest(mCaptureRequestBuiler.build(), null, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            Toast.makeText(CameraActivity.this, "Something went wrong! Error 288", Toast.LENGTH_SHORT).show();
                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if(mCameraDevice != null) {
            mCameraDevice = null;
        }
    }


    private void startBackgroundThread() {
        mBackgroundHadlerThread = new HandlerThread("Camera");
        mBackgroundHadlerThread.start();

        mBackgroundHandler = new Handler(mBackgroundHadlerThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundHadlerThread.quitSafely();
        try {
            mBackgroundHadlerThread.join();
            mBackgroundHadlerThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Creating video folder
    private void createVideoFolder() {
        // Get where movie files get put
        File movieFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);

        // Create unique folder for my app to save videos called moments
        mVideoFolder = new File(movieFile, "moments");

        // Check if folder already exists, if not create
        if(!mVideoFolder.exists()) {
            mVideoFolder.mkdirs();
        }

    }

    private File createVideoFileName() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String prepend = "moment_" + timestamp + "_";

        File momentFile = File.createTempFile(prepend, ".mp4", mVideoFolder);

        mVideoFileName = momentFile.getAbsolutePath();

        return momentFile;
    }

    private void checkWriteStoragePermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                mIsRecording = true;
                mRecord.setImageResource(R.drawable.recording);
                try {
                    createVideoFileName();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "App requires saving videos to storage", Toast.LENGTH_LONG).show();
                }

                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION_RESULT);
            }

        } else {
            mIsRecording = true;
            mRecord.setImageResource(R.drawable.recording);

            try {
                createVideoFileName();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CAMERA_PERMISSION_RESULT) {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // Request rejected :(
                Toast.makeText(this, "Feature won't run without the camera", Toast.LENGTH_LONG).show();
            } else {
                this.recreate();
            }
        } else if(requestCode == REQUEST_STORAGE_PERMISSION_RESULT) {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // Oh no
                Toast.makeText(this, "Sorry, we really care about saving videos", Toast.LENGTH_SHORT).show();
            } else {
                this.recreate();;
            }
        }
    }
}
