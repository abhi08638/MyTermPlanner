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

public class AssessmentsCursorAdapter extends CursorAdapter{
    public AssessmentsCursorAdapter(Context context, Cursor c, int flags) {
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
                cursor.getColumnIndex(Constants.Assessment.ASSESSMENT_TITLE));
        TextView ct = (TextView) view.findViewById(R.id.item1);
        ct.setText(title);
        view.findViewById(R.id.item2).setVisibility(View.GONE);
        view.findViewById(R.id.item3).setVisibility(View.GONE);
        view.findViewById(R.id.item4).setVisibility(View.GONE);
        view.findViewById(R.id.item5).setVisibility(View.GONE);

    }
}
