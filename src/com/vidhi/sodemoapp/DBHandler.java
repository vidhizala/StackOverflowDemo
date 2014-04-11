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

import java.util.ArrayList;

public class DBHandler extends SQLiteOpenHelper {

    enum COLUMNS_MASTER{ COLUMN_MASTERID, COLUMN_USERQUERY };
    enum COLUMNS_QUESTIONS{ COLUMN_QID, COLUMN_QUESTIONTITLE, COLUMN_QUESTIONSCORE, COLUMN_MASTER_REF };
    enum COLUMNS_TAGS{ COLUMN_TAG_ID, COLUMN_TAG_TITLE, COLUMN_TAG_QUE_REF };

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


    public DBHandler(Context paramContext, String paramString, SQLiteDatabase.CursorFactory paramCursorFactory, int paramInt) {

        super(paramContext, DATABASE_NAME, paramCursorFactory, DATABASE_VERSION);
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
        Cursor searchMasterCursor = localSQLiteDatabase.query(TABLE_SEARCH_MASTER,new String[] { COLUMN_MASTERID }, COLUMN_USERQUERY + "=\"" + paramString + "\"", null, null,null,null);
        if (searchMasterCursor.moveToFirst()) {
            Log.d(MainActivity.TAG, "Found duplicate. deleting...");
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

        selectTableSearchCursor = localSQLiteDatabase.query(TABLE_SEARCH_MASTER, null, null, null, null, null, null);
        count = selectTableSearchCursor.getCount();
        if (count == 10) {

            selectTableSearchCursor = localSQLiteDatabase.query(TABLE_SEARCH_MASTER, new String[] { "min(" + COLUMN_MASTERID + ")" }, null, null, null, null, null);
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
        SQLiteDatabase db = getReadableDatabase();
        Cursor searchQuestionsTable = db.query(TABLE_SEARCH_MASTER, null, COLUMN_USERQUERY + " LIKE \"%" + userQuery + "%\"", null, null, null, null);

        QuestionInfo localQuestionInfo;
        String[] tempTagHolder;
        Cursor tagsTableCursor;

        if (searchQuestionsTable.moveToFirst()) {
            int i = new Integer(searchQuestionsTable.getInt(COLUMNS_QUESTIONS.COLUMN_QID.ordinal()));
            searchQuestionsTable = db.query(TABLE_QUESTIONS, null, COLUMN_MASTER_REF + "=" + i, null, null, null, null);
            if (searchQuestionsTable.moveToFirst()) {
                do {
                    localQuestionInfo = new QuestionInfo();
                    localQuestionInfo.setQuestion(searchQuestionsTable.getString(COLUMNS_QUESTIONS.COLUMN_QUESTIONTITLE.ordinal()));
                    localQuestionInfo.setScore(searchQuestionsTable.getString(COLUMNS_QUESTIONS.COLUMN_QUESTIONSCORE.ordinal()));
                    tempTagHolder = new String[5];
                    tagsTableCursor = db.query(TABLE_TAGS, new String[] { COLUMN_TAG_TITLE }, COLUMN_TAG_QUE_REF + " = " + searchQuestionsTable.getString(COLUMNS_QUESTIONS.COLUMN_QID.ordinal()), null, null, null, null);
                    if (tagsTableCursor.moveToFirst()) {
                        int count = 0;
                        do {
                            tempTagHolder[count++] = tagsTableCursor.getString(0);

                        } while (tagsTableCursor.moveToNext());
                        tagsTableCursor.close();

                    }
                    localQuestionInfo.setTags(tempTagHolder);
                    questionInfos.add(localQuestionInfo);

                } while (searchQuestionsTable.moveToNext());
                searchQuestionsTable.close();

            }
        }

        db.close();
        return questionInfos;
    }

    @Override
    public void onUpgrade(SQLiteDatabase paramSQLiteDatabase, int paramInt1, int paramInt2) {
        onCreate(paramSQLiteDatabase);
    }
}