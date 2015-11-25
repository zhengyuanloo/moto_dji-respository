package com.example.ndtw36.new_dji.EdgeDetect.bitmap_manip.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileIO {

    public static final void exportToFile(String str, String directory, String filename){
        File directoryFile = new File(directory);
        directoryFile.mkdirs();

        try{
            File file = new File(directory + filename + ".txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(str, 0, str.length());
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static final void exportToFile(final short[][] array, int height, int width, String directory, String filename){

        try{
            File file = new File(directory + filename + ".txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for (int i = 0; i < height; i++){
                for (int j = 0; j < width; j++){
                    writer.write(array[i][j] + " ");
                }
                writer.write("\n");
            }
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public static final void deleteDirectoryAndContents(String directoryName)
    {
        File dir = new File(directoryName);
        if ( dir.isDirectory() ) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                new File(dir, children[i]).delete();
            }
        }
    }
}

