package com.example.ndtw36.new_dji;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.ndtw36.new_dji.EdgeDetect.bitmap_manip.BitmapPixels;
import com.example.ndtw36.new_dji.EdgeDetect.bitmap_manip.niblack.EdgeDetection;
import com.example.ndtw36.new_dji.EdgeDetect.bitmap_manip.tools.ImageFileReader;

import java.io.File;

//this class is edge detection on image/photo (no real time image processing)
public class EdgeDetectionActivity extends AppCompatActivity {

    final static String INPUT_IMG_DIR  = Environment.getExternalStorageDirectory().getPath() + "/DJI/com.example.ndtw36.new_dji/CACHE_IMAGE/";
    final static String OUTPUT_IMG_DIR = Environment.getExternalStorageDirectory().getPath() + "/output_images/";
    final static BitmapFactory.Options BITMAP_OPTIONS = new BitmapFactory.Options();
    final static Bitmap.Config PREFERRED_CONFIG = Bitmap.Config.ARGB_8888;
    final static int NIBLACK_BLOCK_SIZE 		= 50;
    final static double NIBLACK_STD_THRESHOLD 	= 12.7;
    final static double NIBLACK_CONSTANT 		= -0.25;

    private Button btn;
    private ImageView edgeImg;

    static {
        BITMAP_OPTIONS.inPreferredConfig = PREFERRED_CONFIG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edge_detection);

        btn=(Button)findViewById(R.id.button);
        edgeImg=(ImageView)findViewById(R.id.EdgeImg);

        ensureImageDirs();
        Bitmap bm = null;
        BitmapPixels bp = null;
        for(File img_file: ImageFileReader.getImageFilesFromDir(INPUT_IMG_DIR)) {
            bm = ImageFileReader.decodeFile(img_file, BITMAP_OPTIONS);
            bp = new BitmapPixels(img_file.toString(), bm);
            Toast.makeText(this, "Binarizing " + img_file.getName(), Toast.LENGTH_LONG).show();
            EdgeDetection.binarizeImage(bp, NIBLACK_BLOCK_SIZE, NIBLACK_STD_THRESHOLD, NIBLACK_CONSTANT);
            bm.recycle();
            bm = null;
            String file_name = img_file.getName();
            file_name = file_name.substring(0, file_name.indexOf("."));
            bp.exportBinaryPixelsToFile(OUTPUT_IMG_DIR, file_name + "_niblack");
            bp = null;
        }

        Toast.makeText(this, "Binarization Done!", Toast.LENGTH_LONG).show();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPhoto();
            }
        });
    }

    final static void ensureImageDirs() {
        File inputDir = new File(INPUT_IMG_DIR);
        inputDir.mkdirs();
        File outputDir = new File(OUTPUT_IMG_DIR);
        outputDir.mkdirs();
    }

    Bitmap bitmap;
    File[] allFiles ;
    static int a=0;
    private void selectPhoto() {
        File folder = new File(OUTPUT_IMG_DIR);
        allFiles = folder.listFiles();

        bitmap = BitmapFactory.decodeFile(allFiles[a].toString());
        edgeImg.setImageBitmap(bitmap);

        a++;
        if(a==allFiles.length){
            a=0;
        }
    }
}
