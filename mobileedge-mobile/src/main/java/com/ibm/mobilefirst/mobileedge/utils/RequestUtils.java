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

import android.util.Log;

import com.ibm.mobilefirst.mobileedge.abstractmodel.AccelerometerData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.GyroscopeData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

public class RequestUtils {

    static private ExecutorService executorService = Executors.newCachedThreadPool();

    public static void sendTrainRequest(final String name, final String uuid, final List<AccelerometerData> accelerometerData, final List<GyroscopeData> gyroscopeData, final RequestResult requestResult) {
        
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    executeTrainRequest(name,uuid,accelerometerData,gyroscopeData,requestResult);
                } catch (IOException e) {
                    e.printStackTrace();
                    requestResult.onFailure("Exception");
                }
            }
        });
    }

    public static void executeTrainRequest(String name, String uuid, List<AccelerometerData> accelerometerData, List<GyroscopeData> gyroscopeData, RequestResult requestResult) throws IOException {

        URL url = new URL("https://medge.mybluemix.net/alg/train");

        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setReadTimeout(5000);
        conn.setConnectTimeout(10000);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        conn.setDoInput(true);
        conn.setDoOutput(true);

        JSONObject jsonToSend = createTrainBodyJSON(name,uuid,accelerometerData,gyroscopeData);


        OutputStream outputStream = conn.getOutputStream();
        DataOutputStream wr = new DataOutputStream(outputStream);
        wr.writeBytes(jsonToSend.toString());
        wr.flush();
        wr.close();

        outputStream.close();

        String response = "";

        int responseCode=conn.getResponseCode();

        //Log.e("BBB2","" + responseCode);

        if (responseCode == HttpsURLConnection.HTTP_OK) {
            String line;
            BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line=br.readLine()) != null) {
                response+=line;
            }
        }
        else {
            response="{}";
        }

        handleResponse(response,requestResult);
    }

    private static void handleResponse(String response, RequestResult requestResult) {
        JSONObject jsonResult = responseAsJSON(response);

        Log.d("JSON Responce",jsonResult.toString());
        if (jsonResult.has("error")){
            requestResult.onFailure(jsonResult.optString("error"));
        }
        else{
            requestResult.onSuccess(jsonResult.optString("id"),jsonResult.optString("jsURI"));
        }
    }

    private static JSONObject responseAsJSON(String response) {
        int x = 0;
        Log.e("Train Result",response);

        JSONObject jsonResult = null;

        try {
            jsonResult = new JSONObject(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonResult;
    }

    private static JSONObject createTrainBodyJSON(String name, String id, List<AccelerometerData> accelerometerData, List<GyroscopeData> gyroscopeData) {

        JSONObject jsonToSend = new JSONObject();

        try {
            jsonToSend.put("name",name);
            jsonToSend.put("rawData",createJSONData(accelerometerData,gyroscopeData));
            jsonToSend.put("id",id);
            jsonToSend.put("imageFile", "nofile");
            jsonToSend.put("videoFile", "nofile");
            jsonToSend.put("metaData", false);
            jsonToSend.put("isPublic", false);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonToSend;
    }



    private static JSONObject createJSONData(List<AccelerometerData> accelerometerData, List<GyroscopeData> gyroscopeData) {

        JSONObject result = new JSONObject();

        JSONArray accelerometerArray = new JSONArray();
        for (AccelerometerData data : accelerometerData){

            JSONArray jsonArray = new JSONArray();

            try {
                jsonArray.put(data.x);
                jsonArray.put(data.y);
                jsonArray.put(data.z);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            accelerometerArray.put(jsonArray);
        }

        JSONArray gyroscopeArray = new JSONArray();


        for (GyroscopeData data : gyroscopeData){

            JSONArray jsonArray = new JSONArray();

            try {
                jsonArray.put(data.x);
                jsonArray.put(data.y);
                jsonArray.put(data.z);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            gyroscopeArray.put(jsonArray);
        }

        try {
            result.put("accelerometer",accelerometerArray);
            result.put("gyroscope",gyroscopeArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }


    public interface RequestResult{
        void onSuccess(String id, String jsUrl);
        void onFailure(String error);
    }


    /*

    public static String performPostCall(String requestURL, HashMap<String, String> postDataParams) {

        URL url;
        String response = "";
        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //conn.setReadTimeout(5000);
            //conn.setConnectTimeout(55000);
            conn.setRequestMethod("POST");

            //conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();

            conn.getResponseMessage();
            int responseCode=conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response+=line;
                }
            }
            else {
                response="";

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }


    private static String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }
    */
}
