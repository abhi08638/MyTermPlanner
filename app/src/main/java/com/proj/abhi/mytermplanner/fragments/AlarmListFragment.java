package com.proj.abhi.mytermplanner.fragments;

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
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Toast;

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
import com.proj.abhi.mytermplanner.providers.CoursesProfsProvider;
import com.proj.abhi.mytermplanner.providers.HomeAssessmentsProvider;
import com.proj.abhi.mytermplanner.providers.HomeCoursesProvider;
import com.proj.abhi.mytermplanner.providers.TasksProvider;
import com.proj.abhi.mytermplanner.providers.TermsProvider;
import com.proj.abhi.mytermplanner.services.AlarmTask;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.Utils;

public class AlarmListFragment extends ListFragment implements LoaderCallbacks<Cursor> {
    //we keep this list separate in case we only want to refresh this fragment
    private CursorAdapter cursorAdapter;
    private Bundle initializer=null;
    protected CoordinatorLayout mCoordinatorLayout;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCoordinatorLayout = (CoordinatorLayout) getActivity().findViewById(R.id.coordinatorLayout);
        getListView().setDivider(null);
        getListView().setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.content, null));
        initializer=getArguments();
        switch (initializer.getInt(Constants.CURSOR_LOADER_ID)) {
            case Constants.CursorLoaderIds.ALARM_ID:
                cursorAdapter = new AlarmsCursorAdapter(getActivity(),null,0);
                setEmptyText("No "+getString(R.string.reminders));
                break;
        }
        setListAdapter(cursorAdapter);
        initLoader();
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> av, View v, int position, long l) {
                Long val=new Long(l);
                final int id=val.intValue();
                DialogInterface.OnClickListener dialogClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int button) {
                                if (button == DialogInterface.BUTTON_POSITIVE) {
                                    String cancelAlarmsWhere=" WHERE "+Constants.ID+"="+id;
                                    new AlarmTask(getActivity(),null, null).cancelAlarms(cancelAlarmsWhere);
                                    String where = Constants.ID+"="+id;
                                    getActivity().getContentResolver().delete(AlarmsProvider.CONTENT_URI,where,null);
                                    restartLoader(null);
                                    Snackbar.make(mCoordinatorLayout, R.string.deleted_alarm, Snackbar.LENGTH_LONG).show();
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

}
