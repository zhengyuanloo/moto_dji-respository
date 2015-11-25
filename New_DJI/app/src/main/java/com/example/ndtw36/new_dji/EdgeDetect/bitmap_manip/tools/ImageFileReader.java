package com.example.ndtw36.new_dji.EdgeDetect.bitmap_manip.tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.util.Locale;

public class ImageFileReader {
	//private BitmapFactory.Options mBitmapOptions;
	static final String DEBUG_TAG = ImageFileReader.class.getSimpleName()
			+ "_TAG";

	//public ImageFileReader() {
	//	this.mBitmapOptions = new BitmapFactory.Options();
	//	this.mBitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
	//}

	public final static Bitmap decodeFile(File file, BitmapFactory.Options bm_options) {
		return BitmapFactory.decodeFile(file.getAbsolutePath(), bm_options);
	}

	public final static File[] getImageFilesFromDir(String directory) {
		File dir = new File(directory);
		File[] imageFiles = dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				String fileName = file.getName().toLowerCase(
						Locale.getDefault());
				Log.d(DEBUG_TAG, fileName);
				if (fileName.endsWith(".bmp") || fileName.endsWith(".jpg")
						|| fileName.endsWith(".png"))
					return true;
				return false;
			}
		});
		return imageFiles;
	}
}
