package com.vidhi.sodemoapp;

import java.io.Serializable;

/**
 * Bean for storing information related to a particular question
 */
public class QuestionInfo implements Serializable{
    String questionText;
    String score;
    String bodyMarkdown;
    String page;
    int upvoteCount;
    int downVoteCount;
    String questionID;
    String ownerID;
    String[] tags;

    public String getQuestion() {


        return this.questionText;
    }

    public String getScore() {
        return this.score;
    }

    public String[] getTags() {
        return this.tags;
    }

    public void setQuestion(String paramString) {
        this.questionText = paramString;
    }

    public void setScore(String paramString) {
        this.score = paramString;
    }

    public void setTags(String[] paramArrayOfString) {
        this.tags = paramArrayOfString;
    }

    public String getPage(){ return page; }

    public void setPage(String page) { this.page = page; }

    public String getBodyMarkdown(){ return bodyMarkdown; }

    public void setBodyMarkdown(String bodyMarkdown) { this.bodyMarkdown = bodyMarkdown; }

    public int getUpvoteCount() {
        return upvoteCount;
    }

    public void setUpvoteCount(int upvoteCount) {
        this.upvoteCount = upvoteCount;
    }

    public int getDownVoteCount() {
        return downVoteCount;
    }

    public void setDownVoteCount(int downVoteCount) {
        this.downVoteCount = downVoteCount;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }


    public int compareTo(QuestionInfo questionInfo) {
        return page.compareTo(questionInfo.page);
    }

    public String getQuestionID() {
        return questionID;
    }

    public void setQuestionID(String questionID) {
        this.questionID = questionID;
    }

    @Override
    public boolean equals(Object qInfo){

        boolean tagsFlag = true;
        QuestionInfo questionInfo = (QuestionInfo) qInfo;
        String [] tempTagsStringArray1 = questionInfo.getTags();

        String [] tempTagsStringArray2 = this.getTags();

        for(int i=0; i< tempTagsStringArray2.length - 1; i++){

            if(tempTagsStringArray1[i] != null){

                if(!(tempTagsStringArray1[i].equalsIgnoreCase(tempTagsStringArray2[i])))
                    tagsFlag = false;
            }
        }

        boolean status = false;

        if(this.questionText.equals(questionInfo.getQuestion())
                && this.score.equals(questionInfo.getScore())
                && tagsFlag){
            status = true;
        }
        return status;

    }
}