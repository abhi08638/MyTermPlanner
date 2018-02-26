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
import com.proj.abhi.mytermplanner.cursorAdapters.HomeAssessmentsCursorAdapter;
import com.proj.abhi.mytermplanner.cursorAdapters.HomeCoursesCursorAdapter;
import com.proj.abhi.mytermplanner.cursorAdapters.HomeTasksCursorAdapter;
import com.proj.abhi.mytermplanner.cursorAdapters.HomeTermsCursorAdapter;
import com.proj.abhi.mytermplanner.providers.HomeAssessmentsProvider;
import com.proj.abhi.mytermplanner.providers.HomeCoursesProvider;
import com.proj.abhi.mytermplanner.providers.TasksProvider;
import com.proj.abhi.mytermplanner.providers.TermsProvider;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.Utils;

public class HomeGenericFragment extends ListFragment implements LoaderCallbacks<Cursor>, OnItemClickListener {

    private CursorAdapter termCursorAdapter,courseCursorAdapter,assessmentCursorAdapter,taskCursorAdapter;
    private String sortOrder;
    private int numQueryDays;
    private Bundle initializer=null;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initializer=getArguments();
        numQueryDays=initializer.getInt(Constants.SharedPreferenceKeys.NUM_QUERY_DAYS);
        switch (initializer.getInt(Constants.CURSOR_LOADER_ID)) {
            case Constants.CursorLoaderIds.TERM_ID:
                termCursorAdapter = new HomeTermsCursorAdapter(getActivity(),null,0);
                setListAdapter(termCursorAdapter);
                setEmptyText("No "+getString(R.string.terms));
                break;
            case Constants.CursorLoaderIds.HOME_COURSE_ID:
                courseCursorAdapter = new HomeCoursesCursorAdapter(getActivity(),null,0);
                setListAdapter(courseCursorAdapter);
                setEmptyText("No "+getString(R.string.courses));
                break;
            case Constants.CursorLoaderIds.HOME_ASSESSMENT_ID:
                assessmentCursorAdapter = new HomeAssessmentsCursorAdapter(getActivity(),null,0);
                setListAdapter(assessmentCursorAdapter);
                setEmptyText("No "+getString(R.string.assessments));
                break;
            case Constants.CursorLoaderIds.TASK_ID:
                taskCursorAdapter = new HomeTasksCursorAdapter(getActivity(),null,0);
                setListAdapter(taskCursorAdapter);
                setEmptyText("No "+getString(R.string.tasks));
                break;
        }
        initLoader();
    }

    private void initLoader(){
        getLoaderManager().initLoader(initializer.getInt(Constants.CURSOR_LOADER_ID),initializer,this);
    }

    public void restartLoader(Bundle b){
        numQueryDays=b.getInt(Constants.SharedPreferenceKeys.NUM_QUERY_DAYS);
        getLoaderManager().restartLoader(initializer.getInt(Constants.CURSOR_LOADER_ID),initializer,this);
    }

    private String getWhereClause(){
        String num=Integer.toString(numQueryDays);

        if(numQueryDays>0){
            sortOrder="";
            return " between strftime("+Utils.getSqlDateNow()+") and strftime("+Utils.getSqlDateNow()+",'"+num+" days')";
        }else if(numQueryDays<0){
            sortOrder="desc";
            return " between strftime("+Utils.getSqlDateNow()+",'"+num+" days') and strftime("+Utils.getSqlDateNow()+",'-1 day')";
        }else{
            sortOrder="";
            return " = strftime("+Utils.getSqlDateNow()+")";
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        String[] cols = null;
        String where = null;
        String order = null;
        if(bundle!=null){
            Uri uri = Uri.parse((String) bundle.get("contentUri"));
            if(uri.equals(TermsProvider.CONTENT_URI)){
                if(numQueryDays>=0){
                    where=Constants.Term.TERM_START_DATE+getWhereClause()+
                            " OR ("+Constants.Term.TERM_START_DATE+" <= strftime("+Utils.getSqlDateNow()+") " +
                            "AND "+Constants.Term.TERM_END_DATE+" >= strftime("+Utils.getSqlDateNow()+"))";
                }else{
                    where=Constants.Term.TERM_END_DATE+getWhereClause();
                }
                order=Constants.Term.TERM_START_DATE+" "+sortOrder+","+Constants.Term.TERM_END_DATE+" "+sortOrder;
            }else if(uri.equals(HomeCoursesProvider.CONTENT_URI)){
                //raw query
                if(numQueryDays>=0){
                    where="WHERE c."+Constants.Course.COURSE_START_DATE+getWhereClause()+
                            " OR (c."+Constants.Course.COURSE_START_DATE+" <= strftime("+Utils.getSqlDateNow()+") " +
                            "AND c."+Constants.Course.COURSE_END_DATE+" >= strftime("+Utils.getSqlDateNow()+"))";
                }else{
                    where="WHERE c."+Constants.Course.COURSE_END_DATE+getWhereClause();
                }
                where+=" ORDER BY c."+Constants.Course.COURSE_START_DATE +" "+sortOrder+", c."+Constants.Course.COURSE_END_DATE+" "+sortOrder;
            }else if(uri.equals(HomeAssessmentsProvider.CONTENT_URI)){
                //raw query
                where="WHERE a."+Constants.Assessment.ASSESSMENT_END_DATE+getWhereClause();
                where+=" ORDER BY a."+Constants.Assessment.ASSESSMENT_END_DATE+" "+sortOrder;
            }else if(uri.equals(TasksProvider.CONTENT_URI)){
                if(numQueryDays>=0){
                    where=Constants.Task.TASK_START_DATE+getWhereClause()+
                            " OR ("+Constants.Task.TASK_START_DATE+" <= strftime("+Utils.getSqlDateNow()+") " +
                            "AND "+Constants.Task.TASK_END_DATE+" >= strftime("+Utils.getSqlDateNow()+"))";
                }else{
                    where=Constants.Task.TASK_END_DATE+getWhereClause();
                }

                order=Constants.Task.TASK_START_DATE+" "+sortOrder+","+Constants.Task.TASK_END_DATE+" "+sortOrder;
            }
            return new CursorLoader(getActivity(), uri,
                    cols,where,null,order);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(loader.getId()==Constants.CursorLoaderIds.TERM_ID){
            termCursorAdapter.swapCursor(cursor);
        }else if(loader.getId()==Constants.CursorLoaderIds.HOME_COURSE_ID){
            courseCursorAdapter.swapCursor(cursor);
        }else if(loader.getId()==Constants.CursorLoaderIds.HOME_ASSESSMENT_ID){
            assessmentCursorAdapter.swapCursor(cursor);
        }else if(loader.getId()==Constants.CursorLoaderIds.TASK_ID){
            taskCursorAdapter.swapCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(loader.getId()==Constants.CursorLoaderIds.TERM_ID){
            termCursorAdapter.swapCursor(null);
        }else if(loader.getId()==Constants.CursorLoaderIds.HOME_COURSE_ID){
            courseCursorAdapter.swapCursor(null);
        }else if(loader.getId()==Constants.CursorLoaderIds.HOME_ASSESSMENT_ID){
            assessmentCursorAdapter.swapCursor(null);
        }else if(loader.getId()==Constants.CursorLoaderIds.TASK_ID){
            taskCursorAdapter.swapCursor(null);
        }
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
