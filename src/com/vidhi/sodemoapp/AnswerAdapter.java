package com.vidhi.sodemoapp;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import us.feras.mdv.MarkdownView;

import java.util.ArrayList;

/**
 * Created by vidhi on 4/19/14.
 */
public class AnswerAdapter  extends BaseExpandableListAdapter {

    private ArrayList<AnswerInfo> answerCollection;
    private Activity context;

    public AnswerAdapter(ArrayList answerInfos, Activity mainContext){

        context = mainContext;
        answerCollection = answerInfos;
        Log.d("vidhi", "inside answer adapter constructor");

    }

    @Override
    public int getGroupCount() {
        return answerCollection.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return 1; //there is always one child in our use case
    }

    @Override
    public Object getGroup(int groupPosition) {
        return answerCollection.get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }


    @Override
    public boolean hasStableIds() {
        return true;
    }


    public Object getChild(int groupPosition, int childPosition) {
        Log.d("vidhi", "getChild :"+ groupPosition + " " + childPosition);
        return answerCollection.get(childPosition);
    }

    public long getChildId(int groupPosition, int childPosition) {
        Log.d("vidhi", "getChidID = "+groupPosition+ " " + childPosition);
        return childPosition;
    }


    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        //fool the listview into believing it has only 1 child

        final AnswerInfo answer = (AnswerInfo) getGroup(groupPosition);
        Log.d("vidhi", "getGroupView " + groupPosition+ " ");
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.group_item,
                    null);
        }
        TextView answerTextView = (TextView) convertView.findViewById(R.id.answerHeaderView);

        answerTextView.setText(answer.getBodyMarkdown());
        TextView textViewUpvoteCount = (TextView) convertView.findViewById(R.id.upvoteCount);
        TextView textViewDownvoteCount = (TextView) convertView.findViewById(R.id.downvoteCount);

        TextView textScore = (TextView) convertView.findViewById(R.id.answerScore);
        textScore.setText(""+answer.getScore());

        textViewDownvoteCount.setText(""+answer.getDownVoteCount());
        textViewUpvoteCount.setText(""+answer.getUpvoteCount());


        return convertView;
    }


    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
       // if(groupPosition == childPosition){
            final AnswerInfo answer = (AnswerInfo) getChild(groupPosition, groupPosition);
            Log.d("vidhi", "getChildView " + groupPosition+ " " +groupPosition );
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.child_item, null);
            }

            MarkdownView answerInfo = (MarkdownView) convertView.findViewById(R.id.answerInfo);

            answerInfo.loadMarkDownData(answer.getBodyMarkdown());
        //}
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
