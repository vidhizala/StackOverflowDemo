package com.vidhi.sodemoapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;
import us.feras.mdv.MarkdownView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by vidhi on 4/19/14.
 */
public class DetailActivity extends Activity {

    ExpandableListView expandableListView;
    MarkdownView markdownView;
    ArrayList<AnswerInfo> answerCollection;
    DataController dataController;
    TextView loadingTextView;
    AnswerAdapter expListAdapter;
    String failureMessage = "Cannot fetch answer data. Please try again later.";
    String noAnswersMessage = "No answers for this question yet.";
    String failureQuestionDetails = "No details for this question available";


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        final Bundle extras = intent.getExtras();
        dataController = new DataController(this);
        answerCollection = new ArrayList<AnswerInfo>();
        setContentView(R.layout.detail_layout);
        markdownView = (MarkdownView) findViewById(R.id.questionDetailView);
        expandableListView = (ExpandableListView) findViewById(R.id.answer_list);
        loadingTextView = (TextView) findViewById(R.id.loadingTextView);
        loadingTextView.setVisibility(View.VISIBLE);
        expListAdapter = new AnswerAdapter(answerCollection, this);
        expandableListView.setAdapter(expListAdapter);
        getActionBar().setDisplayHomeAsUpEnabled(true);


        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int previousGroup = -1;

            @Override
            public void onGroupExpand(int groupPosition) {
                if(groupPosition != previousGroup)
                    expandableListView.collapseGroup(previousGroup);
                previousGroup = groupPosition;
            }
        });
        new Thread(new Runnable() {
            public void run()
            {
                Looper.prepare();
                populateQuestionDetails(extras.get("questionID").toString());
                populateAnswerList(extras.get("questionID").toString());
                Looper.loop();
            }
        }).start();

    }

    public void populateQuestionDetails(final String questionID){

        HashMap requestData = new HashMap();
        requestData.put("questionID", questionID);
        dataController.sendRequestForQuestionDetails(requestData, questionDetailsSuccessHandler, questionDetailsFailureHandler);

    }

    public void populateAnswerList(final String questionID){

        HashMap requestData = new HashMap();
        requestData.put("questionID", questionID);
        dataController.sendRequestForAnswers(requestData, answerListSuccessHandler, answerListFailureHandler);

    }


    private Handler answerListSuccessHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            HashMap result = (HashMap) msg.obj;
            answerCollection = (ArrayList) result.get("answerInfos");
            loadingTextView.setVisibility(View.GONE);
            expandableListView.setAdapter(new AnswerAdapter(answerCollection, DetailActivity.this));
            expListAdapter.notifyDataSetChanged();
            boolean noAnswers = (Boolean) result.get("noAnswers");
            int responseCode = Integer.parseInt(result.get("responseCode").toString());

            if(responseCode == 200 && noAnswers){
                // response OK but no answers for this question
                showToast(noAnswersMessage);
            }

            if(responseCode != 200){
                showToast(failureMessage);
            }

        }
    };

    private Handler answerListFailureHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            HashMap result = (HashMap) msg.obj;
            loadingTextView.setVisibility(View.GONE);
            boolean noAnswers = (Boolean) result.get("noAnswers");
            int responseCode = Integer.parseInt(result.get("responseCode").toString());
            if(responseCode == 200 && noAnswers){
                // response OK but no answers for this question
                showToast(noAnswersMessage);
            }

            if(responseCode != 200){
                showToast(failureMessage);
            }
        }
    };

    private Handler questionDetailsSuccessHandler = new Handler(){

        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            HashMap result = (HashMap) msg.obj;
            ArrayList<QuestionInfo> questionInfos = (ArrayList) result.get("questionsData");
            QuestionInfo questionInfo = questionInfos.get(0);

            ArrayList <OwnerInfo> ownerData = (ArrayList) result.get("ownerData");
            OwnerInfo ownerInfo = ownerData.get(0); // ownerInfo to be used when owner display functionality is implemented

            markdownView.loadMarkDownData(questionInfo.getBodyMarkdown());

        }
    };

    private Handler questionDetailsFailureHandler = new Handler(){

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            HashMap result = (HashMap) msg.obj;
            int responseCode = Integer.parseInt(result.get("responseCode").toString());

            if(responseCode != 200){
                showToast(failureQuestionDetails);
            }

        }
    };

    private void showToast(final String message){
        runOnUiThread(new Runnable() {
            public void run() {
                Toast localToast = Toast.makeText(DetailActivity.this, message, 0);
                localToast.setGravity(18,0,0);
                localToast.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);
        menu.findItem(R.id.action_search).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        this.finish();
        super.onBackPressed();
    }

}