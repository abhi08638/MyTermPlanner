package com.proj.abhi.mytermplanner.cursorAdapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.Utils;

public class HomeAssessmentsCursorAdapter extends CursorAdapter{
    public HomeAssessmentsCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(
                R.layout.list_item, parent, false
        );
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        String assessmentTitle = cursor.getString(
                cursor.getColumnIndex("assessmentTitle"));
        String courseTitle = cursor.getString(
                cursor.getColumnIndex("courseTitle"));
        String type = cursor.getString(
                cursor.getColumnIndex(Constants.Assessment.TYPE));
        String endDate = Utils.getUserDate(cursor.getString(
                cursor.getColumnIndex(Constants.Assessment.ASSESSMENT_END_DATE)));
        String termTitle = cursor.getString(
                cursor.getColumnIndex("termTitle"));
        TextView i1 = (TextView) view.findViewById(R.id.item1);
        TextView i2 = (TextView) view.findViewById(R.id.item2);
        TextView i3 = (TextView) view.findViewById(R.id.item3);
        TextView i4 = (TextView) view.findViewById(R.id.item4);
        TextView i5 = (TextView) view.findViewById(R.id.item5);
        i1.setText(type);
        i2.setText(assessmentTitle);
        i3.setText("End Date: "+endDate);
        i4.setText("Course: "+courseTitle);
        i5.setText("Term: "+termTitle);

    }
}
