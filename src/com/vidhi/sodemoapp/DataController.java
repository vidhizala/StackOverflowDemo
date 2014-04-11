package com.vidhi.sodemoapp;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by vidhi on 4/11/14.
 */
public class DataController {

    DBHandler dbHandler;
    HttpClient httpClient;
    public DataController(Context context){
        dbHandler = new DBHandler (context, null, null, 1);
        httpClient = new HttpClient();
    }

    /**
     * Function to convert the response string obtained from HttpRequest sent
     * Conversion results in  list of QuestionInfo objects which can be passed to dbHandler fro storing in database
     *
     * @param returnData
     * @return
     */
    public ArrayList convertResponse(String returnData) {
        ArrayList<QuestionInfo> questionInfos = new ArrayList();
        try {

            JSONObject jObject = new JSONObject(returnData);
            JSONArray jsonArray = jObject.getJSONArray("items");

            if (jsonArray.length() == 0) { //empty questions array in case of absurd search query.
                return null;
            }

            //Parse the JSon string obtained and fill up QuestionInfos array
            for (int i = 0; i < jsonArray.length(); i++) {
                QuestionInfo qInfo = new QuestionInfo();
                String tempTags[] = new String[5];

                JSONObject jObj = jsonArray.getJSONObject(i);
                qInfo.setQuestion(jObj.get("title").toString());
                qInfo.setScore(jObj.get("score").toString());

                JSONArray tagsArray = jObj.getJSONArray("tags");
                for (int j = 0; j < tagsArray.length(); j++) {

                    tempTags[j] = tagsArray.get(j).toString();
                }
                qInfo.setTags(tempTags);
                questionInfos.add(qInfo);
            }

        } catch (Exception e) {
            Log.d(MainActivity.TAG, "Exception occured", e);

            return null;
        }

        return questionInfos;

    }

    public void sendRequest(final String query, final Handler callback, final Handler failureHandler){

        HashMap data = new HashMap();
        data.put("query", query);
        data.put("site", "stackoverflow");

        Handler localSuccessHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){

                HashMap result = (HashMap) msg.obj;
                String response = (String) result.get("response");

                ArrayList<QuestionInfo> questionInfos = convertResponse(response);

                HashMap msgFromData = new HashMap();
                msgFromData.put("responseCode", result.get("responseCode"));

                if (questionInfos == null) {
                    Message failureMessage = failureHandler.obtainMessage();
                    failureMessage.obj = msgFromData;
                    failureHandler.sendMessage(failureMessage);

                }else{
                    Long returnId = dbHandler.addToMaster(query);

                    for (int i = 0; i < questionInfos.size(); i++)
                        dbHandler.addQuestion((QuestionInfo) questionInfos.get(i), returnId);
                    msgFromData.put("response", questionInfos);
                    Message successMessage = callback.obtainMessage();
                    successMessage.obj = msgFromData;
                    callback.sendMessage(successMessage);
                }

            }
        };

        Handler localFailureHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){

                HashMap result = (HashMap) msg.obj;
                HashMap msgFromData = new HashMap();
                msgFromData.put("responseCode", result.get("responseCode"));

                ArrayList<QuestionInfo> questionInfos = dbHandler.fetchFromDatabase(query);

                if(questionInfos.size() == 0){

                    Message failureMessage = failureHandler.obtainMessage();
                    failureMessage.obj = msgFromData;
                    failureHandler.sendMessage(failureMessage);

                }else{
                    msgFromData.put("response", questionInfos);
                    Message successMessage = callback.obtainMessage();
                    successMessage.obj = msgFromData;
                    callback.sendMessage(successMessage);
                }
            }
        };

        httpClient.sendHttpRequest("GET", "http://api.stackexchange.com/2.2/search?", data, localSuccessHandler, localFailureHandler);

    }

}
