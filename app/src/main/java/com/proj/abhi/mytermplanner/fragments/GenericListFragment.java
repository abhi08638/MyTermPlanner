package com.proj.abhi.mytermplanner.fragments;

/**
 * Created by Abhi on 2/25/2018.
 */

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ListView;

import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.activities.AssessmentActivity;
import com.proj.abhi.mytermplanner.activities.CourseActivity;
import com.proj.abhi.mytermplanner.activities.TaskActivity;
import com.proj.abhi.mytermplanner.activities.TermActivity;
import com.proj.abhi.mytermplanner.cursorAdapters.AlarmsCursorAdapter;
import com.proj.abhi.mytermplanner.cursorAdapters.HomeAssessmentsCursorAdapter;
import com.proj.abhi.mytermplanner.cursorAdapters.HomeCoursesCursorAdapter;
import com.proj.abhi.mytermplanner.cursorAdapters.HomeTasksCursorAdapter;
import com.proj.abhi.mytermplanner.cursorAdapters.HomeTermsCursorAdapter;
import com.proj.abhi.mytermplanner.providers.AlarmsProvider;
import com.proj.abhi.mytermplanner.providers.HomeAssessmentsProvider;
import com.proj.abhi.mytermplanner.providers.HomeCoursesProvider;
import com.proj.abhi.mytermplanner.providers.TasksProvider;
import com.proj.abhi.mytermplanner.providers.TermsProvider;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.Utils;

public class GenericListFragment extends ListFragment implements LoaderCallbacks<Cursor>, OnItemClickListener {

    private CursorAdapter cursorAdapter;
    private String sortOrder;
    private int numQueryDays;
    private Bundle initializer=null;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setDivider(null);
        getListView().setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.content, null));
        initializer=getArguments();
        switch (initializer.getInt(Constants.CURSOR_LOADER_ID)) {
            case Constants.CursorLoaderIds.ALARM_ID:
                cursorAdapter = new AlarmsCursorAdapter(getActivity(),null,0);
                setEmptyText("No "+getString(R.string.reminders));
                break;
            case Constants.CursorLoaderIds.HOME_COURSE_ID:
                cursorAdapter = new HomeCoursesCursorAdapter(getActivity(),null,0);
                setEmptyText("No "+getString(R.string.courses));
                break;
            case Constants.CursorLoaderIds.HOME_ASSESSMENT_ID:
                cursorAdapter = new HomeAssessmentsCursorAdapter(getActivity(),null,0);
                setEmptyText("No "+getString(R.string.assessments));
                break;
            case Constants.CursorLoaderIds.TASK_ID:
                cursorAdapter = new HomeTasksCursorAdapter(getActivity(),null,0);
                setEmptyText("No "+getString(R.string.tasks));
                break;
        }
        setListAdapter(cursorAdapter);
        initLoader();
    }

    private void initLoader(){
        getLoaderManager().initLoader(initializer.getInt(Constants.CURSOR_LOADER_ID),initializer,this);
    }

    public void restartLoader(Bundle b){
        getLoaderManager().restartLoader(initializer.getInt(Constants.CURSOR_LOADER_ID),initializer,this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        String[] cols = null;
        String where = null;
        String order = null;
        if(bundle!=null){
            Uri uri = Uri.parse((String) bundle.get(Constants.CONTENT_URI));
            if(uri.equals(AlarmsProvider.CONTENT_URI)){
                where=initializer.getString(Constants.Sql.WHERE);
            }
            return new CursorLoader(getActivity(), uri,
                    cols,where,null,order);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        cursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
    }

    @Override
    public void onListItemClick(ListView parent, View view, int position, long id) {
        if(id>0){
            Long l = new Long(id);
            switch (initializer.getInt(Constants.CURSOR_LOADER_ID)) {
                case Constants.CursorLoaderIds.TERM_ID:
                    Utils.sendToActivity(l.intValue(),getActivity(),TermActivity.class,TermsProvider.CONTENT_URI);
                    break;
                case Constants.CursorLoaderIds.HOME_COURSE_ID:
                    Utils.sendToActivity(l.intValue(),getActivity(),CourseActivity.class,HomeCoursesProvider.CONTENT_URI);
                    break;
                case Constants.CursorLoaderIds.HOME_ASSESSMENT_ID:
                    Utils.sendToActivity(l.intValue(),getActivity(),AssessmentActivity.class,HomeAssessmentsProvider.CONTENT_URI);
                    break;
                case Constants.CursorLoaderIds.TASK_ID:
                    Utils.sendToActivity(l.intValue(),getActivity(),TaskActivity.class,TasksProvider.CONTENT_URI);
                    break;
            }
        }
    }
}
