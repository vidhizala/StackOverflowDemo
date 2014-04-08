package com.vidhi.sodemoapp;

/**
 * Created by vidhi on 3/24/14.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DBHandler extends SQLiteOpenHelper {
    private static final String COLUMN_MASTERID = "masterid";
    private static final String COLUMN_MASTER_REF = "masterref";
    private static final String COLUMN_QID = "questionid";
    private static final String COLUMN_QUESTIONSCORE = "questionscore";
    private static final String COLUMN_QUESTIONTITLE = "questiontitle";
    private static final String COLUMN_TAG_ID = "tagid";
    private static final String COLUMN_TAG_QUE_REF = "tagqueref";
    private static final String COLUMN_TAG_TITLE = "tagtitle";
    private static final String COLUMN_USERQUERY = "userquery";
    private static final String DATABASE_NAME = "SOData.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_QUESTIONS = "questions";
    private static final String TABLE_SEARCH_MASTER = "master";
    private static final String TABLE_TAGS = "tags";

    private HttpClient httpClient;
    private ArrayList<QuestionInfo> questionInfos;
    private String query;
    private String messageAbsurdQuery = "No results found matching your query. Please check spelling and try again.";
    private String messageNoNetwork = "No Response from Server. Please check your connectivity and try again.";

    public DBHandler(Context paramContext, String paramString, SQLiteDatabase.CursorFactory paramCursorFactory, int paramInt) {

        super(paramContext, DATABASE_NAME, paramCursorFactory, paramInt);
        httpClient = new HttpClient();
        questionInfos = new ArrayList<QuestionInfo>();
    }

    /**
     * Function (override) that enables support of foreign keys
     *
     * @param db
     */
    @Override
    public void onOpen(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys = ON;");
    }

    public ArrayList<QuestionInfo> getQuestionInfos() {
        return questionInfos;
    }

    public void setQuestionInfos(ArrayList<QuestionInfo> questionInfos) {
        this.questionInfos = questionInfos;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
    @Override
    public void onCreate(SQLiteDatabase paramSQLiteDatabase) {
        paramSQLiteDatabase.execSQL("CREATE TABLE " + TABLE_SEARCH_MASTER + "( " + COLUMN_MASTERID + " INTEGER primary key autoincrement, " + COLUMN_USERQUERY + " TEXT )");
        paramSQLiteDatabase.execSQL("CREATE TABLE " + TABLE_QUESTIONS + " ( " + COLUMN_QID + "  INTEGER primary key autoincrement, " + COLUMN_QUESTIONTITLE + " TEXT, " + COLUMN_QUESTIONSCORE + " INTEGER, " + COLUMN_MASTER_REF + " integer,FOREIGN KEY(" + COLUMN_MASTER_REF + ") REFERENCES " + TABLE_SEARCH_MASTER + "( " + COLUMN_MASTERID + " ) ON DELETE CASCADE);");
        paramSQLiteDatabase.execSQL("CREATE TABLE " + TABLE_TAGS + " (" + COLUMN_TAG_ID + " integer primary key autoincrement, " + COLUMN_TAG_TITLE + " text, " + COLUMN_TAG_QUE_REF + " integer, FOREIGN KEY(" + COLUMN_TAG_QUE_REF + ") REFERENCES questions( " + COLUMN_QID + ") ON DELETE CASCADE);");
    }

    /**
     * Function to add Question in Questions table of db
     *
     * @param paramQuestionInfo - Object of question that is to be inserted in db
     * @param paramLong         - Reference ID of the userquery that has been inserted in Master table of database. Used to reference it in offline support
     */
    public void addQuestion(QuestionInfo paramQuestionInfo, long paramLong) {

        ContentValues localContentValues = new ContentValues();
        localContentValues.put(COLUMN_QUESTIONTITLE, paramQuestionInfo.getQuestion());
        localContentValues.put(COLUMN_QUESTIONSCORE, paramQuestionInfo.getScore());
        localContentValues.put(COLUMN_MASTER_REF, Long.valueOf(paramLong));
        SQLiteDatabase localSQLiteDatabase = getWritableDatabase();
        long returnReferenceID = localSQLiteDatabase.insert(TABLE_QUESTIONS, null, localContentValues);
        localSQLiteDatabase.close();
        addTags(paramQuestionInfo, returnReferenceID);
    }

    /**
     * Function to add tags of a particular question in Tags table of db
     *
     * @param questionInfo - Object containing the information of question currently whose tags are to be stored
     * @param paramLong         - Reference ID of the Question stored in Question Table of db. Used to store tags of this question
     */
    public void addTags(QuestionInfo questionInfo, long paramLong) {
        String[] arrayOfString = questionInfo.getTags();
        for (int i = 0; i < arrayOfString.length; i++)
            if (arrayOfString[i] != null) {
                ContentValues localContentValues = new ContentValues();
                SQLiteDatabase localSQLiteDatabase = getWritableDatabase();
                localContentValues.put(COLUMN_TAG_TITLE, arrayOfString[i]);
                localContentValues.put(COLUMN_TAG_QUE_REF, Long.valueOf(paramLong));
                localSQLiteDatabase.insert(TABLE_TAGS, null, localContentValues);
                localSQLiteDatabase.close();
            }
    }

    /**
     * Function to add the searchQuery entered by user to Master table in cache database
     * This table stores the string entered by user and serves as a dictionary for finding relevant data for offline support
     *
     * @param paramString
     * @return ID of the record entered in Master table. This ID will be useful as reference to add records in Questions and Tags table
     */
    public long addToMaster(String paramString) {
        Log.d(MainActivity.TAG, "inside add to master");
        long masterRefID = -1;
        if (!checkAndDeleteDuplicateEntry(paramString)) {
            // in case there is a duplicate that needs to be removed and there are 10 records, remove the duplicate one
            //thus making space for the new entry. SO, no need to check and delete older data
            checkAndDeleteOlderData();
        }
        SQLiteDatabase localSQLiteDatabase = getWritableDatabase();
        ContentValues localContentValues = new ContentValues();
        localContentValues.put(COLUMN_USERQUERY, paramString);
        masterRefID = localSQLiteDatabase.insert(TABLE_SEARCH_MASTER, null, localContentValues);
        localSQLiteDatabase.close();
        return masterRefID;
    }

    /**
     * Function to check for duplicate entry in cache database. In case it is present, delete it.
     * We don't want to update the same as the ID should also be changed to depict it's the latest record.
     *
     * @param paramString - user Query entered
     * @return boolean variable depending on whether duplicate record is present in the cache database or not
     */
    public boolean checkAndDeleteDuplicateEntry(String paramString) {

        Log.d(MainActivity.TAG, "inside check and delete duplicate entry");
        SQLiteDatabase localSQLiteDatabase = getWritableDatabase();
        Cursor searchMasterCursor = localSQLiteDatabase.rawQuery("Select * from " + TABLE_SEARCH_MASTER + " where " + COLUMN_USERQUERY + " = \"" + paramString + "\"", null);
        if (searchMasterCursor.moveToFirst()) {
            Log.d(MainActivity.TAG, "Found duplicate. deleting...");
            searchMasterCursor.moveToFirst();
            localSQLiteDatabase.delete(TABLE_SEARCH_MASTER, COLUMN_MASTERID + " = " + searchMasterCursor.getInt(0), null);
            searchMasterCursor.close();
            localSQLiteDatabase.close();
            return true;
        } else return false;

    }

    /**
     * Function to check if there are more than 10 records in cache database.
     * If > 10, delete the oldest one to make space for the newly obtained record.
     */
    public void checkAndDeleteOlderData() {

        Log.d(MainActivity.TAG, "inside check and delete older data");
        SQLiteDatabase localSQLiteDatabase = getWritableDatabase();
        long minID;
        Cursor selectTableSearchCursor;
        int count;

        selectTableSearchCursor = localSQLiteDatabase.rawQuery("Select count(*) FROM " + TABLE_SEARCH_MASTER, null);
        selectTableSearchCursor.moveToFirst();
        count = selectTableSearchCursor.getInt(0);
        if (count == 10) {

            selectTableSearchCursor = localSQLiteDatabase.rawQuery("Select min(" + COLUMN_MASTERID + ") FROM " + TABLE_SEARCH_MASTER, null);
            selectTableSearchCursor.moveToFirst();

            minID = selectTableSearchCursor.getInt(0);

            localSQLiteDatabase.delete(TABLE_SEARCH_MASTER, COLUMN_MASTERID + "=" + minID, null);
        }
        selectTableSearchCursor.close();

        localSQLiteDatabase.close();
    }

    /**
     * Function to provide offline data support. Called in case of no connectivity.
     *
     * @param userQuery - String entered by user to send request
     * @return - list of QuestionInfo objects containing Questions and related data matching userQuery
     */
    public ArrayList<QuestionInfo> fetchFromDatabase(String userQuery) {

        ArrayList questionInfos = new ArrayList<QuestionInfo>();

        String selectMasterTable = "Select * FROM " + TABLE_SEARCH_MASTER + " WHERE " + COLUMN_USERQUERY + " =\"" + userQuery + "\"";
        SQLiteDatabase db = getReadableDatabase();

        Cursor searchMasterTable = db.rawQuery(selectMasterTable, null);

        QuestionInfo localQuestionInfo;
        String[] arrayOfString;
        Cursor tagsTableCursor;

        if (searchMasterTable.moveToFirst()) {
            searchMasterTable.moveToFirst();
            int i = new Integer(searchMasterTable.getInt(0));
            searchMasterTable = db.rawQuery("SELECT * FROM " + TABLE_QUESTIONS + " WHERE " + COLUMN_MASTER_REF + "=" + i, null);
            if (searchMasterTable.moveToFirst()) {
                searchMasterTable.moveToFirst();
                do {
                    localQuestionInfo = new QuestionInfo();
                    localQuestionInfo.setQuestion(searchMasterTable.getString(1));
                    localQuestionInfo.setScore(searchMasterTable.getString(2));
                    String searchTagsTable = "Select * FROM " + TABLE_TAGS + " WHERE " + COLUMN_TAG_QUE_REF + " = " + searchMasterTable.getString(0);
                    arrayOfString = new String[5];
                    tagsTableCursor = db.rawQuery(searchTagsTable, null);
                    if (tagsTableCursor.moveToFirst()) {
                        int count = 0;
                        tagsTableCursor.moveToFirst();
                        do {
                            arrayOfString[count++] = tagsTableCursor.getString(1);

                        } while (tagsTableCursor.moveToNext());
                        tagsTableCursor.close();

                    }
                    localQuestionInfo.setTags(arrayOfString);
                    questionInfos.add(localQuestionInfo);

                } while (searchMasterTable.moveToNext());
                searchMasterTable.close();

            }
        }

        db.close();
        return questionInfos;
    }

    public ArrayList sendRequest(String query, MainActivity context){

        questionInfos = new ArrayList<QuestionInfo>();

        final MainActivity mainActivity = context;

        setQuery(query);
        httpClient.setQuery(query);

        String response = httpClient.sendPost();

        if(httpClient.getResponseCode() == 200){

            if (convertResponse(response)) {

                storeData(addToMaster(query));

            }else{

                mainActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast localToast = Toast.makeText(mainActivity.getApplicationContext(), messageAbsurdQuery, 0);
                        localToast.setGravity(18,0,0);
                        localToast.show();
                    }
                });

            }
        }else{
            Log.d(MainActivity.TAG, "Call database");
            questionInfos = fetchFromDatabase(getQuery());
            if (this.questionInfos.size() == 0)  //even local db doesn't have anything
            {
                Log.d(MainActivity.TAG, "again null");
                mainActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast localToast = Toast.makeText(mainActivity.getApplicationContext(), messageNoNetwork, 0);
                        localToast.setGravity(18,0,0);
                        localToast.show();
                    }
                });
            }
        }
        return questionInfos;
    }

    /**
     * Function to convert the response string obtained from HttpRequest sent
     * Conversion results in  list of QuestionInfo objects which can be passed to dbHandler fro storing in database
     *
     * @param returnData
     * @return
     */
    public boolean convertResponse(String returnData) {
        if(questionInfos == null){
            questionInfos = new ArrayList<QuestionInfo>();
        }
        try {

            JSONObject jObject = new JSONObject(returnData);
            JSONArray jsonArray = jObject.getJSONArray("questions");

            if (jsonArray.length() == 0) { //empty questions array in case of absurd search query.
                return false;
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

        } catch (JSONException e) {
            Log.d(MainActivity.TAG, "Exception occured", e);

           return false;
        }

        return true;

    }


    /**
     * Function to call dbHandler's method to store questions and their related tags in respective tables
     *
     * @param paramLong - Reference ID of the record stored in MasterTable. Used for saving questions related to this query
     *                  <p/>
     *
     */
    public void storeData(long paramLong) {

        for (int i = 0; i < questionInfos.size(); i++)
            addQuestion((QuestionInfo) questionInfos.get(i), paramLong);



    }

    @Override
    public void onUpgrade(SQLiteDatabase paramSQLiteDatabase, int paramInt1, int paramInt2) {
        onCreate(paramSQLiteDatabase);
    }
}