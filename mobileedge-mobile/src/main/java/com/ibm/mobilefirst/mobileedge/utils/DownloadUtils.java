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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadUtils {

    static public void downloadFile(Context context, final String url, String fileName, final DownloadResult downloadResult){

        try {
            final FileOutputStream fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);

            ExecutorService executorService = Executors.newCachedThreadPool();
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    executeDownloadFile(url, fileOutputStream,downloadResult);
                }
            });

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            downloadResult.onFailure("Inner Exception");
        }
    }

    static private void executeDownloadFile(String fileUrl, OutputStream outputStream,DownloadResult downloadResult){

        InputStream input = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(fileUrl);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("GET");
            connection.connect();

            int responseCode = connection.getResponseCode();

            String msg = connection.getResponseMessage();

            if (responseCode == HttpURLConnection.HTTP_OK) {

                // download the file
                input = connection.getInputStream();
                //output = new FileOutputStream("/sdcard/file_name.extension");

                byte data[] = new byte[4096];
                long total = 0;
                int count;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    outputStream.write(data, 0, count);
                }

                downloadResult.onSuccess();
            }

            else{
                downloadResult.onFailure("Wrong response code (Code:" + responseCode + ")");
            }
        } catch (Exception e) {

        } finally {
            try {
                if (input != null){
                    input.close();
                }
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }
    }


    public interface DownloadResult{
        void onSuccess();
        void onFailure(String error);
    }
}
