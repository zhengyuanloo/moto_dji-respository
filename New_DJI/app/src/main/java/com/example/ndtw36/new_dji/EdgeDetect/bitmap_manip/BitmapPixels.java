package com.example.ndtw36.new_dji.EdgeDetect.bitmap_manip;


import android.graphics.Bitmap;

import com.example.ndtw36.new_dji.EdgeDetect.bitmap_manip.tools.BitmapTools;


public class BitmapPixels {

    private String mName;
    private short[][] mPixels;
    private short[][] mBinaryPixels;
    private int mHeight;
    private int mWidth;


    public BitmapPixels(String name, Bitmap bitmap)
    {
        this.mName = name;

        this.mHeight = bitmap.getHeight();
        this.mWidth = bitmap.getWidth();
        this.mPixels = new short[mHeight][mWidth];
        this.mBinaryPixels = new short[mHeight][mWidth];

        BitmapTools.getPixels(bitmap, mPixels);
    }


    public short[][] getPixels() {
        return mPixels;
    }

    public int getHeight() {
        return mHeight;
    }

    public int getWidth() {
        return mWidth;
    }

   

    public int getPixel(int y, int x) {
        return mPixels[y][x];
    }

    public void setBinaryPixel(int y, int x, short val) {
        mBinaryPixels[y][x] = val;
    }

    public short[][] getBinaryPixels() {
        return mBinaryPixels;
    }

   
    public String getName() {
        return mName;
    }

    public void exportBinaryPixelsToFile(String outputDirectory, String name) {
        BitmapTools.exportToFile(mBinaryPixels, mHeight, mWidth,
                outputDirectory, name);
    }

    public short getBinaryPixel(int y, int x) {
        return mBinaryPixels[y][x];
    }
}

