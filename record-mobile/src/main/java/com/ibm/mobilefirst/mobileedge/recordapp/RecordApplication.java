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

package com.ibm.mobilefirst.mobileedge.recordapp;

import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.ibm.mobilefirst.mobileedge.MobileEdgeController;
import com.ibm.mobilefirst.mobileedge.connectors.AndroidWear;

public class RecordApplication extends Application {

    AndroidWear androidWearConnector = new AndroidWear();
    public MobileEdgeController controller = new MobileEdgeController();

    public void connectToAndroidWear(Context context){
        controller.connect(context,androidWearConnector);
    }

    static public void showMessage(Context context, String message){
        new AlertDialog.Builder(context)
                .setTitle("Record Application")
                .setMessage(message)
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
