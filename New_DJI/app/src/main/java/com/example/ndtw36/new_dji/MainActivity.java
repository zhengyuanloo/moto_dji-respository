package com.example.ndtw36.new_dji;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import dji.sdk.api.DJIDrone;
import dji.sdk.api.DJIDroneTypeDef;
import dji.sdk.api.DJIError;
import dji.sdk.interfaces.DJIGeneralListener;

//initial drone type and display some activity such as CameraActivity, GroundStationActivity, and so on
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private int type = 2;

    private final int SHOWDIALOG = 2;

    //display message
    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SHOWDIALOG:
                    showMessage("Activation Message",(String)msg.obj);
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
        setContentView(R.layout.activity_main);

        ListView mListView = (ListView)findViewById(R.id.listView);

        Log.v("type", "Type" + type);

        mListView.setAdapter(new DemoListAdapter());
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View v, int index, long arg3) {
                onListItemClick(index);
            }
        });

        onInitSDK(type);

        //check permission of drone API key
        new Thread(){
            public void run() {
                try {
                    DJIDrone.checkPermission(getApplicationContext(), new DJIGeneralListener() {

                        @Override
                        public void onGetPermissionResult(int result) {

                            Log.e(TAG, "onGetPermissionResult = " + result);
                            Log.e(TAG, "onGetPermissionResultDescription = " + DJIError.getCheckPermissionErrorDescription(result));
                            if (result == 0) {
                                handler.sendMessage(handler.obtainMessage(SHOWDIALOG, DJIError.getCheckPermissionErrorDescription(result)));
                            } else {
                                handler.sendMessage(handler.obtainMessage(SHOWDIALOG, "Activation Error: " + DJIError.getCheckPermissionErrorDescription(result) + "\n" + "Error Code: " + result));

                            }
                        }
                    });
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    protected void onResume() {

        super.onResume();
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onDestroy()
    {

        onUnInitSDK();
        super.onDestroy();
    }

    //initial type pf drone
    private void onInitSDK(int type){
        switch(type){
            case 0 : {
                DJIDrone.initWithType(this.getApplicationContext(), DJIDroneTypeDef.DJIDroneType.DJIDrone_Vision);
                break;
            }
            case 1 : {
                DJIDrone.initWithType(this.getApplicationContext(), DJIDroneTypeDef.DJIDroneType.DJIDrone_Inspire1);
                break;
            }
            case 2 : {
                DJIDrone.initWithType(this.getApplicationContext(), DJIDroneTypeDef.DJIDroneType.DJIDrone_Phantom3_Advanced);
                break;
            }
            case 3 : {
                DJIDrone.initWithType(this.getApplicationContext(), DJIDroneTypeDef.DJIDroneType.DJIDrone_M100);
                break;
            }
            default : {
                break;
            }
        }

        DJIDrone.connectToDrone();//start connecting with drone after initial drone type
    }

    //Disconnect the drone
    private void onUnInitSDK(){
        DJIDrone.disconnectToDrone();
    }

    private void onListItemClick(int index) {
        Intent intent = null;
        intent = new Intent(MainActivity.this, demos[index].demoClass);
        this.startActivity(intent);
    }

    //List of message and class activity
    private static final DemoInfo[] demos = {
           new DemoInfo("Camera DJI","Capture Photo Activity", CameraActivity.class),// Image Processing (software decoder type)
            new DemoInfo("Ground Station DJI","Ground Station Maps Activity", GroundStationMapsActivity.class),//google map ground station /flight control
            new DemoInfo("Texture Camera DJI","Texture Camera Activity", TextureCameraActivity.class),// Image Processing(hardware decoder type)
            new DemoInfo("SurfaceView Camera DJI","SurfaceView Camera Activity", SurfaceViewCameraActivity.class), //image processing (hardware decoder type)
            new DemoInfo("Download Media Data DJI","Download Media Data Activity", DownloadMediaActivity.class), //media data download from drone SD card memory
            new DemoInfo("ScreenShot DJI","ScreenShot Activity", ScreenShotActivity.class), // Screen shot surface displayed on device
            new DemoInfo("Face Detect DJI","FaceDetect Activity", FaceDetectActivity.class), //face detection on image/photo
          //  new DemoInfo("Distance Measure DJI","Distance Measure Activity",DistanceMeasureActivity.class),
            new DemoInfo("Auto Fly DJI","Auto Fly Activity",AutoFlyControllActivity.class), //control flight speed and movement
            new DemoInfo("Face Activity","Face Activity",FaceActivity.class),//real time face detection image processing
          //  new DemoInfo("Object Detection Activity","Object Detection Activity",ObjectDetectionCamPreviewActivity.class),
            //new DemoInfo("Edge Detection Activity","Edge Detection Activity",EdgeDetectionActivity.class),
            new DemoInfo("Corner Detection Activity","Corner Detection Activity",CornerDetectionActivity.class),// real time corner detect
            new DemoInfo("Object Tracker Activity","Object Tracker Activity",ObjectTrackerActivity.class),//real time color detection (object recognition)
            new DemoInfo("Shape Detect Activity","Shape Detect Activity",ShapeDetectActivity.class),//real time shape detection (shape recognition)
            new DemoInfo("Distance Detect Activity","Distance Detect Activity",ObjectTrackingActivity.class),//distance detection from drone to object
            new DemoInfo("Edge Detect Activity","Edge Detect Activity",EdgeDetectActivity.class), //real time edge detection
            new DemoInfo("Line Detect Activity","Line Detect Activity",LineDetectActivity.class),//line detect
    };

    //ListView adaptor for MainActivity
    private  class DemoListAdapter extends BaseAdapter {
        public DemoListAdapter() {
            super();
        }

        @Override
        public View getView(int index, View convertView, ViewGroup parent) {
            convertView = View.inflate(MainActivity.this, R.layout.demo_info_item, null);
            TextView title = (TextView)convertView.findViewById(R.id.title);
            TextView desc = (TextView)convertView.findViewById(R.id.desc);

            title.setText(demos[index].title);
            desc.setText(demos[index].desc);
            return convertView;
        }
        @Override
        public int getCount() {
            return demos.length;
        }
        @Override
        public Object getItem(int index) {
            return  demos[index];
        }

        @Override
        public long getItemId(int id) {
            return id;
        }
    }

    private static class DemoInfo{
        private final String title;
        private final String desc;
        private final Class<? extends android.app.Activity> demoClass;

        public DemoInfo(String title , String desc,Class<? extends android.app.Activity> demoClass) {
            this.title = title;
            this.desc  = desc;
            this.demoClass = demoClass;
        }
    }


    public void onReturn(View view){
        Log.d(TAG ,"onReturn");
        this.finish();
    }

    //display message as Alert Dialog
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
}
