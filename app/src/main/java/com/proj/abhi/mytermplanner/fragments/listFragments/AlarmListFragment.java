package com.proj.abhi.mytermplanner.fragments.listFragments;

/**
 * Created by Abhi on 2/25/2018.
 */

import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.cursorAdapters.AlarmsCursorAdapter;
import com.proj.abhi.mytermplanner.generics.GenericListFragment;
import com.proj.abhi.mytermplanner.providers.AlarmsProvider;
import com.proj.abhi.mytermplanner.utils.Constants;

public class AlarmListFragment extends GenericListFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initializer.putString(Constants.CONTENT_URI, AlarmsProvider.CONTENT_URI.toString());
        initializer.putString(Constants.ID, Constants.ID);

        cursorAdapter = new AlarmsCursorAdapter(getActivity(), null, 0);
        setEmptyText("No " + getString(R.string.reminders));

        setListAdapter(cursorAdapter);
        initLoader();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        String[] cols = null;
        String where = null;
        String order = null;
        if (bundle != null) {
            Uri uri = Uri.parse((String) bundle.get(Constants.CONTENT_URI));
            if (uri.equals(AlarmsProvider.CONTENT_URI)) {
                where = initializer.getString(Constants.Sql.WHERE);
            }
            return new CursorLoader(getActivity(), uri,
                    cols, where, null, order);
        }
        return null;
    }

    @Override
    public void onResume(){
        super.onResume();
        restartLoader();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        restartLoader();
    }
}
