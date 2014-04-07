package com.vidhi.sodemoapp; /**
 * Created by vidhi on 3/24/14.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomListViewAdapter extends ArrayAdapter<QuestionInfo> {
    Context context;
    ArrayList questionInfos;

    public CustomListViewAdapter(Context paramContext, int paramInt, ArrayList<QuestionInfo> questionInfos) {
        super(paramContext, paramInt, questionInfos);

        this.questionInfos = questionInfos;

        this.context = paramContext;

    }

    /**
     * Function used by ArrayAdapter to iterate through the list of questionInfo objects and render each in the single_list_element layout
     *
     * @param paramInt       - used to access the current element from the list of questionInfo objects
     * @param paramView      - single_list_element layout
     * @param paramViewGroup
     * @return listview with rendered data of questionInfo objects
     */
    @Override
    public View getView(int paramInt, View paramView, ViewGroup paramViewGroup) {
        QuestionInfo qInfo;
        // assign the view we are converting to a local variable
        View v = paramView;
        // first check to see if the view is null. if so, we have to inflate it.
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.single_list_element, null);
        }

        /*
         * Recall that the variable position is sent in as an argument to this method.
		 * The variable simply refers to the position of the current object in the list. (The ArrayAdapter
		 * iterates through the list we sent it)
		 *
		 * Therefore, paramInt refers to the current Item object.
		 */
        qInfo = (QuestionInfo) getItem(paramInt);

        if (qInfo != null) {

            TextView tQuestionTitle = (TextView) v.findViewById(R.id.questiontitle);
            TextView tTag1 = (TextView) v.findViewById(R.id.tag1);
            TextView tTag2 = (TextView) v.findViewById(R.id.tag2);
            TextView tTag3 = (TextView) v.findViewById(R.id.tag3);
            TextView tTag4 = (TextView) v.findViewById(R.id.tag4);
            TextView tTag5 = (TextView) v.findViewById(R.id.tag5);
            TextView tScore = (TextView) v.findViewById(R.id.questionscore);

            // check to see if each individual textview is null.
            // if not, assign some text
            if (tQuestionTitle != null) {
                tQuestionTitle.setText(qInfo.getQuestion());
            }

            String[] tempTags = qInfo.getTags();

            if (tTag1 != null && tempTags[0] != null) {
                tTag1.setText(tempTags[0]);
            }
            if (tTag2 != null && tempTags[1] != null) {
                tTag2.setText(tempTags[1]);
            }
            if (tTag3 != null && tempTags[2] != null) {
                tTag3.setText(tempTags[2]);
            }
            if (tTag4 != null && tempTags[3] != null) {
                tTag4.setText(tempTags[3]);
            }
            if (tTag5 != null && tempTags[4] != null) {
                tTag5.setText(tempTags[4]);
            }
            if (tScore != null) {
                tScore.setText(qInfo.getScore());
            }
        }
        // the view must be returned to our activity
        return v;
    }

}