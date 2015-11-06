package com.example.ndtw36.new_dji;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import dji.sdk.api.Camera.DJICameraDecodeTypeDef;
import dji.sdk.api.DJIDrone;
import dji.sdk.api.mediacodec.DJIVideoDecoder;
import dji.sdk.interfaces.DJIReceivedVideoDataCallBack;
import dji.sdk.widget.DjiGLSurfaceView;

public class SurfaceViewCameraActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final String TAG = "SurfaceViewCameraActivity";
    private final int SHOWDIALOG = 1;
    private final int SHOWTOAST = 2;
    private final static int MSG_INIT_DECODER = 3;
    private int i = 0;
    private int TIME = 1000;
    private DJIReceivedVideoDataCallBack mReceivedVideoDataCallBack = null;

    SurfaceView surfaceView;
    private DJIVideoDecoder mVideoDecoder = null;

    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INIT_DECODER:
                    Surface mSurface = (Surface)msg.obj;
                    initDecoder(mSurface);
                    break;
                case SHOWDIALOG:
                    Toast.makeText(SurfaceViewCameraActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case SHOWTOAST:
                    Toast.makeText(SurfaceViewCameraActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
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
            // handler自带方法实现定时器
            try {

                handlerTimer.postDelayed(this, TIME);

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface_view_camera);

        surfaceView=(SurfaceView)findViewById(R.id.surfaceView);
        surfaceView.getHolder().addCallback(this);

        final String mPath = Environment.getExternalStorageDirectory().toString() + "/surfaceViewDjiImage.jpeg";


        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                surfaceView.setDrawingCacheEnabled(true);
                Bitmap bitmap= Bitmap.createBitmap(surfaceView.getDrawingCache());

                OutputStream fout = null;
                File imageFile = new File(mPath);

                try {
                    fout = new FileOutputStream(imageFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
                    fout.flush();
                    fout.close();

                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(SurfaceViewCameraActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(SurfaceViewCameraActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mVideoDecoder == null) {
            Surface mSurface  = holder.getSurface();
            handler.sendMessageDelayed(Message.obtain(handler, MSG_INIT_DECODER, mSurface), 200);
        } else {
            mVideoDecoder.setSurface(holder.getSurface());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    private void initDecoder(Surface surface) {
        DJIDrone.getDjiCamera().setDecodeType(DJICameraDecodeTypeDef.DecoderType.Hardware);
        mVideoDecoder = new DJIVideoDecoder(this, surface);

        mReceivedVideoDataCallBack = new DJIReceivedVideoDataCallBack(){

            @Override
            public void onResult(byte[] videoBuffer, int size)
            {
                DJIDrone.getDjiCamera().sendDataToDecoder(videoBuffer,size);
            }
        };

        DJIDrone.getDjiCamera().setReceivedVideoDataCallBack(mReceivedVideoDataCallBack);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mVideoDecoder != null)
            mVideoDecoder.setSurface(null);
    }

    @Override
    protected void onStop()
    {
        // TODO Auto-generated method stub
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        // TODO Auto-generated method stub
        try
        {
            DJIDrone.getDjiCamera().setReceivedVideoDataCallBack(null);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (mVideoDecoder != null) {
            mVideoDecoder.stopVideoDecoder();
            mVideoDecoder = null;
        }

        super.onDestroy();
    }

    public void onReturn(View view){
        Log.d(TAG, "onReturn");
        this.finish();
    }
}
