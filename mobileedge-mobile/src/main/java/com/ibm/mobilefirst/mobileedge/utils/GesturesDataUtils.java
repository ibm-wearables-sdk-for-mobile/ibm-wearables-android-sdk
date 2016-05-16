/*
 *    © Copyright 2016 IBM Corp.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.ibm.mobilefirst.mobileedge.utils;

import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Utils for handling the recorded gestures
 */
public class GesturesDataUtils {

    static public final String GESTURE_NAME_SUFFIX = "mobileEdge.js";

    static public ArrayList<String> getSavedGestureNames(Context context){

        ArrayList<String> gestureNames = new ArrayList<>();

        File[] files = context.getFilesDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(GESTURE_NAME_SUFFIX);
            }
        });

        //return the files without the suffix
        for (File file : files){
            gestureNames.add(file.getName().substring(0,file.getName().indexOf(GESTURE_NAME_SUFFIX)));
        }

        return gestureNames;
    }

    static public boolean isGestureExists(Context context, String name){
        ArrayList<String> savedGestureNames = getSavedGestureNames(context);

        return savedGestureNames.contains(name);
    }

    static public ArrayList<InputStream> getEnabledGesturesAsInputStream(Context context){

        ArrayList<String> savedGestureNames = getSavedGestureNames(context);
        ArrayList<InputStream> result = new ArrayList<>();

        for (String gesture : savedGestureNames){
            try {
                if (ApplicationPreferences.isGestureEnabled(context,gesture)){
                    result.add(context.openFileInput(gesture + GESTURE_NAME_SUFFIX));
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    static public void downloadGesture(Context context, final String url, String fileName, final DownloadUtils.DownloadResult downloadResult){
        DownloadUtils.downloadFile(context,url,fileName + GESTURE_NAME_SUFFIX, downloadResult);
    }

    static public void deleteGesture(Context context, String gestureName){

        //delete the file from the local storage
        context.deleteFile(gestureName + GESTURE_NAME_SUFFIX);

        //remove the gesture pref
        ApplicationPreferences.removeGesture(context,gestureName);
    }

    public static boolean isGestureEnabled(Context context, String gestureName) {
        return ApplicationPreferences.isGestureEnabled(context,gestureName);
    }
}
