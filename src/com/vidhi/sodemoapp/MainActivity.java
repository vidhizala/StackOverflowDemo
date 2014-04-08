package com.vidhi.sodemoapp; /**
 * Created by vidhi on 3/24/14.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.ArrayList;

public class MainActivity extends Activity implements OnItemClickListener {

    public final static String TAG = "SODemoDebug";
    CustomListViewAdapter adapter;
    ListView listView;
    ProgressDialog mProgressDialog = null;
    Runnable mViewListActivityRunnable;
    DBHandler dBHandler;
    String query;
    ArrayList<QuestionInfo> questionInfos;
    SearchView searchView;

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
        this.dBHandler = new DBHandler(this, null, null, 1);
        this.adapter = new CustomListViewAdapter(this, R.layout.single_list_element, this.questionInfos);
        this.listView.setAdapter(this.adapter);
        this.searchView = new SearchView(this);
    }

    /**
     * Function to start a new thread in which http request is sent and while the whole processing happens,
     * show progress dialog with loading gif
     */
    private void startGetDataForList() {
        this.mViewListActivityRunnable = new Runnable() {
            public void run() {
                MainActivity.this.getDataForList();
            }
        };
        new Thread(null, this.mViewListActivityRunnable, "Background").start();
        this.mProgressDialog = ProgressDialog.show(this, "Please wait...", "Retrieving data ...", true);
    }

    /**
     * Function to actually initiate the http request by invoking client layer
     * the questionInfos returned by client layer will be checked for null data alongwith network availability
     * to show appropriate feedback to user
     */
    private void getDataForList() {
        try {
            this.questionInfos = dBHandler.sendRequest(this.query, this);
            runOnUiThread(this.returnRes);
        } catch (Exception localException) {
            Log.d(TAG, "Error occured : ", localException);
        }
    }

    /**
     * Client has returned data. It is present in questionInfos list. Now tell adapter to update list views accordingly
     */
    private Runnable returnRes = new Runnable() {
        public void run() {
            MainActivity.this.adapter.clear();

            if ((MainActivity.this.questionInfos != null) && (MainActivity.this.questionInfos.size() > 0)) {
                MainActivity.this.adapter.notifyDataSetChanged();
                for (int i = 0; i < MainActivity.this.questionInfos.size(); i++)
                    MainActivity.this.adapter.add(questionInfos.get(i));
            }
            MainActivity.this.mProgressDialog.dismiss();
            MainActivity.this.adapter.notifyDataSetChanged();
        }
    };

    /**
     * Function responsible for showing search Icon in actionbar. Also, the searchView shown when search icon is tapped
     *
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
            //cleanse user input and strip off any preceding or trailing white spaces
            query = userQuery.replaceAll("\\s+", "");
            searchView.clearFocus();
            startGetDataForList();
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