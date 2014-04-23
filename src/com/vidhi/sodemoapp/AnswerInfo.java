package com.vidhi.sodemoapp;

/**
 * Created by vidhi on 4/19/14.
 */
public class AnswerInfo {

    String bodyMarkdown;
    int score;
    int upvoteCount;
    int downVoteCount;
    String ownerID;
    String questionRefID;

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getBodyMarkdown() {
        return bodyMarkdown;
    }

    public void setBodyMarkdown(String bodyMarkdown) {
        this.bodyMarkdown = bodyMarkdown;
    }

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

    public String getQuestionRefID() {
        return questionRefID;
    }

    public void setQuestionRefID(String questionRefID) {
        this.questionRefID = questionRefID;
    }



}
