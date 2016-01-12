package com.example.ndtw36.new_dji;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LineDetectActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    private static final String TAG ="LineDetectActivity" ;

    private Mat mRgba,mGray;
    Mat mYuv ;
    Mat thresholdImage;
    Mat dst;
    MatOfPoint2f approxCurve;
    private Button btn;
    int noEdge=2;
    private EditText etNo;
    TextView tvCdn;

    //Display message
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch(status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "OpenCV Manager Connected");
                    //from now onwards, you can use OpenCV API
                    mOpenCvCameraView.enableView();
                    break;
                case LoaderCallbackInterface.INIT_FAILED:
                    Log.i(TAG,"Init Failed");
                    break;
                case LoaderCallbackInterface.INSTALL_CANCELED:
                    Log.i(TAG,"Install Cancelled");
                    break;
                case LoaderCallbackInterface.INCOMPATIBLE_MANAGER_VERSION:
                    Log.i(TAG,"Incompatible Version");
                    break;
                case LoaderCallbackInterface.MARKET_ERROR:
                    Log.i(TAG,"Market Error");
                    break;
                default:
                    Log.i(TAG,"OpenCV Manager Install");
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    private CameraBridgeViewBase mOpenCvCameraView;

    public LineDetectActivity(){
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_line_detect);

        tvCdn=(TextView)findViewById(R.id.tvCdn);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_LineDetect);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //initial and load the openCV manager
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mOpenCvCameraView!=null){
            mOpenCvCameraView.disableView();
            Log.i("OpenCV","OpenCV Camera disable");
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mGray=new Mat();
        dst=new Mat();
        thresholdImage = new Mat(height + height / 2, width, CvType.CV_8UC1);
        approxCurve = new MatOfPoint2f();
        mYuv = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
        dst.release();
        mYuv.release();
        thresholdImage.release();
    }

    //implement line detection here
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRgba=inputFrame.rgba();
        mGray=inputFrame.gray();
        mYuv=mRgba.clone();
        thresholdImage=inputFrame.gray();

        //get edge from inputframe
        Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGBA2RGB);
        Imgproc.Canny(mGray, dst, 80, 100);

        Mat lines = new Mat();
        int threshold = 50;
        int minLineSize = 20;
        int lineGap = 20;

        double[] data;
        double rho, theta;
        final Point pt1 = new Point();
        final Point pt2 = new Point();
        double a, b;
        double x0, y0;

        double minAngle=Math.toRadians(30);

        /*Imgproc.HoughLines(dst, lines, 1, Math.PI / 180, threshold);

        for (int x = 0; x < lines.cols(); x++)
        {
            data = lines.get(0,x);
            rho = data[0];
            theta = data[1];
            a = Math.cos(theta);
            b = Math.sin(theta);
            x0 = a*rho;
            y0 = b*rho;
            pt1.x = Math.round(x0 + 1000*(-b));
            pt1.y = Math.round(y0 + 1000*a);
            pt2.x = Math.round(x0 - 1000*(-b));
            pt2.y = Math.round(y0 - 1000 *a);

            LineDetectActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if(pt1.y>760){
                        tvCdn.setText("");
                    }else{
                    tvCdn.setText("x1 :" + pt1.x +
                            "x2 :" +pt2.x +
                            "y1 :" + pt1.y +
                            "y2 :" + pt2.y);}
                }
            });

          //  Imgproc.rectangle(dst, start, end, new Scalar(255, 0, 0, 255), 3);
            Imgproc.line(mRgba, pt1, pt2, new Scalar(0, 255, 0, 255), 3);
        }*/

        //detect line using edge detected
        Imgproc.HoughLinesP(dst, lines, 1, Math.PI/180, threshold, minLineSize, lineGap);

        //draw lines along 2 points
        for (int x = 0; x < lines.cols(); x++)
        {
            //define 2 points (x,y)
            double[] vec = lines.get(0, x);
            final double x1 = vec[0],
                    y1 = vec[1],
                    x2 = vec[2],
                    y2 = vec[3];
            final Point start = new Point(x1, y1);
            final Point end = new Point(x2, y2);


                        //start draw line between 2 points
                        Imgproc.line(mRgba, start, end, new Scalar(255, 0, 0), 3);

        }

        return mRgba;
    }
}
