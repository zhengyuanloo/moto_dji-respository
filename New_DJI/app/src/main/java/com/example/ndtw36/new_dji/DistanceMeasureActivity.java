package com.example.ndtw36.new_dji;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import dji.sdk.api.Camera.DJICameraDecodeTypeDef;
import dji.sdk.api.Camera.DJICameraSettingsTypeDef;
import dji.sdk.api.DJIDrone;
import dji.sdk.api.Gimbal.DJIGimbalAttitude;
import dji.sdk.api.Gimbal.DJIGimbalRotation;
import dji.sdk.api.MainController.DJIMainControllerSystemState;
import dji.sdk.api.MainController.DJIMainControllerTypeDef;
import dji.sdk.interfaces.DJIGimbalErrorCallBack;
import dji.sdk.interfaces.DJIGimbalUpdateAttitudeCallBack;
import dji.sdk.interfaces.DJIMcuErrorCallBack;
import dji.sdk.interfaces.DJIMcuUpdateStateCallBack;
import dji.sdk.interfaces.DJIReceivedVideoDataCallBack;
import dji.sdk.widget.DjiGLSurfaceView;

public class DistanceMeasureActivity extends AppCompatActivity {

    private static final String TAG = "DistanceMeaureActivity";
    private int DroneCode;
    private TextView distance;
    private Button pitchUp,pitchDown,pitchGo,lockDistance;
    private boolean isLock=false;
    private EditText etPitch;
    double altitude;
    double pitch;
    private int i = 0;
    private DjiGLSurfaceView mDjiGLSurfaceView;
    private DJIReceivedVideoDataCallBack mReceivedVideoDataCallBack = null;
    private DJICameraSettingsTypeDef.CameraVisionType type = DJIDrone.getDjiCamera().getVisionType();
    private DJIGimbalErrorCallBack mGimbalErrorCallBack;
    private DJIGimbalUpdateAttitudeCallBack mGimbalUpdateAttitudeCallBack;

