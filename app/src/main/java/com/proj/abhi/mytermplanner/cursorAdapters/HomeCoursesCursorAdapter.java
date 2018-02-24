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

public class HomeCoursesCursorAdapter extends CursorAdapter{
    public HomeCoursesCursorAdapter(Context context, Cursor c, int flags) {
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

        String courseTitle = cursor.getString(
                cursor.getColumnIndex("courseTitle"));
        String startDate = Utils.getUserDate(cursor.getString(
                cursor.getColumnIndex(Constants.Course.COURSE_START_DATE)));
        String endDate = Utils.getUserDate(cursor.getString(
                cursor.getColumnIndex(Constants.Course.COURSE_END_DATE)));
        String termTitle = cursor.getString(
                cursor.getColumnIndex("termTitle"));
        TextView ct = (TextView) view.findViewById(R.id.item1);
        TextView sd = (TextView) view.findViewById(R.id.item2);
        TextView ed = (TextView) view.findViewById(R.id.item3);
        TextView tt = (TextView) view.findViewById(R.id.item4);
        ct.setText(courseTitle);
        sd.setText("Start Date: "+startDate);
        ed.setText("End Date: "+endDate);
        tt.setText("Term: "+termTitle);

        view.findViewById(R.id.item5).setVisibility(View.GONE);
    }
}
