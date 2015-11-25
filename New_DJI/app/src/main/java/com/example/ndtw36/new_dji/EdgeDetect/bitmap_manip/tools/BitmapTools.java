package com.example.ndtw36.new_dji.EdgeDetect.bitmap_manip.tools;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapTools {

    public static Bitmap exportToBitmap(final short[][] imageArray, int height, int width)
    {
        Bitmap bitmapImage = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);

        for (int i = 0; i < height; i++)
        {
            for (int j = 0; j < width; j++)
            {
                int val = imageArray[i][j];
                bitmapImage.setPixel(j, i, Color.argb(255, val, val, val));
            }
        }
        return bitmapImage;
    }

    public static void exportToFile(final short[][] imageArray, int height, int width,
                                    String directory, String filename)
    {

        Bitmap bitmap = exportToBitmap(imageArray, height, width);
        exportToFile(bitmap, directory, filename);
        bitmap.recycle();
        bitmap = null;
    }

    public static void exportToFile(Bitmap bitmap, String directory, String filename)
    {
        File directoryFile = new File(directory);
        directoryFile.mkdirs();

        try {
            FileOutputStream out = new FileOutputStream(directory + filename
                    + ".png");

            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public static void getPixels(Bitmap bitmap, short[][] pixels) {
        for (int i = 0, height = bitmap.getHeight(); i < height; i++){
            for (int j = 0, width = bitmap.getWidth(); j < width; j++){
                pixels[i][j] = (short) Color.green(bitmap.getPixel(j, i));
            }
        }
    }

    public static short[][] getSubImage(short[][] pixels, int yStart, int yEnd, int xStart, int xEnd){
        short[][] subImage = new short[yEnd - yStart][xEnd - xStart];
        for (int i = yStart; i < yEnd; i++){
            for (int j = xStart; j < xEnd; j++)
                subImage[i - yStart][j - xStart] = pixels[i][j];
        }
        return subImage;
    }
}