    double len=0;
    double height=0;
    double width=0;
    double angleX,angleY,angleZ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distance_measure);

        DroneCode = 1;

        DJIDrone.getDjiCamera().setDecodeType(DJICameraDecodeTypeDef.DecoderType.Software);

        mDjiGLSurfaceView = (DjiGLSurfaceView)findViewById(R.id.DjiMeaureView);

        mDjiGLSurfaceView.start();

        mReceivedVideoDataCallBack = new DJIReceivedVideoDataCallBack(){

            @Override
            public void onResult(byte[] videoBuffer, int size)
            {
                // TODO Auto-generated method stub
                mDjiGLSurfaceView.setDataToDecoder(videoBuffer, size);
            }


        };

        DJIDrone.getDjiCamera().setReceivedVideoDataCallBack(mReceivedVideoDataCallBack);

        mGimbalErrorCallBack = new DJIGimbalErrorCallBack(){

            @Override
            public void onError(int error) {
                // TODO Auto-generated method stub
                //Log.d(TAG, "Gimbal error = "+error);
            }

        };

        mGimbalUpdateAttitudeCallBack = new DJIGimbalUpdateAttitudeCallBack(){

            @Override
            public void onResult(DJIGimbalAttitude attitude) {
                // TODO Auto-generated method stub
                //Log.d(TAG, attitude.toString());
                pitch=attitude.pitch;
            }
        };

        DJIDrone.getDjiGimbal().setGimbalErrorCallBack(mGimbalErrorCallBack);
        DJIDrone.getDjiGimbal().setGimbalUpdateAttitudeCallBack(mGimbalUpdateAttitudeCallBack);

        measureXYZDistance();

        Minus_Listener minuslisten = new Minus_Listener();
        Plus_Listener Pluslisten = new Plus_Listener();

        distance=(TextView)findViewById(R.id.tvDistance);
        pitchUp=(Button)findViewById(R.id.PitchUp);
        pitchDown=(Button)findViewById(R.id.PitchDown);
        pitchGo=(Button)findViewById(R.id.btn_go);
        etPitch=(EditText)findViewById(R.id.etPitch);
        lockDistance=(Button)findViewById(R.id.lock_btn);

        pitchUp.setOnTouchListener(Pluslisten);
        pitchDown.setOnTouchListener(minuslisten);

        setPitchAngle();
        setDistanceLocker();
    }

    private void measureXYZDistance() {
        DJIDrone.getDjiMC().setMcuUpdateStateCallBack(new DJIMcuUpdateStateCallBack() {
            @Override
            public void onResult(DJIMainControllerSystemState djiMainControllerSystemState) {
                altitude = djiMainControllerSystemState.altitude;
                altitude=0.66;
                angleY=90+pitch;

                if(!isLock){
                    len=altitude*Math.tan(Math.toRadians(angleY));
                }else{
                    if(pitch>0)
                    {
                        height=len*Math.tan(Math.toRadians(pitch));
                        height=height+altitude;
                    }else if(pitch==0)
                    {
                        height=altitude;
                    }else{
                        height=len*Math.tan(Math.toRadians(-pitch));
                        height=0.66-height;
                    }
                }

                DistanceMeasureActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        distance.setText("Altitude: " + altitude + " Pitch: " + angleY+ " Length: "+len
                        +" Height: "+height);
                    }
                });
            }
        });
        DJIDrone.getDjiMC().setMcuErrorCallBack(new DJIMcuErrorCallBack() {
            @Override
            public void onError(DJIMainControllerTypeDef.DJIMcErrorType djiMcErrorType) {

            }
        });
    }

    private void setDistanceLocker() {
        lockDistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLock=!isLock;
            }
        });

        if(!isLock){
            lockDistance.setText("Lock Distance");
        }else{
            lockDistance.setText("Unlock");
        }
    }

    private void setPitchAngle() {
        pitchGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread()
                {
                    public void run()
                    {
                        String vString = etPitch.getText().toString();

                        int pitchGo = 0;
                        try {
                            pitchGo = Integer.parseInt(vString);
                        } catch (Exception e) {
                            // TODO: handle exception
                            pitchGo = 0;

                            DistanceMeasureActivity.this.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    // TODO Auto-generated method stub
                                    etPitch.setText("0");
                                }
                            });

                        }
                        //Log.e("", "PitchGoButton click");
                        DJIGimbalRotation mPitch = new DJIGimbalRotation(true, false,true, pitchGo);

                        DJIDrone.getDjiGimbal().updateGimbalAttitude(mPitch,null,null);

                    }
                }.start();
            }
        });
    }

    private boolean mIsPitchUp = false;
    private boolean mIsPitchDown = false;
    class Plus_Listener implements View.OnClickListener, View.OnTouchListener {
        @Override
        public void onClick(View view) {
            //Log.e("", "plus click");
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_DOWN)
            {
                mIsPitchUp = true;

                new Thread()
                {
                    public void run()
                    {
                        DJIGimbalRotation mPitch = null;
                        if(type == DJICameraSettingsTypeDef.CameraVisionType.Camera_Type_Plus || type == DJICameraSettingsTypeDef.CameraVisionType.Camera_Type_Inspire){
                            mPitch = new DJIGimbalRotation(true,true,false, 150);
                        }
                        else{
                            mPitch = new DJIGimbalRotation(true,true,false, 20);
                        }
                        DJIGimbalRotation mPitch_stop = new DJIGimbalRotation(false, false,false, 0);

                        while(mIsPitchUp)
                        {
                            //Log.e("", "A5S plus click");

                            DJIDrone.getDjiGimbal().updateGimbalAttitude(mPitch,null,null);

                            try
                            {
                                Thread.sleep(50);
                            } catch (InterruptedException e)
                            {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        DJIDrone.getDjiGimbal().updateGimbalAttitude(mPitch_stop,null,null);
                    }
                }.start();

            } else if (event.getAction() == MotionEvent.ACTION_UP|| event.getAction() == MotionEvent.ACTION_OUTSIDE || event.getAction() == MotionEvent.ACTION_CANCEL)
            {

                mIsPitchUp = false;

            }

            return false;
        }
    };

    class Minus_Listener implements View.OnClickListener, View.OnTouchListener {
        @Override
        public void onClick(View view) {
            //Log.e("", "minus click");
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_DOWN)
            {
                mIsPitchDown = true;

                new Thread()
                {
                    public void run()
                    {
                        DJIGimbalRotation mPitch = null;
                        if(type == DJICameraSettingsTypeDef.CameraVisionType.Camera_Type_Plus || type == DJICameraSettingsTypeDef.CameraVisionType.Camera_Type_Inspire)
                        {
                            mPitch = new DJIGimbalRotation(true, false,false, 150);
                        }else
                        {

                            mPitch = new DJIGimbalRotation(true, false, false, 20);
                        }

                        DJIGimbalRotation mPitch_stop = new DJIGimbalRotation(false, false,false, 0);

                        while(mIsPitchDown)
                        {
                            //Log.e("", "A5S plus click");


                            DJIDrone.getDjiGimbal().updateGimbalAttitude(mPitch,null,null);

                            try
                            {
                                Thread.sleep(50);
                            } catch (InterruptedException e)
                            {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        DJIDrone.getDjiGimbal().updateGimbalAttitude(mPitch_stop,null,null);
                    }
                }.start();

            } else if (event.getAction() == MotionEvent.ACTION_UP|| event.getAction() == MotionEvent.ACTION_OUTSIDE || event.getAction() == MotionEvent.ACTION_CANCEL)
            {

                mIsPitchDown = false;

            }

            return false;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        mDjiGLSurfaceView.resume();
        DJIDrone.getDjiGimbal().startUpdateTimer(1000);
        DJIDrone.getDjiMC().startUpdateTimer(1000);

        super.onResume();
    }

    protected void onPause() {
        // TODO Auto-generated method stub
        mDjiGLSurfaceView.pause();

        DJIDrone.getDjiGimbal().stopUpdateTimer();
        DJIDrone.getDjiMC().stopUpdateTimer();

        super.onPause();
    }
}
