package com.proj.abhi.mytermplanner.fragments.listFragments;

/**
 * Created by Abhi on 2/25/2018.
 */

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;

import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.activities.CourseActivity;
import com.proj.abhi.mytermplanner.cursorAdapters.HomeCoursesCursorAdapter;
import com.proj.abhi.mytermplanner.generics.GenericListFragment;
import com.proj.abhi.mytermplanner.providers.CoursesProviderOld;
import com.proj.abhi.mytermplanner.providers.CoursesProvider;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.Utils;

import java.util.LinkedHashMap;

public class CourseListFragment extends GenericListFragment implements LoaderCallbacks<Cursor> {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        cursorAdapter = new HomeCoursesCursorAdapter(getActivity(), null, 0);
        setEmptyText("No " + getString(R.string.courses));
        setListAdapter(cursorAdapter);
        if(savedInstanceState==null)
            initLoader();
    }

    public void restartLoader(Bundle b) {
        initializer.putInt(Constants.Ids.TERM_ID,b.getInt(Constants.Ids.TERM_ID));
        super.restartLoader();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        String[] cols = null;
        String where = null;
        String order = null;
        if (bundle != null) {
            Uri uri = Uri.parse((String) bundle.get(Constants.CONTENT_URI));
           if (uri.equals(CoursesProvider.CONTENT_URI)) {
               where = "WHERE c."+Constants.Ids.TERM_ID+"="+bundle.getInt(Constants.Ids.TERM_ID);
            }
            return new CursorLoader(getActivity(), uri,
                    cols, where, null, order);
        }
        return null;
    }

    @Override
    public void onListItemClick(ListView parent, View view, int position, long id) {
        if (id > 0) {
            Long l = new Long(id);
            Cursor c = ((HomeCoursesCursorAdapter)parent.getAdapter()).getCursor();
            c.moveToPosition(position);
            LinkedHashMap<String,Integer> params = new LinkedHashMap<>();
            params.put(Constants.Ids.TERM_ID,c.getInt(c.getColumnIndex(Constants.Ids.TERM_ID)));
            c.close();
            Utils.sendToActivity(l.intValue(), CourseActivity.class, CoursesProvider.CONTENT_URI,params);
        }
    }

    public void doFabAction(){
        LinkedHashMap<String,Integer> params = new LinkedHashMap<>();
        params.put(Constants.Ids.TERM_ID,initializer.getInt(Constants.Ids.TERM_ID));
        Utils.sendToActivity(0, CourseActivity.class, CoursesProvider.CONTENT_URI,params);
    }
}
