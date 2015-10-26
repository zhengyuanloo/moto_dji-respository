package com.example.ndtw36.new_dji;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import dji.sdk.api.Camera.DJICameraDecodeTypeDef;
import dji.sdk.api.DJIDrone;
import dji.sdk.api.mediacodec.DJIVideoDecoder;
import dji.sdk.interfaces.DJIReceivedVideoDataCallBack;
import dji.sdk.widget.DjiGLSurfaceView;

public class TextureCameraActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    private static final String TAG = "TextureCameraActivity";
    private int DroneCode;
    private final int SHOWDIALOG = 1;
    private final int SHOWTOAST = 2;
    private final static int MSG_INIT_DECODER = 3;
    private int i = 0;
    private int TIME = 1000;
    private DJIReceivedVideoDataCallBack mReceivedVideoDataCallBack = null;

    private DJIVideoDecoder mVideoDecoder = null;
    private TextureView mVideoSurface;

    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INIT_DECODER:
                    Surface mSurface = (Surface)msg.obj;
                    initDecoder(mSurface);
                    break;
                case SHOWDIALOG:
                    Toast.makeText(TextureCameraActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case SHOWTOAST:
                    Toast.makeText(TextureCameraActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
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
        setContentView(R.layout.activity_texture_camera);

        mVideoSurface = (TextureView)findViewById(R.id.texture_surface);
        mVideoSurface.setSurfaceTextureListener(this);
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
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mVideoDecoder == null) {
            Surface mSurface  = new Surface(surface);
            handler.sendMessageDelayed(Message.obtain(handler, MSG_INIT_DECODER, mSurface), 200);
        } else {
            mVideoDecoder.setSurface(new Surface(surface));
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mVideoDecoder != null)
            mVideoDecoder.setSurface(null);
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

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
