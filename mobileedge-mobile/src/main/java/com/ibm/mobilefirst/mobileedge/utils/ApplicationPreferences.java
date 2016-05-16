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
import android.content.SharedPreferences;

public class ApplicationPreferences {

    static final String PREF_NAME = "RecordApplicationPrefs";

    static public void setGestureEnabled(Context context, String gestureName, boolean isEnabled){
        SharedPreferences.Editor editor = getSharedPrefEditor(context);
        editor.putBoolean(gestureName,isEnabled);
        editor.commit();
    }

    static public boolean isGestureEnabled(Context context, String gestureName){
        SharedPreferences sharedPref = getSharedPref(context);
        return sharedPref.getBoolean(gestureName,true);
    }

    static public void removeGesture(Context context, String gestureName){
        SharedPreferences.Editor sharedPrefEditor = getSharedPrefEditor(context);
        sharedPrefEditor.remove(gestureName);
        sharedPrefEditor.commit();
    }

    static private SharedPreferences getSharedPref(Context context){
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    static private SharedPreferences.Editor getSharedPrefEditor(Context context){
        return getSharedPref(context).edit();
    }
}
