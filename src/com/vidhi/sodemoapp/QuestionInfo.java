package com.vidhi.sodemoapp;

/**
 * Bean for storing information related to a particular question
 */
public class QuestionInfo {
   String questionText;
    String score;
    String bodyMarkdown;
    String page;
    int upvoteCount;
    int downVoteCount;
    int questionID;
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

    public void setQuestion(String question) {
        this.questionText = question;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public void setTags(String[] tagsArray) {
        this.tags = tagsArray;
    }

    public String getPage(){
        return page;
    }

    public void setPage(String page){
        this.page = page;
    }

    public String getBodyMarkdown(){
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

    public int compareTo(QuestionInfo questionInfo) {
        return page.compareTo(questionInfo.page);
    }

    public int getQuestionID() {
        return questionID;
    }

    public void setQuestionID(int questionID) {
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
