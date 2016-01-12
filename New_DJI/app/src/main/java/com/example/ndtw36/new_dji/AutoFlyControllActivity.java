package com.example.ndtw36.new_dji;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Text;

import java.util.Timer;

import dji.sdk.api.Camera.DJICameraDecodeTypeDef;
import dji.sdk.api.Camera.DJICameraSystemState;
import dji.sdk.api.DJIDrone;
import dji.sdk.api.DJIDroneTypeDef;
import dji.sdk.api.GroundStation.DJIGroundStationTask;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef;
import dji.sdk.api.GroundStation.DJIGroundStationWaypoint;
import dji.sdk.api.MainController.DJIMainControllerSystemState;
import dji.sdk.api.MainController.DJIMainControllerTypeDef;
import dji.sdk.interfaces.DJICameraSystemStateCallBack;
import dji.sdk.interfaces.DJIGroundStationExecuteCallBack;
import dji.sdk.interfaces.DJIMcuErrorCallBack;
import dji.sdk.interfaces.DJIMcuUpdateStateCallBack;
import dji.sdk.interfaces.DJIReceivedVideoDataCallBack;
import dji.sdk.widget.DjiGLSurfaceView;

public class AutoFlyControllActivity extends AppCompatActivity {

    private static final String TAG = "AutoFlyControllActivity";
    private int DroneCode;
    private final int SHOWDIALOG = 1;
    private final int SHOWTOAST = 2;
    private final int STOP_RECORDING = 10;
    private Button takeOff,start,stop,land,btnDegree;
    private TextView viewTimer,state;
    private EditText etYaw,etFowBack,etLeftRight,etDegree;
    private int i = 0;
    private int TIME = 1000;
    private DjiGLSurfaceView mDjiGLSurfaceView;
    private DJIReceivedVideoDataCallBack mReceivedVideoDataCallBack = null;

    private String flightState;

    //Display message
    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SHOWDIALOG:
                    //showMessage("Activation Message",(String)msg.obj);
                    break;
                case SHOWTOAST:
                    Toast.makeText(AutoFlyControllActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_fly_controll);

        DroneCode = 1;

        //select decoded type (hardware or software)
        DJIDrone.getDjiCamera().setDecodeType(DJICameraDecodeTypeDef.DecoderType.Software);

        //initial the djiSurfaceView view in order to display real time image from drone camera
        mDjiGLSurfaceView = (DjiGLSurfaceView)findViewById(R.id.AutoFly_surfaceDJI);

        mDjiGLSurfaceView.start();

        //Call video buffer data from drone
        mReceivedVideoDataCallBack = new DJIReceivedVideoDataCallBack(){

            @Override
            public void onResult(byte[] videoBuffer, int size)
            {

                mDjiGLSurfaceView.setDataToDecoder(videoBuffer, size);
            }


        };

        //Get drone camera state
        DJIDrone.getDjiCamera().setDjiCameraSystemStateCallBack(new DJICameraSystemStateCallBack() {

            @Override
            public void onResult(DJICameraSystemState state) {

                if (state.isTakingContinusPhoto) {
                    handler.sendMessage(handler.obtainMessage(SHOWTOAST, "isTakingContinuousPhoto"));
                }
            }
        });

        DJIDrone.getDjiCamera().setReceivedVideoDataCallBack(mReceivedVideoDataCallBack);

        displayMainControll();

        takeOff=(Button)findViewById(R.id.takeOFF);
        start=(Button)findViewById(R.id.btn_start);
        stop=(Button)findViewById(R.id.btnStop);
        land=(Button)findViewById(R.id.btn_land);
        viewTimer=(TextView)findViewById(R.id.viewTimer);
        state=(TextView)findViewById(R.id.tvState);
        btnDegree=(Button)findViewById(R.id.btnDegree);
        etDegree=(EditText)findViewById(R.id.etDegree);

       etYaw=(EditText)findViewById(R.id.etYaw); //speed of yaw -500 to 500 // negative speed is anticlockwise //positive speed is clockwise
        etFowBack=(EditText)findViewById(R.id.etFowBack);//speed of moving horizontal// positive speed is moving forward// negative speed is moving backward
        etLeftRight=(EditText)findViewById(R.id.etLeftRight);//speed of moving horizontal// positive speed is moving right// negative speed is moving left

