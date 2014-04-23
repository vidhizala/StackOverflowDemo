package com.vidhi.sodemoapp;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by vidhi on 4/11/14.
 */
public class DataController {

    private static String urlFilterForQuestions = "!-MOiNm40F1s8yWmhD820n.tAO2qXhr_1R";
    private DBHandler dbHandler;
    private HttpClient httpClient;
    boolean has_more;
    boolean staleFlag;
    String query;
    int pageSize;
    int page;

    public DataController(Context context){
        dbHandler = new DBHandler (context, null, null, 1);
        httpClient = new HttpClient();
        has_more = false;
    }

    /**
     * Function to convert the response string obtained from HttpRequest sent
     * Conversion results in  list of QuestionInfo objects which can be passed to dbHandler fro storing in database
     *
     * @param dataFromServer
     * @return
     */
    public HashMap convertResponseForQuestions(String dataFromServer) {
        ArrayList<QuestionInfo> questionInfos = new ArrayList<QuestionInfo>();
        ArrayList<OwnerInfo> ownerInfos = new ArrayList<OwnerInfo>();
        HashMap data = new HashMap();

        try {

            JSONObject jObject = new JSONObject(dataFromServer);
            String page = jObject.get("page").toString();
            has_more = jObject.getBoolean("has_more");
            JSONArray jsonArray = jObject.getJSONArray("items");

            if (jsonArray.length() == 0) { //empty questions array in case of absurd search query.
                data.put("questionData", null);
                data.put("ownerData", null);
                return data;
            }

            //Parse the JSon string obtained and fill up QuestionInfos array
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jObj = jsonArray.getJSONObject(i);
                QuestionInfo qInfo = new QuestionInfo();

                String tempTags[] = new String[5];
                qInfo.setQuestion(jObj.get("title").toString());
                qInfo.setScore(jObj.get("score").toString());
                qInfo.setQuestionID(jObj.get("question_id").toString());
                qInfo.setPage(page);

                JSONArray tagsArray = jObj.getJSONArray("tags");
                for (int j = 0; j < tagsArray.length(); j++) {

                    tempTags[j] = tagsArray.get(j).toString();
                }
                qInfo.setTags(tempTags);
                questionInfos.add(qInfo);
            }

        } catch (JSONException e) {
            Log.d(MainActivity.TAG, "Exception occured", e);
            return data;
        }

        data.put("questionsData", questionInfos);
        data.put("ownerData", ownerInfos);
        return data;

    }

    public void sendRequestforQuestions(final HashMap requestData, final Handler callback, final Handler failureHandler){

        page = Integer.parseInt(requestData.get("page").toString());
        query = requestData.get("query").toString();
        pageSize = Integer.parseInt(requestData.get("pageSize").toString());

        HashMap data = new HashMap();
        data.put("page", page);
        data.put("query", query);
        data.put("pagesize", pageSize);
        data.put("filter", urlFilterForQuestions);
        data.put("site", "stackoverflow");

        Handler localSuccessHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){

                HashMap dataFromParser;

                HashMap result = (HashMap) msg.obj;
                String response = (String) result.get("response");
                ArrayList<QuestionInfo> questionInfos = null;
                ArrayList<OwnerInfo> ownerInfos = null;

                if(response != null){

                   dataFromParser = convertResponseForQuestions(response);
                   questionInfos = (ArrayList) dataFromParser.get("questionsData");
                   ownerInfos = (ArrayList) dataFromParser.get("ownerData");

                }

                HashMap msgFromData = new HashMap();
                msgFromData.put("responseCode", result.get("responseCode"));
                msgFromData.put("has_more", has_more);
                msgFromData.put("page", page);

                if (questionInfos == null) {
                    Message failureMessage = failureHandler.obtainMessage();
                    failureMessage.obj = msgFromData;
                    failureHandler.sendMessage(failureMessage);

                }else{
                    boolean refreshList = false;

                    if(requestData.containsKey("refreshList")){
                        refreshList = (Boolean) requestData.get("refreshList");
                    }

                    if(ownerInfos != null){
                        dbHandler.addOwners(ownerInfos);
                    }

                    // user has traversed to page 1 from prefetched list, then refreshList = true in that case.
                    // So no need to make a master entry
                    if (page == 1 && refreshList == false){
                        dbHandler.addToMaster(query);
                        staleFlag = false;
                    }
                    if(staleFlag){
                        dbHandler.removeStalePageifExists(page, query);
                    }

                    dbHandler.addQuestions(questionInfos, query);


                    msgFromData.put("staleFlag", staleFlag);
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

                // it came from db so everything needs to be refetched when network is ON again.
                // set stale flag to set this state
                staleFlag = true;

                HashMap result = (HashMap) msg.obj;
                HashMap msgFromData = new HashMap();
                msgFromData.put("responseCode", result.get("responseCode"));
                msgFromData.put("page", page);
                msgFromData.put("staleFlag", staleFlag);

                int limitFactor = (page - 1)*pageSize;

                HashMap dataFromDbHandler = dbHandler.fetchQuestionsFromDatabase(query, limitFactor, pageSize);
                ArrayList questionInfos = (ArrayList) dataFromDbHandler.get("data");

                if(questionInfos.size() != 0){

                    msgFromData.put("has_more", dataFromDbHandler.get("has_more"));
                    msgFromData.put("response", questionInfos);

                    Message successMessage = callback.obtainMessage();
                    successMessage.obj = msgFromData;
                    callback.sendMessage(successMessage);

                }else{
                    msgFromData.put("has_more", false);
                    Message failureMessage = failureHandler.obtainMessage();
                    failureMessage.obj = msgFromData;
                    failureHandler.sendMessage(failureMessage);
                }

            }
        };

        httpClient.sendHttpRequestforQuestions("GET", "http://api.stackexchange.com/2.2/search?", data,
                localSuccessHandler, localFailureHandler);

    }

}
