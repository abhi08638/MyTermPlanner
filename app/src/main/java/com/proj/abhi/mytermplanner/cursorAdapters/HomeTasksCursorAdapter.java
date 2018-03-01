package com.proj.abhi.mytermplanner.cursorAdapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.pojos.TaskPojo;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.DateUtils;
import com.proj.abhi.mytermplanner.utils.Utils;

public class HomeTasksCursorAdapter extends CursorAdapter{
    public HomeTasksCursorAdapter(Context context, Cursor c, int flags) {
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
        TaskPojo task = new TaskPojo();
        task.initPojo(cursor);
        TextView i1 = (TextView) view.findViewById(R.id.item1);
        TextView i2 = (TextView) view.findViewById(R.id.item2);
        TextView i3 = (TextView) view.findViewById(R.id.item3);
        i1.setTextSize(18);
        i1.setText(task.getTitle());
        i2.setText("Start Date: "+DateUtils.getUserDateTime(task.getStartDate()));
        i3.setText("End Date: "+DateUtils.getUserDateTime(task.getEndDate()));

        view.findViewById(R.id.item4).setVisibility(View.GONE);
        view.findViewById(R.id.item5).setVisibility(View.GONE);

    }
}
