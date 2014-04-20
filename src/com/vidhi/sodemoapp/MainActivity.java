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

import static android.widget.AbsListView.OnScrollListener;

public class MainActivity extends Activity {

    public final static String TAG = "SODemoDebug";
    public final static String LIST_INSTANCE_STATE = "listInstanceState";
    CustomListViewAdapter adapter;
    ListView listView;
    DataController dataController;
    String query;
    ProgressDialog progressDialog;
    ArrayList<QuestionInfo> questionInfos;
    SearchView searchView;
    TextView textViewResult;
    int prevPage;

    String failureMessageAbsurdQuery = "No results found matching your query. Please check spelling and try again.";
    String failureMessageNoNetwork = "No Response from Server. Please check your connectivity and try again.";
    String failureDefaultMessage = "Cannot retrieve data. Please try again later.";
    String successNoNetwork = "Showing cached data. No connectivity.";
    String progressDialogMessage = "Retrieving data... Please wait";
    String failureDataNotFound = "No data found matching the search query. Please try again.";

    int prevTotalCount;
    int pageSize = 15;
    int page = 1;
    boolean hasMore = false;

    /**
     * Function (override) to create and initialize objects to be used along the whole app - adapters, views, client and data layer objects, etc.
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_layout);
        this.listView = ((ListView) findViewById(R.id.list));
        this.questionInfos = new ArrayList();
        this.dataController = new DataController(this);
        this.adapter = new CustomListViewAdapter(this, R.layout.single_list_element, this.questionInfos);
        this.listView.setAdapter(this.adapter);
        this.listView.setOnScrollListener(customScrollListener);
        this.listView.setOnItemClickListener(customOnItemClickListener);
        this.searchView = new SearchView(this);
        this.textViewResult = (TextView) findViewById(R.id.textViewResult);
        this.textViewResult.setVisibility(View.INVISIBLE);

        if(savedInstanceState != null) {

            Log.d(MainActivity.TAG,"savedInstanceState not null");
            questionInfos = (ArrayList) savedInstanceState.get(LIST_INSTANCE_STATE);
            adapter.notifyDataSetChanged();
        }
    }

  public AbsListView.OnScrollListener customScrollListener = new OnScrollListener(){
        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {
        }

        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            page = ((firstVisibleItem + visibleItemCount) /pageSize) +1;
            Log.d(MainActivity.TAG, "page in scrollhandler =" + page + "firstvisibleItem +visibleitemcount ="+(firstVisibleItem+visibleItemCount) + "totalItemCount"+totalItemCount + "prevpage ="+prevPage);

            if(hasMore){ // when user reached bottom of list
                final int lastItem = firstVisibleItem + visibleItemCount;
                if(lastItem == totalItemCount) {
                    if (totalItemCount == 0 || adapter == null)
                        return;
                    if (prevTotalCount == totalItemCount)
                        return;
                    boolean loadMore = firstVisibleItem + visibleItemCount >= totalItemCount;
                    if (loadMore){

                        Log.d(TAG, "reached the end of list...");
                        getDataForList(successHandler,failureHandler, false);
                        prevTotalCount = totalItemCount;
                    }
                }
            }
            if(prevPage > page){
                //refetch this page and notify adapter dataset change
                Log.d(MainActivity.TAG,"prevpage is > page. PrevPage=" + prevPage);
                getDataForList(successHandler, failureHandler, true);
            }

        }

    };


    /**
     * Function to actually initiate the http request by invoking client layer
     * the questionInfos returned by client layer will be checked for null data alongwith network availability
     * to show appropriate feedback to user
     */
    private void getDataForList(Handler successHandler, Handler failureHandler, boolean fromNavigatePreloadedList) {
        prevPage = page; // save the current page to detect scroll up in onScrollListener
        try {
            this.textViewResult.setVisibility(View.VISIBLE);
            this.textViewResult.setText("Searching...");
            progressDialog = ProgressDialog.show(this, "", progressDialogMessage);
            HashMap requestData = new HashMap();
            requestData.put("query", query);
            Log.d(MainActivity.TAG , "page sending ="+page);
            requestData.put("page", page);
            requestData.put("pageSize", pageSize);
            if(fromNavigatePreloadedList){
                requestData.put("refreshList", fromNavigatePreloadedList);
            }
            dataController.sendRequestforQuestions(requestData, successHandler, failureHandler);progressDialog.setCancelable(true);

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

            page = Integer.parseInt(result.get("page").toString());
            if((Boolean)result.get("has_more")){
                hasMore = true;
            }
            Log.d(MainActivity.TAG, "has_more" + hasMore);
            textViewResult.setVisibility(View.VISIBLE);
            progressDialog.dismiss();
            adapter.clear();
            switch(status){
                case 200 : textViewResult.setText("Result:\n" + failureMessageAbsurdQuery);
                           showToast(failureMessageAbsurdQuery);
                           adapter.clear();
                           break;
                case 0   : textViewResult.setText("Result:\n"+failureMessageNoNetwork);
                           showToast(failureMessageNoNetwork);
                           break;
                case 404 : textViewResult.setText("Result:\n" + failureDataNotFound);
                           showToast(failureDataNotFound);
                           adapter.clear();
                           break;
                default  : textViewResult.setText("Result:\n" + failureDefaultMessage);
                           showToast(failureDefaultMessage);
                           adapter.clear();
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
            HashMap result = (HashMap) msg.obj;
            questionInfos = (ArrayList) result.get("response");
            adapter.removeIfExists(page);
            if ((questionInfos != null) && (questionInfos.size() > 0)) {
                for (int i = 0; i < questionInfos.size(); i++){
                   adapter.add(questionInfos.get(i));
                }
            }
            hasMore = (Boolean) result.get("has_more");
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
            if(query != null){
                if(!(query.equals(userQuery))){
                   page = 1;
                   adapter.clear();
                }
            }
            query = userQuery.replace("\"","\\\"");
            getDataForList(successHandler, failureHandler, false);
            searchView.clearFocus();
            return true;
        }
    };

  @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d(MainActivity.TAG,"onSaveInstanceState");

        super.onSaveInstanceState(savedInstanceState);

        Bundle instanceMap = new Bundle();
        instanceMap.putParcelableArrayList(LIST_INSTANCE_STATE, adapter.questionInfos);
        savedInstanceState.putAll(instanceMap);


    }

    @Override
    public void onPause() {
        Log.d(MainActivity.TAG, "onPause");

        super.onPause();  // Always call the superclass method first

    }

    @Override
    public void onResume() {
        Log.d(MainActivity.TAG, "onResume");

        super.onResume();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        Log.d(MainActivity.TAG, "onRestorInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
        questionInfos = (ArrayList) savedInstanceState.get(LIST_INSTANCE_STATE);

        adapter.notifyDataSetChanged();
    }
    /**
     * @param adapterView
     * @param view
     * @param viewPosition
     * @param itemID
     */
    public OnItemClickListener customOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int viewPosition, long itemID) {

        }
    };
}
