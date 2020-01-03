package com.entage.nrd.entage.utilities_1;

import java.io.File;
import java.util.ArrayList;

public class FileSearch {
    private static final String TAG = "FileSearch";

    /**
     * Search a directory and return  list of all **directories** contained inside
     * @param directory
     * @return
     */
    public static ArrayList<String> getDirectoryPath(String directory){
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listfiles = file.listFiles();
        for (int i = 0; i < listfiles.length; i++){
            if(listfiles[i].isDirectory()){
                pathArray.add(listfiles[i].getAbsolutePath());
            }
        }
        return pathArray;
    }

    /**
     * Search a directory and return  list of all **files** contained inside
     * @param directory
     * @return
     */
    public static ArrayList<String> getFilePath(String directory){
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);

       // Log.d(TAG, "getFilePath: " + file.);
        File[] listfiles = file.listFiles();
        if (listfiles != null) {
            for (int i = 0; i < listfiles.length; i++){
                if (listfiles[i].getName().endsWith(".png")
                        || listfiles[i].getName().endsWith(".jpg")
                        || listfiles[i].getName().endsWith(".jpeg")
                        || listfiles[i].getName().endsWith(".gif"))
                {
                    pathArray.add(listfiles[i].getAbsolutePath());
                }
            /*if(listfiles[i].isFile()){
                pathArray.add(listfiles[i].getAbsolutePath());
            }*/
            }
        }

        return pathArray;
    }


    public static void searchForDirectoryHasImages(String directory, ArrayList<String> directoriesImages){
        ArrayList<String> imagesPath = getFilePath(directory);
        if(imagesPath.size() > 0){
            // there are images inside (directory)
            directoriesImages.add(directory);
        }

        ArrayList<String> directoriesPath = getDirectoryPath(directory);
        if(directoriesPath.size() > 0){
            // there are directories inside (directory)
            // go check if there are images inside them
            for(String dire : directoriesPath){
                //searchForDirectoryHasImages( dire, directoriesImages);
            }
        }
    }


    public static ArrayList<String> searchForDirectoryHasImages(File file){
        File[] listfiles = file.listFiles();
        ArrayList<String> directoriesHasImages = new ArrayList<>();
        for (int i = 0; i < listfiles.length; i++){
            // is there directories inside it
            if(listfiles[i].isDirectory()){
                ArrayList<String> subdirectories = searchForDirectoryHasImages(listfiles[i]);
                if(subdirectories.size() > 0){
                    directoriesHasImages.addAll(subdirectories);
                }

            }else {
                // are files inside directory images
                if (listfiles[i].getName().endsWith(".png")
                        || listfiles[i].getName().endsWith(".jpg")
                        || listfiles[i].getName().endsWith(".jpeg")
                        || listfiles[i].getName().endsWith(".gif"))
                {
                    if(!directoriesHasImages.contains(file.getAbsolutePath())){
                        directoriesHasImages.add(file.getAbsolutePath());
                    }
                }
            }
        }

        return directoriesHasImages;
    }
}
