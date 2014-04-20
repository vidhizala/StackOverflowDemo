package com.vidhi.sodemoapp;

/**
 * Created by vidhi on 3/24/14.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class DBHandler extends SQLiteOpenHelper {

    enum COLUMNS_MASTER{ COLUMN_MASTERID, COLUMN_USERQUERY };
    enum COLUMNS_QUESTIONS{ COLUMN_QID, COLUMN_QUESTIONTITLE, COLUMN_QUESTIONSCORE, COLUMN_QUESTIONPAGE, COLUMN_MASTER_REF };
    enum COLUMNS_TAGS{ COLUMN_TAG_ID, COLUMN_TAG_TITLE, COLUMN_TAG_QUE_REF };

    private static final String COLUMN_MASTERID = "masterid";
    private static final String COLUMN_MASTER_REF = "masterref";
    private static final String COLUMN_QID = "questionid";
    private static final String COLUMN_QUESTIONSCORE = "questionscore";
    private static final String COLUMN_QUESTIONPAGE = "questionPage";
    private static final String COLUMN_QUESTIONTITLE = "questionTitle";
    private static final String COLUMN_QUESTIONBODYMARKDOWN = "questionBodyMarkdown";
    private static final String COLUMN_TAG_ID = "tagid";
    private static final String COLUMN_TAG_QUE_REF = "tagqueref";
    private static final String COLUMN_TAG_TITLE = "tagtitle";
    private static final String COLUMN_USERQUERY = "userquery";
    private static final String DATABASE_NAME = "SOData.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_QUESTIONS = "questions";
    private static final String TABLE_SEARCH_MASTER = "master";
    private static final String TABLE_TAGS = "tags";


    public DBHandler(Context context, String databaseName,
                     SQLiteDatabase.CursorFactory cursorFactory, int databaseVersion) {
        super(context, DATABASE_NAME, cursorFactory, DATABASE_VERSION);
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

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_SEARCH_MASTER + "( " + COLUMN_MASTERID +
                               " INTEGER primary key autoincrement, " + COLUMN_USERQUERY + " TEXT )");

        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_QUESTIONS + " ( " + COLUMN_QID +
                               "  INTEGER primary key autoincrement, " + COLUMN_QUESTIONTITLE + " TEXT, " +
                               COLUMN_QUESTIONSCORE + " INTEGER, " +
                               COLUMN_QUESTIONPAGE + " INTEGER, " +
                               COLUMN_QUESTIONBODYMARKDOWN + " TEXT, " +
                               COLUMN_MASTER_REF + " integer,FOREIGN KEY(" +
                               COLUMN_MASTER_REF + ") REFERENCES " + TABLE_SEARCH_MASTER + "( " + COLUMN_MASTERID +
                               " ) ON DELETE CASCADE);");

        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_TAGS + " (" + COLUMN_TAG_ID +
                               " integer primary key autoincrement, " +  COLUMN_TAG_TITLE + " text, " +
                               COLUMN_TAG_QUE_REF + " integer, FOREIGN KEY(" + COLUMN_TAG_QUE_REF +
                               ") REFERENCES " + TABLE_QUESTIONS +"( " + COLUMN_QID + ") ON DELETE CASCADE);");
    }

    /**
     * Function to add Question in Questions table of db
     *
     * @param questionInfo - Object of question that is to be inserted in db
     * @param query         - userquery that has been inserted in Master table of database. Used to reference it in offline support
     */
    public void addQuestion(QuestionInfo questionInfo, String query) {

        ContentValues questionTableValues = new ContentValues();
        questionTableValues.put(COLUMN_QUESTIONTITLE, questionInfo.getQuestion());
        questionTableValues.put(COLUMN_QUESTIONSCORE, questionInfo.getScore());
        questionTableValues.put(COLUMN_QUESTIONPAGE, questionInfo.getPage());
        long masterTableID = fetchMasterIdFromQuery(query);
        questionTableValues.put(COLUMN_MASTER_REF, Long.valueOf(masterTableID));
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        long returnReferenceID = sqLiteDatabase.insert(TABLE_QUESTIONS, null, questionTableValues);
        sqLiteDatabase.close();
        addTags(questionInfo, returnReferenceID);
    }

   public long fetchMasterIdFromQuery(String query){

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        Cursor getMasterIDCursor;

        getMasterIDCursor = sqLiteDatabase.query(TABLE_SEARCH_MASTER, new String[]{COLUMN_MASTERID}, COLUMN_USERQUERY + "=\"" + query+"\"", null, null, null, null);
        getMasterIDCursor.moveToFirst();
        return (long) getMasterIDCursor.getInt(0);
    }

    /**
     * Function to add tags of a particular question in Tags table of db
     *
     * @param questionInfo - Object containing the information of question currently whose tags are to be stored
     * @param questionTableID         - Reference ID of the Question stored in Question Table of db. Used to store tags of this question
     */
      public void addTags(QuestionInfo questionInfo, long questionTableID) {
        String[] arrayOfString = questionInfo.getTags();
        for (int i = 0; i < arrayOfString.length; i++)
            if (arrayOfString[i] != null) {
                ContentValues tagsTableValues = new ContentValues();
                SQLiteDatabase sqLiteDatabase = getWritableDatabase();
                tagsTableValues.put(COLUMN_TAG_TITLE, arrayOfString[i]);
                tagsTableValues.put(COLUMN_TAG_QUE_REF, Long.valueOf(questionTableID));
                sqLiteDatabase.insert(TABLE_TAGS, null, tagsTableValues);
                sqLiteDatabase.close();
            }
    }

    /**
     * Function to add the searchQuery entered by user to Master table in cache database
     * This table stores the string entered by user and serves as a dictionary for finding relevant data for offline support
     *
     * @param userQuery
     */
    public void addToMaster(String userQuery) {
        Log.d(MainActivity.TAG, "inside add to master");
        if (!checkAndDeleteDuplicateEntry(userQuery)) {
            // in case there is a duplicate that needs to be removed and there are 10 records, remove the duplicate one
            //thus making space for the new entry. SO, no need to check and delete older data
            checkAndDeleteOlderData();
        }
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues contentValuesMaster = new ContentValues();
        contentValuesMaster.put(COLUMN_USERQUERY, userQuery);
        sqLiteDatabase.insert(TABLE_SEARCH_MASTER, null, contentValuesMaster);
        sqLiteDatabase.close();
    }

    /**
     * Function to check for duplicate entry in cache database. In case it is present, delete it.
     * We don't want to update the same as the ID should also be changed to depict it's the latest record.
     *
     * @param userQuery - user Query entered
     * @return boolean variable depending on whether duplicate record is present in the cache database or not
     */
    public boolean checkAndDeleteDuplicateEntry(String userQuery) {

        Log.d(MainActivity.TAG, "inside check and delete duplicate entry");
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        Cursor searchMasterCursor = sqLiteDatabase.query(TABLE_SEARCH_MASTER,new String[] { COLUMN_MASTERID },
                                    COLUMN_USERQUERY + "=\"" + userQuery + "\"", null, null,null,null);
        if (searchMasterCursor.moveToFirst()) {
            Log.d(MainActivity.TAG, "Found duplicate. deleting...");
            sqLiteDatabase.delete(TABLE_SEARCH_MASTER, COLUMN_MASTERID + " = " + searchMasterCursor.getInt(0), null);
            searchMasterCursor.close();
            sqLiteDatabase.close();
            return true;
        } else return false;

    }

    /**
     * Function to check if there are more than 10 records in cache database.
     * If > 10, delete the oldest one to make space for the newly obtained record.
     */
    public void checkAndDeleteOlderData() {

        Log.d(MainActivity.TAG, "inside check and delete older data");
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        long minID;
        Cursor selectTableSearchCursor;
        int count;

        selectTableSearchCursor = sqLiteDatabase.query(TABLE_SEARCH_MASTER, null, null, null, null, null, null);
        count = selectTableSearchCursor.getCount();
        if (count == 10) {

            selectTableSearchCursor = sqLiteDatabase.query(TABLE_SEARCH_MASTER, new String[] { "min(" +
                                      COLUMN_MASTERID + ")" }, null, null, null, null, null);
            selectTableSearchCursor.moveToFirst();
            minID = selectTableSearchCursor.getInt(0);

            sqLiteDatabase.delete(TABLE_SEARCH_MASTER, COLUMN_MASTERID + "=" + minID, null);
        }
        selectTableSearchCursor.close();

        sqLiteDatabase.close();
    }

    /**
     * Function to provide offline data support. Called in case of no connectivity.
     *
     * @param userQuery - String entered by user to send request
     * @return - list of QuestionInfo objects containing Questions and related data matching userQuery
     */
  public HashMap fetchFromDatabase(String userQuery, int startFrom, int pageSize) {

        HashMap dataToreturn = new HashMap();
        boolean has_more = false;
        ArrayList questionInfos = new ArrayList<QuestionInfo>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor searchQuestionsTable = db.query(TABLE_SEARCH_MASTER, null, COLUMN_USERQUERY + " LIKE \"%" + userQuery +
                                      "%\"", null, null, null, null);

        QuestionInfo questionInfo;
        String[] tempTagHolder;
        Cursor tagsTableCursor;

        if (searchQuestionsTable.moveToFirst()) {
            int i = new Integer(searchQuestionsTable.getInt(COLUMNS_QUESTIONS.COLUMN_QID.ordinal()));
            searchQuestionsTable = db.query(TABLE_QUESTIONS, null, COLUMN_MASTER_REF + "=" + i, null, null, null,
                                   null, "" + startFrom + "," + pageSize);
            if (searchQuestionsTable.moveToFirst()) {
                do {
                    questionInfo = new QuestionInfo();
                    questionInfo.setQuestion(searchQuestionsTable
                                .getString(COLUMNS_QUESTIONS.COLUMN_QUESTIONTITLE.ordinal()));
                    questionInfo.setScore(searchQuestionsTable
                                .getString(COLUMNS_QUESTIONS.COLUMN_QUESTIONSCORE.ordinal()));
                    questionInfo.setPage(searchQuestionsTable.getString(COLUMNS_QUESTIONS.COLUMN_QUESTIONPAGE.ordinal()));
                    tempTagHolder = new String[5];
                    tagsTableCursor = db.query(TABLE_TAGS, new String[] { COLUMN_TAG_TITLE }, COLUMN_TAG_QUE_REF +
                                      " = " + searchQuestionsTable.getString(COLUMNS_QUESTIONS.COLUMN_QID.ordinal()),
                                      null, null, null, null);
                    if (tagsTableCursor.moveToFirst()) {
                        int count = 0;
                        do {
                            tempTagHolder[count++] = tagsTableCursor.getString(0);

                        } while (tagsTableCursor.moveToNext());
                        tagsTableCursor.close();

                    }
                    questionInfo.setTags(tempTagHolder);
                    questionInfos.add(questionInfo);

                } while (searchQuestionsTable.moveToNext());
                searchQuestionsTable.close();

            }
        }
        if((startFrom + pageSize) < getTotalRows(TABLE_QUESTIONS)){
            has_more =true;
        }else{
            has_more = false;
        }

        db.close();

        dataToreturn.put("data", questionInfos);
        dataToreturn.put("has_more", has_more);
        return dataToreturn;
    }

  public int getTotalRows(String tableName){
        int totalRows;
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        totalRows = (int) DatabaseUtils.queryNumEntries(sqLiteDatabase, tableName);
        Log.d(MainActivity.TAG,"total rows in "+tableName+ " is "+totalRows);
        sqLiteDatabase.close();
        return totalRows;
    }

    public void removeStalePageifExists(int page, String query){
        Log.d(MainActivity.TAG, "inside remove stale page" + page);
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        long masterID = fetchMasterIdFromQuery(query);
        sqLiteDatabase.delete(TABLE_QUESTIONS, COLUMN_MASTER_REF + "=" + masterID + " AND " + COLUMN_QUESTIONPAGE + "=" + page, null);
        sqLiteDatabase.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase paramSQLiteDatabase, int paramInt1, int paramInt2) {
        onCreate(paramSQLiteDatabase);
    }
}