        takeOFFDrone();
        startFlightMode();
        stopFlightMode();
        landDrone();
        degreeControl();

    }


    double curYaw;
    //set drone yaw degree
    private void degreeControl() {
        final int[] yawDegreeSet = new int[1];

        btnDegree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                yawDegreeSet[0] =Integer.parseInt(etDegree.getText().toString());

                if(yawDegreeSet[0] >curYaw){
                    while(curYaw<yawDegreeSet[0]){
                        setAircraftFlightMode(30, 0, 0, 0);}
                }else{
                    while(curYaw>yawDegreeSet[0]){
                        setAircraftFlightMode(-30, 0, 0, 0);}
                }
            }
        });
    }

    double latitude;
    double longitude;
    float altitude;
    String bufferStr;
    double maxVelocityX=0;
    double maxVelocityY=0;
    double maxVelocityZ=0;
    String maxVelocity;

    //Display drone state such location, speed, height and so on
    public void displayMainControll() {

        DJIDrone.getDjiMC().setMcuUpdateStateCallBack(new DJIMcuUpdateStateCallBack() {
            @Override
            public void onResult(DJIMainControllerSystemState djiMainControllerSystemState) {
                StringBuffer sb = new StringBuffer();

                latitude=djiMainControllerSystemState.droneLocationLatitude;
                longitude=djiMainControllerSystemState.droneLocationLongitude;
                altitude= (float) djiMainControllerSystemState.altitude;
                curYaw=djiMainControllerSystemState.yaw;

                sb.append("velocityX=").append(djiMainControllerSystemState.velocityX).append("\n");
                sb.append("velocityY=").append(djiMainControllerSystemState.velocityY).append("\n");
                sb.append("velocityZ=").append(djiMainControllerSystemState.velocityZ).append("\n");
                sb.append("speed=").append(djiMainControllerSystemState.speed).append("\n");
                sb.append("altitude=").append(djiMainControllerSystemState.altitude).append("\n");
                sb.append("pitch=").append(djiMainControllerSystemState.pitch).append("\n");
                sb.append("roll=").append(djiMainControllerSystemState.roll).append("\n");
                sb.append("yaw=").append(djiMainControllerSystemState.yaw).append("\n");
                sb.append("remainPower=").append(djiMainControllerSystemState.remainPower).append("\n");
                sb.append("remainFlyTime=").append(djiMainControllerSystemState.remainFlyTime).append("\n");

                bufferStr=sb.toString();

                if(djiMainControllerSystemState.velocityX>maxVelocityX){
                    maxVelocityX=djiMainControllerSystemState.velocityX;
                }
                if(djiMainControllerSystemState.velocityY>maxVelocityY){
                    maxVelocityY=djiMainControllerSystemState.velocityY;
                }
                if(djiMainControllerSystemState.velocityX>maxVelocityZ){
                    maxVelocityZ=djiMainControllerSystemState.velocityZ;
                }

                maxVelocity="Max Velocity:\nX: "+maxVelocityX+"\nY: "+maxVelocityY+"\nZ: "+maxVelocityZ;

                AutoFlyControllActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        state.setText(flightState + "\n" + bufferStr.toString()+"\n"+maxVelocity);
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

    //Order drone to landing //th
    private void landDrone() {
       land.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               while(altitude>0) {
                   setAircraftFlightMode(0,0,0,2);
               }
           }
       });
    }


    //setting flight speed and drone rotation
    int yaw=0,roll=0,pitch=0,throttle=0;
    private void startFlightMode() {

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                yaw = Integer.parseInt(etYaw.getText().toString());
                pitch = Integer.parseInt(etFowBack.getText().toString());
                roll = Integer.parseInt(etLeftRight.getText().toString());

                setAircraftFlightMode(yaw, pitch, roll, 0);
            }
        });
    }

    //function for stopping the flying drone
    private void stopFlightMode() {

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAircraftFlightMode(0, 0, 0, 0);
                maxVelocityX=0;
                maxVelocityY=0;
                maxVelocityZ=0;
            }
        });
    }

    //setting flight speed and drone rotation
    public void setAircraftFlightMode(final int yaw, final int pitch, final int roll, final int throttle){

        DJIDrone.getDjiGroundStation().setAircraftJoystick(yaw, pitch, roll, throttle, new DJIGroundStationExecuteCallBack() {

            @Override
            public void onResult(DJIGroundStationTypeDef.GroundStationResult result) {

                AutoFlyControllActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        flightState = "Yaw: " + yaw + " Pitch: " + pitch
                                + "\n Roll: " + roll + " throttle: " + throttle;
                    }
                });
            }
        });
    }

    //function for taking off
    private void takeOFFDrone() {
        takeOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DJIDrone.getDjiGroundStation().openGroundStation(new DJIGroundStationExecuteCallBack() {

                    @Override
                    public void onResult(DJIGroundStationTypeDef.GroundStationResult result) {

                        String ResultsString = "opens result =" + result.toString();
                        handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));

                        DJIDrone.getDjiGroundStation().oneKeyFly(new DJIGroundStationExecuteCallBack() {
                            @Override
                            public void onResult(DJIGroundStationTypeDef.GroundStationResult result) {

                                String ResultsString = "one key fly result =" + result.toString();
                                handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));

                                if (result == DJIGroundStationTypeDef.GroundStationResult.GS_Result_Success) {

                                }
                            }

                        });

                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        mDjiGLSurfaceView.resume();

        DJIDrone.getDjiMC().startUpdateTimer(1000);
        super.onResume();
    }

    @Override
    protected void onPause() {
        mDjiGLSurfaceView.pause();

        DJIDrone.getDjiMC().stopUpdateTimer();
        super.onPause();
    }
}
