package com.example.ndtw36.new_dji;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.File;


public class FaceDetectActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "FaceTracker";

    private Button selectPhoto, recogniseFace;
    private ImageView imageView;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detect);

        selectPhoto = (Button) findViewById(R.id.selectPhoto);
        recogniseFace = (Button) findViewById(R.id.startRecoginising);
        imageView = (ImageView) findViewById(R.id.imageViewFaceDetect);
        selectPhoto.setOnClickListener(this);
        recogniseFace.setOnClickListener(this);

    }

    File[] allFiles ;
    static int a=0;
    private void selectPhoto() {
        File folder = new File(Environment.getExternalStorageDirectory().getPath()+"/screenshots/");
        allFiles = folder.listFiles();

        bitmap = BitmapFactory.decodeFile(allFiles[a].toString());
        imageView.setImageBitmap(bitmap);

        a++;
    }


    private void onFaceRecognise() {

        //Paint object that you’ll use for drawing on the image
        Paint myRectPaint = new Paint();
        myRectPaint.setStrokeWidth(5);
        myRectPaint.setColor(Color.RED);
        myRectPaint.setStyle(Paint.Style.STROKE);

        //setting up the a temp bitmap using the original.
        Bitmap tempBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
        Canvas tempCanvas = new Canvas(tempBitmap);
        tempCanvas.drawBitmap(bitmap, 0, 0, null);

        //Detect the Faces
        FaceDetector faceDetector = new FaceDetector.Builder(getApplicationContext())
                .setTrackingEnabled(false)
                .build();
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Face> faces = faceDetector.detect(frame);

        //Draw Rectangles on the Faces
        for (int i = 0; i < faces.size(); i++) {
            Face thisFace = faces.valueAt(i);
            float x1 = thisFace.getPosition().x;
            float y1 = thisFace.getPosition().y;
            float x2 = x1 + thisFace.getWidth();
            float y2 = y1 + thisFace.getHeight();
            tempCanvas.drawRoundRect(new RectF(x1, y1, x2, y2), 2, 2, myRectPaint);
        }
        imageView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.selectPhoto:
                selectPhoto();
            case R.id.startRecoginising:
                if (bitmap != null) {
                    Toast.makeText(getApplicationContext(),"success",Toast.LENGTH_SHORT).show();
                    onFaceRecognise();
                }
                break;
        }
    }
}

