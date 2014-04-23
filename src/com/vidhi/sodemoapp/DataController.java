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
    private static String urlFilterForAnswers = "!7hWRAIcoYpfTUGMcrdgn*F6WwmgZjr3FrY";
    private static  String urlFilterForQuestionDetails = "!OfYlQkPfrYocP)TOiZ67)-ZTkKJ8WDnAei_yPO3P6s2";
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


    public HashMap convertResponseForAnswers(String dataFromServer) {

        HashMap data = new HashMap();
        ArrayList<AnswerInfo> answerInfos = new ArrayList<AnswerInfo>();
        ArrayList<OwnerInfo> ownerInfos = new ArrayList<OwnerInfo>();
        try {

            JSONObject jObject = new JSONObject(dataFromServer);

            JSONArray jsonArray = jObject.getJSONArray("items");

            if (jsonArray.length() == 0) { //empty answer array

                //find if there are actually no answers or what?
                if(Integer.parseInt(jObject.get("total").toString()) == 0){
                    data.put("noAnswers", true);
                }

                data.put("answerData", null);
                data.put("ownerData", null);
                return data;
            }

            //Parse the JSon string obtained and fill up AnswerInfos array
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jObj = jsonArray.getJSONObject(i);
                JSONObject ownerObject = jObj.getJSONObject("owner");

                OwnerInfo ownerInfo = new OwnerInfo();
                AnswerInfo answerInfo = new AnswerInfo();

                if(ownerObject != null){

                    String userType = ownerObject.getString("user_type");
                    if(userType.equals("registered")){

                        ownerInfo.setOwnerDisplayName(ownerObject.getString("display_name"));
                        ownerInfo.setOwnerID(ownerObject.get("user_id").toString());
                        answerInfo.setOwnerID(ownerInfo.getOwnerID());
                        ownerInfo.setOwnerReputation(Integer.parseInt(ownerObject.get("reputation").toString()));
                        ownerInfos.add(ownerInfo);
                    }
                }

                answerInfo.setBodyMarkdown(jObj.get("body_markdown").toString());
                answerInfo.setQuestionRefID(jObj.get("question_id").toString());
                answerInfo.setDownVoteCount(jObj.getInt("down_vote_count"));
                answerInfo.setUpvoteCount(jObj.getInt("up_vote_count"));
                answerInfo.setScore(jObj.getInt("score"));

                answerInfos.add(answerInfo);
            }

        } catch (JSONException e) {
            Log.d(MainActivity.TAG, "Exception occured in convert response for answers:", e);
            return data;
        }
        data.put("noAnswers", false);

        data.put("answerData", answerInfos);
        data.put("ownerData", ownerInfos);

        return data;
    }

    public HashMap convertResponseForQuestionDetails(String dataFromServer){

        ArrayList<QuestionInfo> questionInfos = new ArrayList<QuestionInfo>();
        ArrayList<OwnerInfo> ownerInfos = new ArrayList<OwnerInfo>();
        HashMap data = new HashMap();

        try {

            JSONObject jObject = new JSONObject(dataFromServer);
            JSONArray jsonArray = jObject.getJSONArray("items");

            if (jsonArray.length() == 0) {
                data.put("questionData", null);
                data.put("ownerData", null);
                return data;
            }

            //Parse the JSon string obtained and fill up QuestionInfos array
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jObj = jsonArray.getJSONObject(i);
                QuestionInfo qInfo = new QuestionInfo();
                JSONObject ownerObject = jObj.getJSONObject("owner");
                if(ownerObject != null){

                    String userType = ownerObject.getString("user_type");
                    if(userType.equals("registered")){

                        OwnerInfo ownerInfo = new OwnerInfo();
                        ownerInfo.setOwnerDisplayName(ownerObject.getString("display_name"));
                        ownerInfo.setOwnerID(ownerObject.get("user_id").toString());
                        qInfo.setOwnerID(ownerInfo.getOwnerID());
                        ownerInfo.setOwnerReputation(Integer.parseInt(ownerObject.get("reputation").toString()));
                        ownerInfos.add(ownerInfo);
                    }
                }
                qInfo.setBodyMarkdown(jObj.getString("body_markdown"));
                qInfo.setUpvoteCount(Integer.parseInt(jObj.get("up_vote_count").toString()));
                qInfo.setDownVoteCount(Integer.parseInt(jObj.get("down_vote_count").toString()));
                questionInfos.add(qInfo);
            }

        }catch(JSONException e){
            Log.d(MainActivity.TAG, "Exception occured", e);
            return data;
        }

        data.put("questionsData", questionInfos);
        data.put("ownerData", ownerInfos);
        return data;
    }

    public void sendRequestForAnswers(final HashMap requestData, final Handler callback, final Handler failureHandler){

        HashMap data = new HashMap();
        data.put("filter", urlFilterForAnswers);
        data.put("site", "stackoverflow");

        Handler localSuccessHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){

                boolean noAnswers;
                HashMap dataFromParser;

                HashMap result = (HashMap) msg.obj;
                String response = (String) result.get("response");
                ArrayList<AnswerInfo> answerInfos = null;
                HashMap msgFromData = new HashMap();


                if(response != null){
                    ArrayList<OwnerInfo> ownerInfos = null;
                    dataFromParser = convertResponseForAnswers(response);
                    answerInfos = (ArrayList) dataFromParser.get("answerData");
                    ownerInfos = (ArrayList) dataFromParser.get("ownerData");
                    noAnswers = (Boolean) dataFromParser.get("noAnswers");
                    msgFromData.put("noAnswers", noAnswers);


                    if(ownerInfos != null){
                        dbHandler.addOwners(ownerInfos);
                    }
                }

                msgFromData.put("responseCode", result.get("responseCode"));
                if (answerInfos == null) {
                    Message failureMessage = failureHandler.obtainMessage();
                    failureMessage.obj = msgFromData;
                    failureHandler.sendMessage(failureMessage);

                }else{

                    dbHandler.addAnswers(answerInfos);
                    msgFromData.put("answerInfos", answerInfos);
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
                ArrayList answerInfos = dbHandler.fetchAnswersFromDatabase(requestData.get("questionID").toString());

                if(answerInfos.size() != 0){
                    msgFromData.put("noAnswers", true);
                    msgFromData.put("answerInfos", answerInfos);
                    Message successMessage = callback.obtainMessage();
                    successMessage.obj = msgFromData;
                    callback.sendMessage(successMessage);
                }else{
                    msgFromData.put("noAnswers", false);
                    Message failureMessage = failureHandler.obtainMessage();
                    failureMessage.obj = msgFromData;
                    failureHandler.sendMessage(failureMessage);
                }

            }
        };

        String url = "http://api.stackexchange.com/2.2/questions/" + requestData.get("questionID") + "/answers?";
        httpClient.sendHttpRequestForAnswers("GET", url, data,
                localSuccessHandler, localFailureHandler);
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

    public void sendRequestForQuestionDetails(final HashMap requestData,
                                              final Handler questionDetailsSuccessHandler,
                                              final Handler questionDetailsFailureHandler) {

        final String questionID = requestData.get("questionID").toString();
        HashMap data = new HashMap();
        data.put("filter", urlFilterForQuestionDetails);
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

                      dataFromParser = convertResponseForQuestionDetails(response);
                      questionInfos = (ArrayList) dataFromParser.get("questionsData");
                      ownerInfos = (ArrayList) dataFromParser.get("ownerData");
                  }

                  HashMap msgFromData = new HashMap();
                  msgFromData.put("responseCode", result.get("responseCode"));

                  if (questionInfos == null) {
                      Message failureMessage = questionDetailsFailureHandler.obtainMessage();
                      failureMessage.obj = msgFromData;
                      questionDetailsFailureHandler.sendMessage(failureMessage);

                  }else{

                      if(ownerInfos != null){
                          dbHandler.addOwners(ownerInfos);
                      }
                      dbHandler.addQuestionDetails(questionInfos, questionID );
                  }
                  msgFromData.put("questionsData", questionInfos);
                  msgFromData.put("ownerData", ownerInfos);

                  Message successMessage = questionDetailsSuccessHandler.obtainMessage();
                  successMessage.obj = msgFromData;
                  questionDetailsSuccessHandler.sendMessage(successMessage);
              }
        };

        Handler localFailureHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){

                HashMap result = (HashMap) msg.obj;

                HashMap msgFromData = new HashMap();
                msgFromData.put("responseCode", result.get("responseCode"));

                HashMap data = dbHandler.fetchQuestionDetails(questionID);
                QuestionInfo questionInfo = (QuestionInfo) data.get("questionsData");
                ArrayList questionInfos = new ArrayList();
                questionInfos.add(questionInfo);

               OwnerInfo ownerInfo = (OwnerInfo) data.get("ownerData");
                ArrayList ownerInfos = new ArrayList();
                ownerInfos.add(ownerInfo);

                if(questionInfo != null){
                    msgFromData.put("questionsData", questionInfos);
                    msgFromData.put("ownerData", ownerInfos);
                    Message successMessage = questionDetailsSuccessHandler.obtainMessage();
                    successMessage.obj = msgFromData;
                    questionDetailsSuccessHandler.sendMessage(successMessage);
                }
                else{
                    Message failureMessage = questionDetailsFailureHandler.obtainMessage();
                    failureMessage.obj = msgFromData;
                    questionDetailsFailureHandler.sendMessage(failureMessage);
                }
            }
        };

        httpClient.sendHttpRequestforQuestionDetails("GET", "http://api.stackexchange.com/2.2/questions/"+ questionID +"?", data, localSuccessHandler, localFailureHandler);

    }
}
