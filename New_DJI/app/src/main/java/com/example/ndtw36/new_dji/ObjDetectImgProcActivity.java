package com.example.ndtw36.new_dji;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ObjDetectImgProcActivity extends AppCompatActivity implements OnTouchListener {
    private ImageView mImgView;
    private int mImgViewWidth;
    private int	mImgViewHeight;

    private String LOG_TAG = "ObjDetectImgProcActivity";

    private static final int 	RGB_VIEW = 0;
    private static final int 	PYRDOWN_VIEW = 1;
    private static final int 	HSV_VIEW = 2;
    private static final int 	MASK_VIEW = 3;
    private static final int 	DILATED_MASK_VIEW = 4;

    private Bitmap mSrcBmp;
    private Mat mSrcMat;					// Mat created from the original captured image
    private Mat 				mModMat;				// Initially a copied of the original captured image
    // This Mat would be modified during rendering.

    private Bitmap				mBmpOut;
    private Scalar mBlobColorRgb;
    private Scalar              mBlobColorHsv;
    private Mat					mSpectrum;
    private Size SPECTRUM_SIZE;
    private Scalar               CONTOUR_COLOR;

    private MenuItem[] 			mDiffViewItems;
    private SubMenu mDiffViewMenu;

    private ObjBlobDetection 	mObjDetection;
    private static final int	mPyrDownScale = 2;
    private static final int	mColorSelectionSize = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_obj_detect_img_proc);

        mImgView = (ImageView) findViewById(R.id.captured_img_view);
        mImgView.setOnTouchListener(ObjDetectImgProcActivity.this);
        ViewTreeObserver mVto = mImgView.getViewTreeObserver();
        mVto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                mImgViewHeight = mImgView.getMeasuredHeight();
                mImgViewWidth = mImgView.getMeasuredWidth();
                return true;
            }
        });

        Log.v(LOG_TAG, "created");

        File[] allFiles ;
        File folder = new File( Environment.getExternalStorageDirectory().getPath() +"/DJI/com.example.ndtw36.new_dji/CACHE_IMAGE");
        allFiles = folder.listFiles();
        mSrcBmp = BitmapFactory.decodeFile(allFiles[0].toString());

        mSrcMat = new Mat ( mSrcBmp.getHeight(), mSrcBmp.getWidth(), CvType.CV_8U, new Scalar(4));
        Utils.bitmapToMat(mSrcBmp, mSrcMat);

        mModMat = mSrcMat.clone();

        mObjDetection = new ObjBlobDetection(mSrcMat, mPyrDownScale);

        mSpectrum = new Mat();
        mBlobColorRgb = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(255,0,0,255);

        Bitmap mBmpOut = Bitmap.createBitmap(mSrcMat.cols(), mSrcMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mSrcMat, mBmpOut);

        mImgView.setImageBitmap(mBmpOut);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Log.i(LOG_TAG, "called ImgPro::onCreateOptionsMenu");

        mDiffViewMenu = menu.addSubMenu("Show Different Views");
        mDiffViewItems = new MenuItem[6];
        mDiffViewItems[0] = mDiffViewMenu.add(1, 0, Menu.NONE, "RGB");
        mDiffViewItems[1] = mDiffViewMenu.add(1, 1, Menu.NONE, "PyrDown");
        mDiffViewItems[2] = mDiffViewMenu.add(1, 2, Menu.NONE, "HSV");
        mDiffViewItems[3] = mDiffViewMenu.add(1, 3, Menu.NONE, "Mask");
        mDiffViewItems[4] = mDiffViewMenu.add(1, 4, Menu.NONE, "DilatedMask");
        mDiffViewItems[5] = mDiffViewMenu.add(1, 5, Menu.NONE, "DilatedHierarchy");

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        Log.i(LOG_TAG, "called onOptionsItemsSelected; selected item: " + item);
        Mat tmp = new Mat();

        if (item.getGroupId() == 1){
            int id = item.getItemId();
            switch (id){
                case RGB_VIEW:
                    mModMat = mSrcMat.clone();
                    setImgView(mSrcMat, 0);
                    return true;
                case PYRDOWN_VIEW:
                    tmp = mObjDetection.getPyrDownMat();
                    setImgView(tmp, mPyrDownScale);
                    return true;
                case HSV_VIEW:
                    tmp = mObjDetection.getHsvMat();
                    setImgView(tmp, mPyrDownScale);
                    return true;
                case MASK_VIEW:
                    tmp = mObjDetection.getMaskMat();
                    setImgView(tmp, mPyrDownScale);
                    return true;
                case DILATED_MASK_VIEW:
                    tmp = mObjDetection.getDilatedMask();
                    setImgView(tmp, mPyrDownScale);
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }
        return true;
    }

    // calculate the image size based on required width and height
    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight){
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth){
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    // scale the image to optimize memory use
    private Bitmap decodeSampleBitmapFromFile(String fileName,
                                              int reqWidth, int reqHeight){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileName, options);
        Toast.makeText(this, fileName + " loaded.", Toast.LENGTH_SHORT).show();

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(fileName, options);
    }


    // set the bitmap input to the image view
    private void setImgView(Mat srcMat, int pyrUpRep){
        Mat tmpMat = srcMat.clone();

        for (int i = 0; i < pyrUpRep; i++)
            Imgproc.pyrUp(tmpMat, tmpMat);

        Bitmap bmp = Bitmap.createBitmap(tmpMat.cols(), tmpMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(tmpMat, bmp);

        mImgView.setImageBitmap(bmp);

        tmpMat.release();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int cols = mSrcMat.cols();
        int rows = mSrcMat.rows();
        Log.i(LOG_TAG, "View cols: " + cols + " rows: " + rows);

        int xOffset = (mImgViewWidth - cols) / 2;
        int yOffset = (mImgViewHeight - rows) / 2;
        Log.i(LOG_TAG, "View xOffset: " + xOffset + " yOffset: " + yOffset);

        int x = (int)event.getX() - xOffset;
        int y = (int)event.getY() - yOffset;

        Toast.makeText(this, "Touch image coordinates: (" + x + ", " + y + ")", Toast.LENGTH_SHORT).show();
        Log.i(LOG_TAG, "Touch image coordinates: (" + x + ", " + y + ")");

        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        Rect touchedRect = new Rect();

        touchedRect.x = (x>mColorSelectionSize) ? x-mColorSelectionSize : 0;
        touchedRect.y = (y>mColorSelectionSize) ? y-mColorSelectionSize : 0;

        touchedRect.width = (x+mColorSelectionSize < cols) ? x + mColorSelectionSize - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y+mColorSelectionSize < rows) ? y + mColorSelectionSize - touchedRect.y : rows - touchedRect.y;

        Mat touchedRegionRgba = mSrcMat.submat(touchedRect);

        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width*touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;

        mBlobColorRgb = converScalarHsv2Rgb(mBlobColorHsv);

        Log.i(LOG_TAG, "Touched rgba color: (" + mBlobColorRgb.val[0] + ", " + mBlobColorRgb.val[1] +
                ", " + mBlobColorRgb.val[2] + ")");

        mObjDetection.setHsvColor(mBlobColorHsv);
        mObjDetection.updateMask();
        mObjDetection.findContours();

        Imgproc.resize(mObjDetection.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

        List<MatOfPoint> contours = mObjDetection.getContours();
        Log.i(LOG_TAG, "Contours: " + contours.toString());

        Mat mOverlayMat = mModMat.clone();
        Imgproc.rectangle(mOverlayMat, new Point((double) x - mColorSelectionSize, (double) y - mColorSelectionSize),
                new Point((double) x + mColorSelectionSize, (double) y + mColorSelectionSize), new Scalar(0, 255, 255));
        Imgproc.rectangle(mOverlayMat, new Point(10.0, 10 - yOffset), new Point(74.0, 74 - yOffset), mBlobColorRgb, -1);
        Mat spectrumLabel = mOverlayMat	.rowRange(10 - yOffset, 10 - yOffset + mSpectrum.rows())
                .colRange(80, 80 + mSpectrum.cols());
        mSpectrum.copyTo(spectrumLabel);

        Core.addWeighted(mOverlayMat, 1.0, mModMat, 0.0, 0, mModMat);

        //mOverlayMat = mModMat.clone();

        MatOfInt hullInt = new MatOfInt();
        List<Point> hullPointList = new ArrayList<Point>();
        MatOfPoint hullPointMat = new MatOfPoint();

        Iterator<MatOfPoint> each = contours.iterator();
        each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();

            //MatOfPoint2f tmpContour = new MatOfPoint2f(contour.toArray());
            //Imgproc.convexHull(tmpContour, tmpContour)
            //Imgproc.approxPolyDP(tmpContour, tmpContour, 0.01, true);
            //contour = new MatOfPoint(tmpContour.toArray());
            Imgproc.convexHull(contour, hullInt);

            for (int j = 0; j < hullInt.toList().size(); j++){
                hullPointList.add(contour.toList().get(hullInt.toList().get(j)));
            }

            hullPointMat.fromList(hullPointList);

            Imgproc.fillConvexPoly(mOverlayMat, hullPointMat, CONTOUR_COLOR);
        }
        Core.addWeighted(mOverlayMat, 0.3, mModMat, 0.7, 0, mModMat);


        Log.i(LOG_TAG, spectrumLabel.toString());
        //mObjDetection.getContours();

        setImgView(mModMat, 0);

        touchedRegionRgba.release();
        touchedRegionHsv.release();

        return false; // don't need subsequent touch events
    }

    private Scalar converScalarHsv2Rgb(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        Scalar tmp = new Scalar(pointMatRgba.get(0, 0));
        return new Scalar(tmp.val[0], tmp.val[1], tmp.val[2]);
    }
}
