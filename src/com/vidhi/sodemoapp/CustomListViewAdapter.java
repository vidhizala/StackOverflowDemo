package com.vidhi.sodemoapp;
/**
 * Created by vidhi on 3/24/14.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CustomListViewAdapter extends ArrayAdapter<QuestionInfo> {
    Context context;
    ArrayList questionInfos;

    public CustomListViewAdapter(Context context, int position, ArrayList<QuestionInfo> questionInfos) {
        super(context, position, questionInfos);

        this.questionInfos = questionInfos;

        this.context = context;

    }

 @Override
    public void notifyDataSetChanged() {
        Collections.sort(questionInfos, questionInfoComparator);
        super.notifyDataSetChanged();
    }

    public Comparator questionInfoComparator = new Comparator<QuestionInfo>() {
        @Override
        public int compare(QuestionInfo questionInfo, QuestionInfo questionInfo2) {
            return questionInfo.compareTo(questionInfo2);
        }
    };
    public void removeIfExists(int page){
        ArrayList tempQuestionInfos = new ArrayList();
        QuestionInfo questionInfo;
        int pageToCompare;
        int sizeQuestionInfos = questionInfos.size();
        for(int i=0 ;i < sizeQuestionInfos; i++){
            questionInfo = (QuestionInfo) questionInfos.get(i);
            pageToCompare = Integer.parseInt(questionInfo.getPage());
            if((page != pageToCompare)){
                tempQuestionInfos.add(questionInfos.get(i));
            }
        }
        questionInfos.clear();
        questionInfos.addAll(tempQuestionInfos);
    }

    /**
     * Function used by ArrayAdapter to iterate through the list of questionInfo objects and render each in the single_list_element layout
     * @param position       - used to access the current element from the list of questionInfo objects
     * @param convertView      - single_list_element layout
     * @param parent
     * @return listview with rendered data of questionInfo objects
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        QuestionInfo qInfo = getItem(position);
        ViewHolder holder;
        // first check to see if the view is null. if so, we have to inflate it.
        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.single_list_element, null);
            holder.setViews(convertView);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tQuestionTitle.setText(qInfo.getQuestion());
        holder.tScore.setText(qInfo.getScore());
        String[] tempQuestionTags = qInfo.getTags();
        holder.setQuestionTags(tempQuestionTags);

        // the view must be returned to our activity
        return convertView;
    }

    private static class ViewHolder{
        public TextView tQuestionTitle;
        public TextView tTag1;
        public TextView tTag2;
        public TextView tTag3;
        public TextView tTag4;
        public TextView tTag5;
        public TextView tScore;

        public void setQuestionTags(String tempTags[]){
            if(tempTags[0] != null)
                tTag1.setText(tempTags[0]);
            if(tempTags[1] != null)
                tTag2.setText(tempTags[1]);
            if(tempTags[2] != null)
                tTag3.setText(tempTags[2]);
            if(tempTags[3] != null)
                tTag4.setText(tempTags[3]);
            if(tempTags[4] != null)
                tTag5.setText(tempTags[4]);

        }

        public void setViews(View convertView){
            tQuestionTitle = (TextView) convertView.findViewById(R.id.questiontitle);
            tTag1 = (TextView) convertView.findViewById(R.id.tag1);
            tTag2 = (TextView) convertView.findViewById(R.id.tag2);
            tTag3 = (TextView) convertView.findViewById(R.id.tag3);
            tTag4 = (TextView) convertView.findViewById(R.id.tag4);
            tTag5 = (TextView) convertView.findViewById(R.id.tag5);
            tScore = (TextView) convertView.findViewById(R.id.questionscore);

        }
    }

}
