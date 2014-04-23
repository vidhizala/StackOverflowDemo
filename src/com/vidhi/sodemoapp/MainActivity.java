package com.vidhi.sodemoapp; /**
 * Created by vidhi on 3/24/14.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
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
    CustomListViewAdapter adapter;
    ListView listView;
    DataController dataController;
    String query;
    ArrayList<QuestionInfo> questionInfos;
    SearchView searchView;
    TextView footerTextView;
    View footerView;
    int prevPage;
    boolean staleFlag;

    String failureMessageAbsurdQuery = "No results found matching your query. Please check spelling and try again.";
    String failureMessageNoNetwork = "No Response from Server. Please check your connectivity and try again.";
    String failureDefaultMessage = "Cannot retrieve data. Please try again later.";
    String successNoNetwork = "Showing cached data. No connectivity.";
    String failureDataNotFound = "No data found matching the search query. Please try again.";
    String initMessage = "Type a query to start search";
    String loadingMessage = "Loading more...";
    String loadingFirstMessage = "Loading results...";
    String loadingDone = "No more results";

    int pageSize = 15;
    int page = 1;
    boolean hasMore = false;
    boolean loadingMore;
    int prevTotalCount;

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
        this.footerView = ((LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footerview_layout, null, false);
        this.listView.addFooterView(footerView);
        this.footerTextView = (TextView) findViewById(R.id.footerTextView);
        this.footerView.setEnabled(false);
        this.footerTextView.setText(initMessage);
        this.questionInfos = new ArrayList();
        this.dataController = new DataController(this);
        this.adapter = new CustomListViewAdapter(this, R.layout.single_list_element, this.questionInfos);
        this.listView.setAdapter(this.adapter);
        this.listView.setOnScrollListener(customScrollListener);
        this.listView.setOnItemClickListener(customOnItemClickListener);
        this.searchView = new SearchView(this);

    }

    public AbsListView.OnScrollListener customScrollListener = new OnScrollListener(){
        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {
        }

        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            page = ((firstVisibleItem + visibleItemCount-1) /pageSize) +1;
            Log.d(MainActivity.TAG, "page ="+page+ "\n hasMore ="+hasMore+ "\n staleFlag="+staleFlag+ "\n loadingMore="+loadingMore);
            if(hasMore) {
                // when user reached bottom of list
                final int lastItem = firstVisibleItem + visibleItemCount;
                if(lastItem == totalItemCount) {
                    if (totalItemCount == 0 || adapter == null) {
                        return;
                    }

                    boolean loadMore = firstVisibleItem + visibleItemCount + 1 >= totalItemCount;
                    Log.d(MainActivity.TAG, "loadMre =" +loadMore+"\n LoadingMore ="+loadingMore);
                    if (loadMore && (!loadingMore) && (prevTotalCount != totalItemCount)) {
                        getDataForList(successHandler,failureHandler, false);
                        prevTotalCount = totalItemCount;
                    }
                }
            }
            if(prevPage > page && staleFlag){
                //refetch this page and notify adapter dataset change
                getDataForList(successHandler, failureHandler, true);
            }

        }

    };

    /**
     * Function to actually initiate the http request by invoking client layer
     * the questionInfos returned by client layer will be checked for null data alongwith network availability
     * to show appropriate feedback to user
     */
    private void getDataForList(final Handler successHandler, final Handler failureHandler, final boolean fromNavigatePreloadedList) {
        prevPage = page; // save the current page to detect scroll up in onScrollListener
        loadingMore = true;
        new Thread(new Runnable() {
            public void run()
            {
                try {

                    Looper.prepare();

                    HashMap requestData = new HashMap();
                    requestData.put("query", query);
                    requestData.put("page", page);
                    requestData.put("pageSize", pageSize);
                    if(fromNavigatePreloadedList){
                        requestData.put("refreshList", fromNavigatePreloadedList);
                    }
                    dataController.sendRequestforQuestions(requestData, successHandler, failureHandler);

                    Looper.loop();

                } catch (Exception localException) {
                    Log.d(TAG, "Error occured : ", localException);
                }
            }
        }).start();
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
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);

            runOnUiThread(new Runnable() {
                public void run() {

                    HashMap result = (HashMap) msg.obj;
                    Integer status = (Integer) result.get("responseCode");

                    page = Integer.parseInt(result.get("page").toString());

                    if(result.containsKey("has_more")){
                        hasMore = (Boolean)result.get("has_more");
                    }
                    if(result.containsKey("staleFlag")){
                        staleFlag = (Boolean)result.get("staleFlag");
                    }

                    switch (status) {
                        case 200:
                            showToast(failureMessageAbsurdQuery);
                            adapter.clear();
                            break;
                        case 0:
                            showToast(failureMessageNoNetwork);
                            break;
                        case 404:
                            showToast(failureDataNotFound);
                            adapter.clear();
                            break;
                        default:
                            showToast(failureDefaultMessage);
                            adapter.clear();
                            break;
                    }
                    loadingMore = false;

                    if(listView.getCount() == 1){
                        footerTextView.setText(initMessage);
                    }else if(hasMore == false){
                        footerTextView.setText(loadingDone);
                    }else{
                        footerTextView.setText(loadingMessage);
                    }

                }
            });
        }
    };

    private void showToast(final String message){
        runOnUiThread(new Runnable() {
            public void run() {
                Toast localToast = Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT);
                localToast.setGravity(18, 0, 0);
                localToast.show();
            }
        });
    }

    private Handler successHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final HashMap result = (HashMap) msg.obj;
            staleFlag = (Boolean) result.get("staleFlag");
            hasMore = (Boolean) result.get("has_more");
            questionInfos = (ArrayList) result.get("response");
            adapter.removeIfExists(page);

            addQuestionsToList(questionInfos);

            int responseCode = Integer.parseInt(result.get("responseCode").toString());
            if(responseCode != 200){
                showToast(successNoNetwork);
            }
            if(listView.getCount() == 1){
                footerTextView.setText(initMessage);
            }else if(hasMore == false){
                footerTextView.setText(loadingDone);
            }else{
                footerTextView.setText(loadingMessage);
            }

        }
    };

    public void addQuestionsToList(final ArrayList<QuestionInfo> questionInfos){

        runOnUiThread(new Runnable() {

            public void run() {
                Log.d(MainActivity.TAG, "Thread for updating adapter from successHandler =" + Thread.currentThread().getId());

                if ((questionInfos != null) && (questionInfos.size() > 0)) {
                    for (int i = 0; i < questionInfos.size(); i++) {
                        adapter.add(questionInfos.get(i));
                    }
                }
                adapter.notifyDataSetChanged();
                loadingMore = false;
            }
        });
    }


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
                   staleFlag = false;
                   adapter.clear();
                }
            }
            query = userQuery.replace("\"","\\\"");
            footerTextView.setText(loadingFirstMessage);
            getDataForList(successHandler,failureHandler, false);
            searchView.clearFocus();
            return true;
        }
    };


    public void startDetailActivity (QuestionInfo questionInfo) {
        if(questionInfo != null){
            Intent questionDetail = new Intent(this, DetailActivity.class);
            questionDetail.putExtra("questionTitle", questionInfo.getQuestion());
            questionDetail.putExtra("questionScore", questionInfo.getScore() );
            questionDetail.putExtra("questionID", questionInfo.getQuestionID() );

            startActivity(questionDetail);
        }
    }


    public OnItemClickListener customOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int viewPosition, long itemID) {
            startDetailActivity((QuestionInfo) adapterView.getItemAtPosition(viewPosition));

        }
    };
}
