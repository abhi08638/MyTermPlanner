package com.proj.abhi.mytermplanner.fragments.listFragments;

/**
 * Created by Abhi on 2/25/2018.
 */

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;

import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.activities.AssessmentActivity;
import com.proj.abhi.mytermplanner.activities.CourseActivity;
import com.proj.abhi.mytermplanner.activities.TaskActivity;
import com.proj.abhi.mytermplanner.activities.TermActivity;
import com.proj.abhi.mytermplanner.providers.HomeAssessmentsProvider;
import com.proj.abhi.mytermplanner.providers.HomeCoursesProvider;
import com.proj.abhi.mytermplanner.providers.TasksProvider;
import com.proj.abhi.mytermplanner.providers.TermsProvider;
import com.proj.abhi.mytermplanner.services.AlarmTask;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.Utils;

public abstract class GenericListFragment extends ListFragment implements LoaderCallbacks<Cursor> {

    protected CursorAdapter cursorAdapter;
    protected CoordinatorLayout mCoordinatorLayout;
    protected Bundle initializer = null;
    protected boolean restartAll = true;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setDivider(null);
        mCoordinatorLayout = (CoordinatorLayout) getActivity().findViewById(R.id.coordinatorLayout);
        initializer = getArguments();
        if (initializer == null) {
            initializer = new Bundle();
        }
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> av, View v, int position, long l) {
                Long val = new Long(l);
                final int id = val.intValue();
                DialogInterface.OnClickListener dialogClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int button) {
                                if (button == DialogInterface.BUTTON_POSITIVE) {
                                    String cancelAlarmsWhere = " WHERE " + initializer.get(Constants.ID) + "=" + id;
                                    new AlarmTask(getActivity(), null, null).cancelAlarms(cancelAlarmsWhere);
                                    String where = Constants.ID + "=" + id;
                                    getActivity().getContentResolver().delete(Uri.parse((String) initializer.get(Constants.CONTENT_URI)), where, null);
                                    restartLoaders(restartAll);
                                    Snackbar.make(mCoordinatorLayout, R.string.deleted, Snackbar.LENGTH_LONG).show();
                                }
                            }
                        };

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setTitle(R.string.do_delete)
                        .setPositiveButton(getString(R.string.delete), dialogClickListener)
                        .setNegativeButton(getString(android.R.string.no), dialogClickListener);

                final AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                alertDialog.setCanceledOnTouchOutside(false);
                return true;
            }
        });
    }

    private void restartLoaders(boolean restartAll) {
        if (restartAll) {
            for (android.support.v4.app.Fragment f : getActivity().getSupportFragmentManager().getFragments()) {
                if (f instanceof GenericListFragment) {
                    ((GenericListFragment) f).restartLoader();
                }
            }
        } else {
            restartLoader();
        }
    }

    protected void initLoader() {
        getLoaderManager().initLoader(initializer.getInt(Constants.CURSOR_LOADER_ID), initializer, this);
    }

    public void restartLoader() {
        getLoaderManager().restartLoader(initializer.getInt(Constants.CURSOR_LOADER_ID), initializer, this);
    }

    @Override
    public abstract Loader<Cursor> onCreateLoader(int id, Bundle bundle);

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        cursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
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
            }
        }
    }


}
