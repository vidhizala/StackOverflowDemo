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
    enum COLUMNS_QUESTIONS{ COLUMN_QID, COLUMN_QUESTIONTITLE, COLUMN_QUESTIONSCORE, COLUMN_QUESTIONPAGE, COLUMN_QUESTION_BODY_MARKDOWN, COLUMN_MASTER_REF, COLUMN_QUE_OWNER_REF };
    enum COLUMNS_TAGS{ COLUMN_TAG_ID, COLUMN_TAG_TITLE, COLUMN_TAG_QUE_REF };
    enum COLUMNS_ANSWER{ COLUMN_ANSWER_ID, COLUMN_BODY_MARKDOWN, COLUMN_DOWNVOTE_COUNT, COLUMN_UPVOTE_COUNT, COLUMN_SCORE, COLUMN_OWNERID, COLUMN_QUESTION_REF_ID };

    private static final String COLUMN_MASTERID = "masterId";
    private static final String COLUMN_MASTER_REF = "masterRef";
    private static final String COLUMN_QID = "questionId";
    private static final String COLUMN_QUESTIONSCORE = "questionScore";
    private static final String COLUMN_QUESTIONPAGE = "questionPage";
    private static final String COLUMN_QUESTIONTITLE = "questionTitle";
    private static final String COLUMN_QUESTIONBODYMARKDOWN = "questionBodyMarkdown";
    private static final String COLUMN_QUESTIONOWNERREF = "questionOwnerRef";


    private static final String COLUMN_TAG_ID = "tagId";
    private static final String COLUMN_TAG_QUE_REF = "tagQueRef";
    private static final String COLUMN_TAG_TITLE = "tagTitle";
    private static final String COLUMN_USERQUERY = "userQuery";

    private static final String COLUMN_ANSWERID = "answerID";
    private static final String COLUMN_ANSWERMARKDOWN = "answerMarkdown";
    private static final String COLUMN_ANSWERSCORE = "answerScore";
    private static final String COLUMN_ANSWERUPVOTECOUNT = "answerUpvoteCount";
    private static final String COLUMN_ANSWERDOWNVOTECOUNT = "answerDownvoteCount";
    private static final String COLUMN_ANSWEROWNERREF = "answerOwnerRef";
    private static final String COLUMN_ANSWERQUEREF ="answerQueRef";

    private static final String COLUMN_OWNERID = "ownerId";
    private static final String COLUMN_OWNERDISPLAYNAME = "ownerDisplayName";
    private static final String COLUMN_OWNERREPUTATION = "ownerReputation";

    private static final String DATABASE_NAME = "SOData.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_QUESTIONS = "Questions";
    private static final String TABLE_SEARCH_MASTER = "Master";
    private static final String TABLE_TAGS = "Tags";
    private static final String TABLE_ANSWERS = "Answers";
    private static final String TABLE_OWNERS = "Owners";


    public DBHandler(Context context, String databaseName,
                     SQLiteDatabase.CursorFactory cursorFactory, int databaseVersion) {
        super(context, "/mnt/sdcard/"+DATABASE_NAME, cursorFactory, DATABASE_VERSION);
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
                               "  INTEGER primary key, " + COLUMN_QUESTIONTITLE + " TEXT, " +
                               COLUMN_QUESTIONSCORE + " INTEGER, " +
                               COLUMN_QUESTIONPAGE + " INTEGER, " +
                               COLUMN_QUESTIONBODYMARKDOWN + " TEXT, " +
                               COLUMN_MASTER_REF + " integer, "+
                               COLUMN_QUESTIONOWNERREF + " integer, " +
                               "FOREIGN KEY(" + COLUMN_QUESTIONOWNERREF + ") REFERENCES " + TABLE_OWNERS + "( " + COLUMN_OWNERID +
                               " ) ON DELETE SET NULL," +
                               "FOREIGN KEY(" + COLUMN_MASTER_REF + ") REFERENCES " + TABLE_SEARCH_MASTER + "( " + COLUMN_MASTERID +
                               " ) ON DELETE CASCADE);");

        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_TAGS + " (" + COLUMN_TAG_ID +
                               " integer primary key autoincrement, " +  COLUMN_TAG_TITLE + " text, " +
                               COLUMN_TAG_QUE_REF + " integer, FOREIGN KEY(" + COLUMN_TAG_QUE_REF +
                               ") REFERENCES " + TABLE_QUESTIONS +"( " + COLUMN_QID + ") ON DELETE CASCADE);");

        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_OWNERS + " (" + COLUMN_OWNERID +
                " integer primary key, " +  COLUMN_OWNERDISPLAYNAME + " text, " +
                COLUMN_OWNERREPUTATION + " integer);");

        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_ANSWERS + " (" + COLUMN_ANSWERID +
                " integer primary key, " +  COLUMN_ANSWERMARKDOWN + " text, " +
                COLUMN_ANSWERDOWNVOTECOUNT + " integer, " + COLUMN_ANSWERUPVOTECOUNT + " integer, "+
                COLUMN_ANSWERSCORE + " integer, " +
                COLUMN_ANSWEROWNERREF + " integer, "+
                COLUMN_ANSWERQUEREF + " integer, " +
                "FOREIGN KEY(" + COLUMN_ANSWEROWNERREF +
                ") REFERENCES " + TABLE_OWNERS +"( " + COLUMN_OWNERID + ") ON DELETE SET NULL, " +
                "FOREIGN KEY(" + COLUMN_ANSWERQUEREF +
                ") REFERENCES "+ TABLE_QUESTIONS +"( "+ COLUMN_QID +") ON DELETE CASCADE);");
    }

    /**
     * Function to add Question in Questions table of db
     *
     * @param questionInfos - Array of Objects of question that is to be inserted in db
     *                     query - user query
     */
    public void addQuestions(ArrayList<QuestionInfo> questionInfos, String query) {

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        long masterTableID = fetchMasterIdFromQuery(query);


        for(int i=0; i< questionInfos.size(); i++){

            QuestionInfo questionInfo = questionInfos.get(i);
            Log.d(MainActivity.TAG, "adding question... Thread ID =" + Thread.currentThread().getId() );
            ContentValues questionTableValues = new ContentValues();
            questionTableValues.put(COLUMN_QID, questionInfo.getQuestionID());
            questionTableValues.put(COLUMN_QUESTIONTITLE, questionInfo.getQuestion());
            questionTableValues.put(COLUMN_QUESTIONSCORE, questionInfo.getScore());
            questionTableValues.put(COLUMN_QUESTIONPAGE, questionInfo.getPage());
            questionTableValues.put(COLUMN_QUESTIONBODYMARKDOWN, questionInfo.getBodyMarkdown());
            questionTableValues.put(COLUMN_MASTER_REF, Long.valueOf(masterTableID));
            questionTableValues.put(COLUMN_QUESTIONOWNERREF, questionInfo.getOwnerID());
            long returnReferenceID = sqLiteDatabase.insert(TABLE_QUESTIONS, null, questionTableValues);
            addTags(questionInfo, returnReferenceID, sqLiteDatabase);

        }

        sqLiteDatabase.close();
    }

    public void addQuestionDetails(ArrayList<QuestionInfo> questionInfos, String questionID) {

        QuestionInfo questioninfo = questionInfos.get(0);
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues questionTableValues = new ContentValues();
        questionTableValues.put(COLUMN_QID, questionID);
        questionTableValues.put(COLUMN_QUESTIONBODYMARKDOWN, questioninfo.getBodyMarkdown());
        sqLiteDatabase.update(TABLE_QUESTIONS, questionTableValues, COLUMN_QID + "=" + questionID, null);
        sqLiteDatabase.close();

    }

    public HashMap fetchQuestionDetails(String questionId){
        HashMap questionDetails = new HashMap();
        QuestionInfo questionInfo = null;
        OwnerInfo ownerInfo = null;
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        int ownerId = -1;

        Cursor searchQuestionTable = sqLiteDatabase.query(TABLE_QUESTIONS,new String[] { COLUMN_QUESTIONBODYMARKDOWN, COLUMN_QUESTIONOWNERREF },
                COLUMN_QID + " = " + questionId, null, null, null, null);
        Log.d(MainActivity.TAG, "size of cursor ="+searchQuestionTable.getCount());
        if (searchQuestionTable.moveToFirst()) {
            questionInfo = new QuestionInfo();
            questionInfo.setBodyMarkdown(searchQuestionTable.getString(0));
            ownerId = searchQuestionTable.getInt(1);
        }
        searchQuestionTable.close();

        if(ownerId != -1){
            Cursor searchOwnerTableCursor = sqLiteDatabase.query(TABLE_OWNERS, null, COLUMN_OWNERID + " = " + ownerId, null, null, null, null );
            if(searchOwnerTableCursor.moveToFirst()){
                ownerInfo = new OwnerInfo();
                ownerInfo.setOwnerID(searchOwnerTableCursor.getString(0));
                ownerInfo.setOwnerDisplayName(searchOwnerTableCursor.getString(1));
                ownerInfo.setOwnerReputation(searchOwnerTableCursor.getInt(2));
            }
        }

        questionDetails.put("questionsData", questionInfo);
        questionDetails.put("ownerData", ownerInfo);

        sqLiteDatabase.close();
        return questionDetails;
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
     * @param questionTableID         - Reference ID of the Question stored in Question Table of db. Used to store
     *                                  tags of this question
     */
    public void addTags(QuestionInfo questionInfo, long questionTableID, SQLiteDatabase sqLiteDatabase) {
        String[] arrayOfString = questionInfo.getTags();

        for (int i = 0; i < arrayOfString.length; i++)
            if (arrayOfString[i] != null) {
                ContentValues tagsTableValues = new ContentValues();
                tagsTableValues.put(COLUMN_TAG_TITLE, arrayOfString[i]);
                tagsTableValues.put(COLUMN_TAG_QUE_REF, Long.valueOf(questionTableID));
                sqLiteDatabase.insert(TABLE_TAGS, null, tagsTableValues);
            }

    }

    public void addOwners(ArrayList<OwnerInfo> ownerInfos){

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        for(int i=0 ;i< ownerInfos.size(); i++){

            OwnerInfo ownerInfo = ownerInfos.get(i);

            boolean exists;
            ContentValues ownerTableValues = new ContentValues();
            ownerTableValues.put(COLUMN_OWNERREPUTATION, ownerInfo.getOwnerReputation());
            ownerTableValues.put(COLUMN_OWNERDISPLAYNAME, ownerInfo.getOwnerDisplayName());

            exists = checkIfOwnerExists(ownerInfo, sqLiteDatabase);


            if(!exists){
                ownerTableValues.put(COLUMN_OWNERID, ownerInfo.getOwnerID());
                sqLiteDatabase.insert(TABLE_OWNERS, null, ownerTableValues);

            }else{
                sqLiteDatabase.update(TABLE_OWNERS, ownerTableValues, COLUMN_OWNERID + "=" + ownerInfo.getOwnerID(), null);
            }

        }
        sqLiteDatabase.close();
    }

    private boolean checkIfOwnerExists(OwnerInfo ownerinfo, SQLiteDatabase sqLiteDatabase) {

        Cursor searchOwnerInfo = sqLiteDatabase.query(TABLE_OWNERS,new String[] { COLUMN_OWNERID },
                COLUMN_OWNERID + "=" + ownerinfo.getOwnerID(), null, null,null,null);
        if (searchOwnerInfo.getCount() != 0) {
            searchOwnerInfo.close();
            return true;
        }
        searchOwnerInfo.close();
        return false;
    }

    public void addAnswers(ArrayList<AnswerInfo> answerInfos){

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        for(int i=0 ;i<answerInfos.size(); i++){
            AnswerInfo answerInfo = answerInfos.get(i);

            ContentValues answerTableContentValues = new ContentValues();
            answerTableContentValues.put(COLUMN_ANSWERSCORE, answerInfo.getScore());
            answerTableContentValues.put(COLUMN_ANSWERMARKDOWN, answerInfo.getBodyMarkdown());
            answerTableContentValues.put(COLUMN_ANSWERDOWNVOTECOUNT, answerInfo.getDownVoteCount());
            answerTableContentValues.put(COLUMN_ANSWERUPVOTECOUNT, answerInfo.getUpvoteCount());
            answerTableContentValues.put(COLUMN_ANSWEROWNERREF, answerInfo.getOwnerID());
            answerTableContentValues.put(COLUMN_ANSWERQUEREF, answerInfo.getQuestionRefID());

            sqLiteDatabase.insert(TABLE_ANSWERS, null, answerTableContentValues);
        }

        sqLiteDatabase.close();

    }
    /**
     * Function to add the searchQuery entered by user to Master table in cache database
     * This table stores the string entered by user and serves as a dictionary for finding relevant data
     * for offline support
     *
     * @param userQuery
     * @return ID of the record entered in Master table. This ID will be useful as reference to add records in Questions
     * and Tags table
     */
    public void addToMaster(String userQuery) {

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
    public HashMap fetchQuestionsFromDatabase(String userQuery, int startFrom, int pageSize) {

        HashMap dataToreturn = new HashMap();
        boolean has_more;
        ArrayList questionInfos = new ArrayList<QuestionInfo>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor searchTable = db.query(TABLE_SEARCH_MASTER, null, COLUMN_USERQUERY + " LIKE \"%" + userQuery +
                                      "%\"", null, null, null, null);

        QuestionInfo questionInfo;
        String[] tempTagHolder;
        Cursor tagsTableCursor;

        if (searchTable.moveToFirst()) {
            int i = new Integer(searchTable.getInt(COLUMNS_MASTER.COLUMN_MASTERID.ordinal()));
            searchTable = db.query(TABLE_QUESTIONS, null, COLUMN_MASTER_REF + "=" + i, null, null, null,
                                   null, "" + startFrom + "," + pageSize);
            if (searchTable.moveToFirst()) {
                do {
                    questionInfo = new QuestionInfo();
                    questionInfo.setQuestion(searchTable
                                .getString(COLUMNS_QUESTIONS.COLUMN_QUESTIONTITLE.ordinal()));
                    questionInfo.setScore(searchTable
                            .getString(COLUMNS_QUESTIONS.COLUMN_QUESTIONSCORE.ordinal()));
                    questionInfo.setBodyMarkdown(searchTable.getString(COLUMNS_QUESTIONS.COLUMN_QUESTION_BODY_MARKDOWN.ordinal()));
                    questionInfo.setOwnerID(searchTable.getString(COLUMNS_QUESTIONS.COLUMN_QUE_OWNER_REF.ordinal()));
                    questionInfo.setPage(searchTable.getString(COLUMNS_QUESTIONS.COLUMN_QUESTIONPAGE.ordinal()));
                    questionInfo.setQuestionID(searchTable.getString(COLUMNS_QUESTIONS.COLUMN_QID.ordinal()));
                    tempTagHolder = new String[5];
                    tagsTableCursor = db.query(TABLE_TAGS, new String[] { COLUMN_TAG_TITLE }, COLUMN_TAG_QUE_REF +
                                      " = " + searchTable.getString(COLUMNS_QUESTIONS.COLUMN_QID.ordinal()),
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

                } while (searchTable.moveToNext());
                searchTable.close();

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


    public ArrayList fetchAnswersFromDatabase(String questionID) {
        ArrayList<AnswerInfo> answerInfos = new ArrayList<AnswerInfo>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor searchAnswerTable = db.query(TABLE_ANSWERS, null,  COLUMN_ANSWERQUEREF + "=" + questionID, null, null, null, null);

        if (searchAnswerTable.moveToFirst()) {
            do{
                AnswerInfo answerInfo = new AnswerInfo();
                answerInfo.setScore(searchAnswerTable.getInt(COLUMNS_ANSWER.COLUMN_SCORE.ordinal()));
                answerInfo.setDownVoteCount(searchAnswerTable.getInt(COLUMNS_ANSWER.COLUMN_DOWNVOTE_COUNT.ordinal()));
                answerInfo.setUpvoteCount(searchAnswerTable.getInt(COLUMNS_ANSWER.COLUMN_UPVOTE_COUNT.ordinal()));
                answerInfo.setQuestionRefID(searchAnswerTable.getString(COLUMNS_ANSWER.COLUMN_QUESTION_REF_ID.ordinal()));
                answerInfo.setBodyMarkdown(searchAnswerTable.getString(COLUMNS_ANSWER.COLUMN_BODY_MARKDOWN.ordinal()));
                answerInfo.setOwnerID(searchAnswerTable.getString(COLUMNS_ANSWER.COLUMN_OWNERID.ordinal()));

                answerInfos.add(answerInfo);
            }while(searchAnswerTable.moveToNext());
        }
        searchAnswerTable.close();
        db.close();

        return answerInfos;
    }


    public int getTotalRows(String tableName){
        int totalRows;
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        totalRows = (int) DatabaseUtils.queryNumEntries(sqLiteDatabase, tableName);
        sqLiteDatabase.close();
        return totalRows;
    }

    public void removeStalePageifExists(int page, String query){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        long masterID = fetchMasterIdFromQuery(query);
        sqLiteDatabase.delete(TABLE_QUESTIONS, COLUMN_MASTER_REF + "=" + masterID + " AND " + COLUMN_QUESTIONPAGE + "=" + page, null);
        sqLiteDatabase.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }


}