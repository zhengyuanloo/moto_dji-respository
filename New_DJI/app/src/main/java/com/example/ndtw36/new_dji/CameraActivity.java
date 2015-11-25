package com.example.ndtw36.new_dji;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.*;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import dji.sdk.api.Camera.DJICameraDecodeTypeDef;
import dji.sdk.api.Camera.DJICameraSettingsTypeDef;
import dji.sdk.api.Camera.DJICameraSystemState;
import dji.sdk.api.DJIDrone;
import dji.sdk.api.DJIError;
import dji.sdk.interfaces.DJICameraSystemStateCallBack;
import dji.sdk.interfaces.DJIExecuteResultCallback;
import dji.sdk.interfaces.DJIReceivedVideoDataCallBack;
import dji.sdk.widget.DjiGLSurfaceView;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "CameraActivity";
    private int DroneCode;
    private final int SHOWDIALOG = 1;
    private final int SHOWTOAST = 2;
    private final int STOP_RECORDING = 10;
    private Button captureAction, recordAction, captureMode;
    private TextView viewTimer;
    private int i = 0;
    private int TIME = 1000;
    private DjiGLSurfaceView mDjiGLSurfaceView;
    private DJIReceivedVideoDataCallBack mReceivedVideoDataCallBack = null;

    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SHOWDIALOG:
                    showMessage("Activation Message",(String)msg.obj);
                    break;
                case SHOWTOAST:
                    Toast.makeText(CameraActivity.this, (String)msg.obj, Toast.LENGTH_SHORT).show();
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
                viewTimer.setText(Integer.toString(i++));

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        DroneCode = 1;

        DJIDrone.getDjiCamera().setDecodeType(DJICameraDecodeTypeDef.DecoderType.Software);

        mDjiGLSurfaceView = (DjiGLSurfaceView)findViewById(R.id.DjiSurfaceView_02);

        mDjiGLSurfaceView.start();

        mReceivedVideoDataCallBack = new DJIReceivedVideoDataCallBack(){

            @Override
            public void onResult(byte[] videoBuffer, int size)
            {
                // TODO Auto-generated method stub
                mDjiGLSurfaceView.setDataToDecoder(videoBuffer, size);
            }


        };

        DJIDrone.getDjiCamera().setDjiCameraSystemStateCallBack(new DJICameraSystemStateCallBack() {

            @Override
            public void onResult(DJICameraSystemState state)
            {
                // TODO Auto-generated method stub
                if (state.isTakingContinusPhoto) {
                    handler.sendMessage(handler.obtainMessage(SHOWTOAST, "isTakingContinuousPhoto"));
                }
            }
        });

        DJIDrone.getDjiCamera().setReceivedVideoDataCallBack(mReceivedVideoDataCallBack);


        viewTimer = (TextView) findViewById(R.id.timer);
        captureAction = (Button) findViewById(R.id.button1);
        recordAction = (Button) findViewById(R.id.button2);
        captureMode = (Button) findViewById(R.id.button3);

        captureAction.setOnClickListener(this);
        recordAction.setOnClickListener(this);
        captureMode.setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:{
                captureAction();
                break;
            }
            case R.id.button2:{
                recordAction();
                break;
            }
            case R.id.button3:{
                stopRecord();
                break;
            }
            default:
                break;
        }
    }

    // function for taking photo
    private void captureAction(){

        DJICameraSettingsTypeDef.CameraMode cameraMode = DJICameraSettingsTypeDef.CameraMode.Camera_Capture_Mode;
        DJIDrone.getDjiCamera().setCameraMode(cameraMode, new DJIExecuteResultCallback(){

            @Override
            public void onResult(DJIError mErr)
            {
                // TODO Auto-generated method stub
                String result = "errorCode =" + mErr.errorCode + "\n"+"errorDescription =" + DJIError.getErrorDescriptionByErrcode(mErr.errorCode);
                if (mErr.errorCode == DJIError.RESULT_OK) {
                    DJICameraSettingsTypeDef.CameraCaptureMode photoMode = DJICameraSettingsTypeDef.CameraCaptureMode.Camera_Single_Capture; // Set the camera capture mode as Camera_Single_Capture

                    DJIDrone.getDjiCamera().startTakePhoto(photoMode, new DJIExecuteResultCallback(){

                        @Override
                        public void onResult(DJIError mErr)
                        {
                            // TODO Auto-generated method stub
                            String result = "errorCode =" + mErr.errorCode + "\n"+"errorDescription =" + DJIError.getErrorDescriptionByErrcode(mErr.errorCode);
                            handler.sendMessage(handler.obtainMessage(SHOWTOAST, result));  // display the returned message in the callback
                        }

                    }); // Execute the startTakePhoto API
                } else {
                    handler.sendMessage(handler.obtainMessage(SHOWTOAST, result));
                }

            }

        });

    }
    // function for starting recording
    private void recordAction(){
        DJICameraSettingsTypeDef.CameraMode cameraMode = DJICameraSettingsTypeDef.CameraMode.Camera_Record_Mode;
        DJIDrone.getDjiCamera().setCameraMode(cameraMode, new DJIExecuteResultCallback(){

            @Override
            public void onResult(DJIError mErr)
            {
                // TODO Auto-generated method stub
                String result = "errorCode =" + mErr.errorCode + "\n"+"errorDescription =" + DJIError.getErrorDescriptionByErrcode(mErr.errorCode);
                if (mErr.errorCode == DJIError.RESULT_OK) {


                    DJIDrone.getDjiCamera().startRecord(new DJIExecuteResultCallback(){

                        @Override
                        public void onResult(DJIError mErr)
                        {
                            // TODO Auto-generated method stub
                            String result = "errorCode =" + mErr.errorCode + "\n"+"errorDescription =" + DJIError.getErrorDescriptionByErrcode(mErr.errorCode);
                            handler.sendMessage(handler.obtainMessage(SHOWTOAST, result));  // display the returned message in the callback
                            handlerTimer.postDelayed(runnable, TIME); // Start the timer for recording
                        }

                    }); // Execute the startTakePhoto API
                } else {
                    handler.sendMessage(handler.obtainMessage(SHOWTOAST, result));
                }

            }

        });

    }
    // function for stopping recording
    private void stopRecord(){
        DJIDrone.getDjiCamera().stopRecord(new DJIExecuteResultCallback(){

            @Override
            public void onResult(DJIError mErr)
            {
                // TODO Auto-generated method stub
                String result = "errorCode =" + mErr.errorCode + "\n"+"errorDescription =" + DJIError.getErrorDescriptionByErrcode(mErr.errorCode);
                handler.sendMessage(handler.obtainMessage(SHOWTOAST, result));
                handlerTimer.removeCallbacks(runnable); // Start the timer for recording
                i = 0; // Reset the timer for recording
            }

        });
    }

    public void showMessage(String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub

        super.onResume();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub

        super.onPause();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        // TODO Auto-generated method stub

        // The following codes are used to destroy the SurfaceView.
        if(DJIDrone.getDjiCamera() != null)
            DJIDrone.getDjiCamera().setReceivedVideoDataCallBack(null);
        mDjiGLSurfaceView.destroy();
        super.onDestroy();
        // The following codes are used to kill the application process.
      //  android.os.Process.killProcess(Process.myPid());
    }
    // The following codes are used to exit FPVActivity when pressing the phone's "return" button twice.
    private static boolean first = false;
    private Timer ExitTimer = new Timer();

    class ExitCleanTask extends TimerTask {

        @Override
        public void run() {

            Log.e("ExitCleanTask", "Run in!!!! ");
            first = false;
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d(TAG,"onKeyDown KEYCODE_BACK");

            if (first) {
                first = false;
                finish();
            }
            else
            {
                first = true;
                Toast.makeText(CameraActivity.this, "Press again to exit application", Toast.LENGTH_SHORT).show();
                ExitTimer.schedule(new ExitCleanTask(), 2000);
            }

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
