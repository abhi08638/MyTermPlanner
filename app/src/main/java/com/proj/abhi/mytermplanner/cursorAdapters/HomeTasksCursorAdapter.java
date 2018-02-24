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

        String title = cursor.getString(
                cursor.getColumnIndex(Constants.Task.TASK_TITLE));
        String startDate = Utils.getUserDate(cursor.getString(
                cursor.getColumnIndex(Constants.Task.TASK_START_DATE)));
        String endDate = Utils.getUserDate(cursor.getString(
                cursor.getColumnIndex(Constants.Task.TASK_END_DATE)));
        TextView t = (TextView) view.findViewById(R.id.item1);
        TextView sd = (TextView) view.findViewById(R.id.item2);
        TextView ed = (TextView) view.findViewById(R.id.item3);
        t.setText(title);
        sd.setText("Start Date: "+startDate);
        ed.setText("End Date: "+endDate);

        view.findViewById(R.id.item4).setVisibility(View.GONE);
        view.findViewById(R.id.item5).setVisibility(View.GONE);

    }
}
