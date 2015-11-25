package com.example.ndtw36.new_dji.EdgeDetect.bitmap_manip.niblack;


import com.example.ndtw36.new_dji.EdgeDetect.bitmap_manip.BitmapPixels;

public class EdgeDetection {

    private static int binaryThreshold = 127;

    public static void binarizeImage(BitmapPixels image, int blockSize,
                                     double stdThreshold, double niblackConstant){
        int height = image.getHeight();
        int width  = image.getWidth();
        for (int i = 0; i < height; i = i + blockSize){
            for (int j = 0; j < width; j = j + blockSize) {

                int m = i + blockSize;
                if (m >= height)
                    m = height - 1;

                int n = j + blockSize;
                if (n >= width)
                    n = width - 1;

                double meanVal = 0;
                double stdVal = 0;
                int p = 0;

                for (int k = i; k < m; k++){
                    for (int l = j; l < n; l++){
                        meanVal = meanVal + image.getPixel(k, l);
                        p++;
                    }
                }
                meanVal = meanVal/p;

                for (int k = i; k < m; k++){
                    for (int l = j; l < n; l++){
                        stdVal = stdVal + Math.pow(image.getPixel(k, l) - meanVal, 2.0);
                    }
                }
                stdVal = Math.sqrt(stdVal/p);
                double threshold = binaryThreshold;
                if (stdVal > stdThreshold)  {
                    threshold = (meanVal + niblackConstant * stdVal);
                }

                for (int k = i; k < m; k++){
                    for (int l = j; l < n; l++){
                        if (image.getPixel(k, l) <= threshold)
                            image.setBinaryPixel(k, l, (short) 0);
                        else
                            image.setBinaryPixel(k, l, (short) 255);
                    }
                }
                
            }
        }
        
        
        for (int i = 0; i < height-1; i++){
            for (int j = 0; j < width-1; j++){
            	int dx = (int) rgb_pix_dx(image,j,i);
            	int dy = (int) rgb_pix_dy(image,j,i);
                image.setBinaryPixel(i, j, (short) gradient_magnitude(dx,dy));
            }
        }
    }
    
    
    
	public static float rgb_pix_dy(BitmapPixels image,int c, int r)
	{
		float default_delta =(float) 1.0;
		if(!(is_in_range(image,c,r)))
		{
			return default_delta;
		}
		else
		{
			float dy = (image.getPixel(r, c-1) - image.getPixel(r, c+1));
			if(dy==0)
				return default_delta;
			else
				return dy;
		}
	}


	public static boolean is_in_range(BitmapPixels image,int c, int r)
	{
		if((c>0) && (c < image.getWidth()) && (r>0) && (r < image.getHeight()))
		{
			return true;
		}
		return false;
	}
	
	
	public static float rgb_pix_dx(BitmapPixels image,int c, int r)
	{
		float default_delta =(float) 1.0;
		if(!(is_in_range(image,c,r)))
		{
			return default_delta;
		}
		else
		{
			float dx = (image.getPixel(r+1, c) - image.getPixel(r-1, c));
			if(dx==0)
				return default_delta;
			else
				return dx;
		}
	}


	public static double gradient_magnitude(float pdx,float pdy)
	{
		return Math.sqrt(Math.pow(pdx, 2)+Math.pow(pdy,2));
	}
}


