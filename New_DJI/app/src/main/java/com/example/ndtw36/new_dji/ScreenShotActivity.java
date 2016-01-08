package com.example.ndtw36.new_dji;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import dji.sdk.api.Camera.DJICameraDecodeTypeDef;
import dji.sdk.api.Camera.DJICameraSystemState;
import dji.sdk.api.DJIDrone;
import dji.sdk.interfaces.DJICameraSystemStateCallBack;
import dji.sdk.interfaces.DJIReceivedVideoDataCallBack;
import dji.sdk.widget.DjiGLSurfaceView;


//Having a screenshot activity
public class ScreenShotActivity extends AppCompatActivity {

    private static final String TAG = "ScreenShotActivity";
    private final int SHOWDIALOG = 1;
    private final int SHOWTOAST = 2;
    private int i = 0;
    private int TIME = 1000;
    private DjiGLSurfaceView mDjiGLSurfaceView;
    private DJIReceivedVideoDataCallBack mReceivedVideoDataCallBack = null;
    private Button btn;
    private TextView tvTime;

    private static final int        REQUEST_CODE= 100;
    private static MediaProjection MEDIA_PROJECTION;
    private static String           STORE_DIRECTORY;
    private static int              IMAGES_PRODUCED;

    private MediaProjectionManager mProjectionManager;
    private ImageReader mImageReader;
    private Handler mHandler;
    private int                     mWidth;
    private int                     mHeight;

    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SHOWTOAST:
                    Toast.makeText(ScreenShotActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            return false;
        }
    });


    private Handler handlerTimer = new Handler();
    Runnable runnable = new Runnable(){
        @Override
        public void run() {
            try {

                handlerTimer.postDelayed(this, TIME);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_screen_shot);

        tvTime=(TextView)findViewById(R.id.tvTime);
        btn=(Button)findViewById(R.id.btn_start);
        tvTime.setVisibility(View.GONE);
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mHandler = new Handler();
                Looper.loop();
            }
        }.start();

        DJIDrone.getDjiCamera().setDecodeType(DJICameraDecodeTypeDef.DecoderType.Software);

        mDjiGLSurfaceView = (DjiGLSurfaceView)findViewById(R.id.DjiSurfaceScreenShot);

        mDjiGLSurfaceView.start();

        mReceivedVideoDataCallBack = new DJIReceivedVideoDataCallBack(){

            @Override
            public void onResult(byte[] videoBuffer, int size)
            {

                passVideoDJI(videoBuffer,size);
                mDjiGLSurfaceView.setDataToDecoder(videoBuffer, size);
            }


        };

        DJIDrone.getDjiCamera().setDjiCameraSystemStateCallBack(new DJICameraSystemStateCallBack() {

            @Override
            public void onResult(DJICameraSystemState state) {

                if (state.isTakingContinusPhoto) {
                    handler.sendMessage(handler.obtainMessage(SHOWTOAST, "isTakingContinuousPhoto"));
                }
            }
        });

        DJIDrone.getDjiCamera().setReceivedVideoDataCallBack(mReceivedVideoDataCallBack);

        btn.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
                btn.setVisibility(View.GONE);
                tvTime.setVisibility(View.VISIBLE);
            }
        });

        mDjiGLSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopProjection();
                btn.setVisibility(View.VISIBLE);
                tvTime.setVisibility(View.GONE);
            }
        });
    }

    private void passVideoDJI(byte[] videoBuffer, int size) {
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = null;
            FileOutputStream fos = null;
            Bitmap bitmap = null;

            try {
                image = mImageReader.acquireLatestImage();
                if (image != null) {
                    Image.Plane[] planes = image.getPlanes();
                    ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * mWidth;

                    // create bitmap
                    bitmap = Bitmap.createBitmap(mWidth + rowPadding / pixelStride, mHeight, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);

                    // write bitmap to a file
                    fos = new FileOutputStream(STORE_DIRECTORY + "/myscreen_" + IMAGES_PRODUCED + ".png");
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

                    IMAGES_PRODUCED++;
                    tvTime.setText(IMAGES_PRODUCED);
                    Log.e(TAG, "captured image: " + IMAGES_PRODUCED);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos!=null) {
                    try {
                        fos.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }

                if (bitmap!=null) {
                    bitmap.recycle();
                }

                if (image!=null) {
                    image.close();
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            MEDIA_PROJECTION = mProjectionManager.getMediaProjection(resultCode, data);

            if (MEDIA_PROJECTION != null) {
                STORE_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/screenshots/";
                File storeDirectory = new File(STORE_DIRECTORY);
                if(!storeDirectory.exists()) {
                    boolean success = storeDirectory.mkdirs();
                    if(!success) {
                        Log.e(TAG, "failed to create file storage directory.");
                        return;
                    }
                }

                DisplayMetrics metrics = getResources().getDisplayMetrics();
                int density = metrics.densityDpi;
                int flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                mWidth = size.x;
                mHeight = size.y;

                mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
                MEDIA_PROJECTION.createVirtualDisplay("screencap", mWidth, mHeight, density, flags, mImageReader.getSurface(), null, mHandler);
                mImageReader.setOnImageAvailableListener(new ImageAvailableListener(), mHandler);
            }
        }
    }

    private void stopProjection() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(MEDIA_PROJECTION != null) MEDIA_PROJECTION.stop();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopProjection();
    }
}

