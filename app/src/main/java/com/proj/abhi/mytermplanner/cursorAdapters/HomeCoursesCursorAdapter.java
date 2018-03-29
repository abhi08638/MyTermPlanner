package com.proj.abhi.mytermplanner.cursorAdapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.pojos.CoursePojo;
import com.proj.abhi.mytermplanner.utils.DateUtils;

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
        CoursePojo course = new CoursePojo();
        course.initPojo(cursor);
        String termTitle = cursor.getString(
                cursor.getColumnIndex("termTitle"));
        TextView i1 = (TextView) view.findViewById(R.id.item1);
        TextView i2 = (TextView) view.findViewById(R.id.item2);
        TextView i3 = (TextView) view.findViewById(R.id.item3);
        TextView i4 = (TextView) view.findViewById(R.id.item4);
        i1.setText(course.getTitle());
        i1.setTextSize(18);
        i2.setText("Start Date: "+ DateUtils.getUserDate(course.getStartDate()));
        i3.setText("End Date: "+DateUtils.getUserDate(course.getEndDate()));
        i4.setText("Term: "+termTitle);

        view.findViewById(R.id.item5).setVisibility(View.GONE);
    }
}
