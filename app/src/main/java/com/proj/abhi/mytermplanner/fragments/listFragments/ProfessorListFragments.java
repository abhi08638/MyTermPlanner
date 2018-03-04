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
import com.proj.abhi.mytermplanner.cursorAdapters.PhonesCursorAdapter;
import com.proj.abhi.mytermplanner.generics.GenericActivity;
import com.proj.abhi.mytermplanner.generics.GenericListFragment;
import com.proj.abhi.mytermplanner.providers.HomeAssessmentsProvider;
import com.proj.abhi.mytermplanner.providers.HomeCoursesProvider;
import com.proj.abhi.mytermplanner.providers.PhonesProvider;
import com.proj.abhi.mytermplanner.providers.TasksProvider;
import com.proj.abhi.mytermplanner.providers.TermsProvider;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.DateUtils;
import com.proj.abhi.mytermplanner.utils.Utils;

public class ProfessorListFragments extends GenericListFragment implements LoaderCallbacks<Cursor> {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        switch (initializer.getInt(Constants.CURSOR_LOADER_ID)) {
            case Constants.CursorLoaderIds.PHONE_ID:
                cursorAdapter = new PhonesCursorAdapter(getActivity(), null, 0);
                setEmptyText("No " + getString(R.string.phones));
                break;

        }
        setListAdapter(cursorAdapter);
        if(savedInstanceState==null)
            initLoader();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        String[] cols = null;
        String where = null;
        String order = null;
        if (bundle != null) {
            Uri uri = Uri.parse((String) bundle.get(Constants.CONTENT_URI));
            if (uri.equals(PhonesProvider.CONTENT_URI)) {
                where=Constants.Ids.PROF_ID+"="+((GenericActivity)getActivity()).getCurrentUriId();
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
