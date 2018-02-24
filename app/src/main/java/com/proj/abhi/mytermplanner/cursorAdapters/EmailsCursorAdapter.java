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

public class EmailsCursorAdapter extends CursorAdapter{
    public EmailsCursorAdapter(Context context, Cursor c, int flags) {
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

        String email = cursor.getString(
                cursor.getColumnIndex(Constants.Professor.EMAIL));
        String type = cursor.getString(
                cursor.getColumnIndex(Constants.Professor.EMAIL_TYPE));

        TextView i1 = (TextView) view.findViewById(R.id.item1);
        TextView i2 = (TextView) view.findViewById(R.id.item2);

        i1.setText(type);
        i2.setText(email);

        view.findViewById(R.id.item3).setVisibility(View.GONE);
        view.findViewById(R.id.item4).setVisibility(View.GONE);
        view.findViewById(R.id.item5).setVisibility(View.GONE);
    }
}
