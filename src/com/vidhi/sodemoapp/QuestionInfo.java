package com.vidhi.sodemoapp;

/**
 * Bean for storing information related to a particular question
 */
public class QuestionInfo {
    String questionText;
    String score;
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