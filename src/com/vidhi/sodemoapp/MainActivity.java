package com.vidhi.sodemoapp; /**
 * Created by vidhi on 3/24/14.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends Activity implements OnItemClickListener {

    public final static String TAG = "SODemoDebug";
    CustomListViewAdapter adapter;
    ListView listView;
    DataController dataController;
    String query;
    ProgressDialog progressDialog;
    ArrayList<QuestionInfo> questionInfos;
    SearchView searchView;
    TextView textViewResult;

    String failureMessageAbsurdQuery = "No results found matching your query. Please check spelling and try again.";
    String failureMessageNoNetwork = "No Response from Server. Please check your connectivity and try again.";
    String failureDefaultMessage = "Cannot retrieve data. Please try again later.";
    String successNoNetwork = "Showing cached data. No connectivity.";
    String progressDialogMessage = "Retrieving data... Please wait";
    String failureDataNotFound = "No data found matching the search query. Please try again.";

    /**
     * Function (override) to create and initialize objects to be used along the whole app - adapters, views, client and data layer objects, etc.
     *
     * @param paramBundle
     */
    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.list_layout);
        this.listView = ((ListView) findViewById(R.id.list));
        this.questionInfos = new ArrayList();
        this.dataController = new DataController(this);
        this.adapter = new CustomListViewAdapter(this, R.layout.single_list_element, this.questionInfos);
        this.listView.setAdapter(this.adapter);
        this.searchView = new SearchView(this);
        this.textViewResult = (TextView) findViewById(R.id.textViewResult);
        this.textViewResult.setVisibility(View.INVISIBLE);
    }


    /**
     * Function to actually initiate the http request by invoking client layer
     * the questionInfos returned by client layer will be checked for null data alongwith network availability
     * to show appropriate feedback to user
     */
    private void getDataForList(Handler successHandler, Handler failureHandler) {
        try {
            this.textViewResult.setVisibility(View.VISIBLE);
            this.textViewResult.setText("Searching...");
            progressDialog = ProgressDialog.show(this, "", progressDialogMessage);
            progressDialog.setCancelable(true);
            dataController.sendRequest(query, successHandler, failureHandler);

        } catch (Exception localException) {
            Log.d(TAG, "Error occured : ", localException);
        }
    }


    /**
     * Function responsible for showing the searchView in MenuBar
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate( R.menu.menu, menu );
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setIconifiedByDefault(true); // the icon will be outside the search view
        this.searchView.setOnQueryTextListener(this.mOnQueryTextListener);
        this.searchView.clearFocus();
        return super.onCreateOptionsMenu(menu);
    }


    private Handler failureHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            HashMap result = (HashMap) msg.obj;
            Integer status = (Integer) result.get("responseCode");
            textViewResult.setVisibility(View.VISIBLE);
            progressDialog.dismiss();
            adapter.clear();

            switch(status){
                case 200 : textViewResult.setText("Result:\n"+failureMessageAbsurdQuery);
                           showToast(failureMessageAbsurdQuery);
                           break;
                case 0   : textViewResult.setText("Result:\n"+failureMessageNoNetwork);
                           showToast(failureMessageNoNetwork);
                           break;
                case 404 : textViewResult.setText("Result:\n"+failureDataNotFound);
                           showToast(failureDataNotFound);
                           break;
                default  : textViewResult.setText("Result:\n"+failureDefaultMessage);
                           showToast(failureDefaultMessage);
                           break;
            }
        }
    };

    private void showToast(final String message){
        runOnUiThread(new Runnable() {
            public void run() {
                Toast localToast = Toast.makeText(MainActivity.this, message, 0);
                localToast.setGravity(18,0,0);
                localToast.show();
            }
        });
    }

    private Handler successHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            adapter.clear();
            HashMap result = (HashMap) msg.obj;
            questionInfos = (ArrayList) result.get("response");

            if ((questionInfos != null) && (questionInfos.size() > 0)) {
                adapter.notifyDataSetChanged();
                for (int i = 0; i < questionInfos.size(); i++)
                    adapter.add(questionInfos.get(i));
            }

            progressDialog.dismiss();
            textViewResult.setText("Result:\n");
            adapter.notifyDataSetChanged();
            int responseCode = Integer.parseInt(result.get("responseCode").toString());

            if(responseCode != 200){
                textViewResult.setText("Result:\n"+successNoNetwork);
                showToast(successNoNetwork);
            }
        }
    };


    /**
     * Listener for query text entry in searchView by user.
     * When user is done entering the query, this function starts the process of sending http request on a new thread
     */
    private final SearchView.OnQueryTextListener mOnQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextChange(String query) {
            return true;
        }

        @Override
        public boolean onQueryTextSubmit(String userQuery) {

            query = userQuery.replace("\"","\\\"");
            searchView.clearFocus();
            getDataForList(successHandler, failureHandler);
            return true;
        }
    };

    /**
     * Does nothing for now. For future extension
     *
     * @param paramAdapterView
     * @param paramView
     * @param paramInt
     * @param paramLong
     */
    @Override
    public void onItemClick(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {

    }
}