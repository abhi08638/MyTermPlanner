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

public class ProfsCursorAdapter extends CursorAdapter{
    public ProfsCursorAdapter(Context context, Cursor c, int flags) {
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
                cursor.getColumnIndex(Constants.Professor.TITLE));
        String firstName = cursor.getString(
                cursor.getColumnIndex(Constants.Professor.FIRST_NAME));
        String middleName = cursor.getString(
                cursor.getColumnIndex(Constants.Professor.MIDDLE_NAME));
        String lastName = cursor.getString(
                cursor.getColumnIndex(Constants.Professor.LAST_NAME));

        String fullName;
        if(middleName.trim()=="" || middleName==null){
            fullName=title+" "+firstName+" "+lastName;
        }else{
            fullName=title+" "+firstName+" "+middleName+" "+lastName;
        }

        TextView i1 = (TextView) view.findViewById(R.id.item1);

        i1.setText(fullName);

        view.findViewById(R.id.item2).setVisibility(View.GONE);
        view.findViewById(R.id.item3).setVisibility(View.GONE);
        view.findViewById(R.id.item4).setVisibility(View.GONE);
        view.findViewById(R.id.item5).setVisibility(View.GONE);
    }
}
