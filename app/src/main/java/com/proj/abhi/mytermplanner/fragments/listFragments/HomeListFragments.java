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
import com.proj.abhi.mytermplanner.activities.AssessmentActivity;
import com.proj.abhi.mytermplanner.activities.CourseActivity;
import com.proj.abhi.mytermplanner.activities.TaskActivity;
import com.proj.abhi.mytermplanner.activities.TermActivity;
import com.proj.abhi.mytermplanner.cursorAdapters.HomeAssessmentsCursorAdapter;
import com.proj.abhi.mytermplanner.cursorAdapters.HomeCoursesCursorAdapter;
import com.proj.abhi.mytermplanner.cursorAdapters.HomeTasksCursorAdapter;
import com.proj.abhi.mytermplanner.cursorAdapters.HomeTermsCursorAdapter;
import com.proj.abhi.mytermplanner.generics.GenericListFragment;
import com.proj.abhi.mytermplanner.providers.HomeAssessmentsProvider;
import com.proj.abhi.mytermplanner.providers.HomeCoursesProvider;
import com.proj.abhi.mytermplanner.providers.TasksProvider;
import com.proj.abhi.mytermplanner.providers.TermsProvider;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.DateUtils;
import com.proj.abhi.mytermplanner.utils.Utils;

public class HomeListFragments extends GenericListFragment implements LoaderCallbacks<Cursor> {

    private String sortOrder;
    private int numQueryDays;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        numQueryDays = initializer.getInt(Constants.SharedPreferenceKeys.NUM_QUERY_DAYS);
        switch (initializer.getInt(Constants.CURSOR_LOADER_ID)) {
            case Constants.CursorLoaderIds.TERM_ID:
                cursorAdapter = new HomeTermsCursorAdapter(getActivity(), null, 0);
                setEmptyText("No " + getString(R.string.terms));
                break;
            case Constants.CursorLoaderIds.HOME_COURSE_ID:
                cursorAdapter = new HomeCoursesCursorAdapter(getActivity(), null, 0);
                setEmptyText("No " + getString(R.string.courses));
                break;
            case Constants.CursorLoaderIds.HOME_ASSESSMENT_ID:
                cursorAdapter = new HomeAssessmentsCursorAdapter(getActivity(), null, 0);
                setEmptyText("No " + getString(R.string.assessments));
                break;
            case Constants.CursorLoaderIds.TASK_ID:
                cursorAdapter = new HomeTasksCursorAdapter(getActivity(), null, 0);
                setEmptyText("No " + getString(R.string.tasks));
                break;
        }
        setListAdapter(cursorAdapter);
        if(savedInstanceState==null)
            initLoader();
    }

    public void restartLoader(Bundle b) {
        if (b != null)
            numQueryDays = b.getInt(Constants.SharedPreferenceKeys.NUM_QUERY_DAYS);
        super.restartLoader();
    }

    private String getWhereClause() {
        String num = Integer.toString(numQueryDays);

        if (numQueryDays > 0) {
            sortOrder = "";
            return " between strftime(" + DateUtils.getSqlDateNowStart() + ") and strftime(" + DateUtils.getSqlDateNowEnd() + ",'" + num + " days')";
        } else if (numQueryDays < 0) {
            sortOrder = "desc";
            return " between strftime(" + DateUtils.getSqlDateNowStart() + ",'" + num + " days') and strftime(" + DateUtils.getSqlDateNowEnd() + ",'-1 day')";
        } else {
            sortOrder = "";
            return " between strftime(" + DateUtils.getSqlDateNowStart() + ") and strftime(" + DateUtils.getSqlDateNowEnd() + ")";
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        String[] cols = null;
        String where = null;
        String order = null;
        if (bundle != null) {
            Uri uri = Uri.parse((String) bundle.get(Constants.CONTENT_URI));
            if (uri.equals(TermsProvider.CONTENT_URI)) {
                if (numQueryDays >= 0) {
                    where = Constants.Term.TERM_START_DATE + getWhereClause() +
                            " OR (" + Constants.Term.TERM_START_DATE + " <= strftime(" + Utils.getSqlDateNow() + ") " +
                            "AND " + Constants.Term.TERM_END_DATE + " >= strftime(" + Utils.getSqlDateNow() + "))";
                } else {
                    where = Constants.Term.TERM_END_DATE + getWhereClause();
                }
                order = Constants.Term.TERM_START_DATE + " " + sortOrder + "," + Constants.Term.TERM_END_DATE + " " + sortOrder;
            } else if (uri.equals(HomeCoursesProvider.CONTENT_URI)) {
                //raw query
                if (numQueryDays >= 0) {
                    where = "WHERE c." + Constants.Course.COURSE_START_DATE + getWhereClause() +
                            " OR (c." + Constants.Course.COURSE_START_DATE + " <= strftime(" + Utils.getSqlDateNow() + ") " +
                            "AND c." + Constants.Course.COURSE_END_DATE + " >= strftime(" + Utils.getSqlDateNow() + "))";
                } else {
                    where = "WHERE c." + Constants.Course.COURSE_END_DATE + getWhereClause();
                }
                where += " ORDER BY c." + Constants.Course.COURSE_START_DATE + " " + sortOrder + ", c." + Constants.Course.COURSE_END_DATE + " " + sortOrder;
            } else if (uri.equals(HomeAssessmentsProvider.CONTENT_URI)) {
                //raw query
                where = "WHERE a." + Constants.Assessment.ASSESSMENT_END_DATE + getWhereClause();
                where += " ORDER BY a." + Constants.Assessment.ASSESSMENT_END_DATE + " " + sortOrder;
            } else if (uri.equals(TasksProvider.CONTENT_URI)) {
                if (numQueryDays >= 0) {
                    where = Constants.Task.TASK_START_DATE + getWhereClause() +
                            " OR (" + Constants.Task.TASK_START_DATE + " <= strftime(" + DateUtils.getSqlDateNowStart() + ") " +
                            "AND " + Constants.Task.TASK_END_DATE + " >= strftime(" + DateUtils.getSqlDateNowStart() + "))";
                } else {
                    where = Constants.Task.TASK_END_DATE + getWhereClause();
                }

                order = Constants.Task.TASK_START_DATE + " " + sortOrder + "," + Constants.Task.TASK_END_DATE + " " + sortOrder;
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
            switch (initializer.getInt(Constants.CURSOR_LOADER_ID)) {
                case Constants.CursorLoaderIds.TERM_ID:
                    Utils.sendToActivity(l.intValue(), TermActivity.class, TermsProvider.CONTENT_URI);
                    break;
                case Constants.CursorLoaderIds.HOME_COURSE_ID:
                    Utils.sendToActivity(l.intValue(), CourseActivity.class, HomeCoursesProvider.CONTENT_URI);
                    break;
                case Constants.CursorLoaderIds.HOME_ASSESSMENT_ID:
                    Utils.sendToActivity(l.intValue(), AssessmentActivity.class, HomeAssessmentsProvider.CONTENT_URI);
                    break;
                case Constants.CursorLoaderIds.TASK_ID:
                    Utils.sendToActivity(l.intValue(), TaskActivity.class, TasksProvider.CONTENT_URI);
                    break;
                default:
                    break;
            }
        }
    }
}
