package com.proj.abhi.mytermplanner.fragments.listFragments;

/**
 * Created by Abhi on 2/25/2018.
 */

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.cursorAdapters.EmailsCursorAdapter;
import com.proj.abhi.mytermplanner.cursorAdapters.PhonesCursorAdapter;
import com.proj.abhi.mytermplanner.generics.GenericActivity;
import com.proj.abhi.mytermplanner.generics.GenericListFragment;
import com.proj.abhi.mytermplanner.providers.EmailsProvider;
import com.proj.abhi.mytermplanner.providers.PhonesProvider;
import com.proj.abhi.mytermplanner.utils.Constants;

public class PhoneEmailListFragments extends GenericListFragment implements LoaderCallbacks<Cursor> {

    private String msg;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        switch (initializer.getInt(Constants.CURSOR_LOADER_ID)) {
            case Constants.CursorLoaderIds.PHONE_ID:
                cursorAdapter = new PhonesCursorAdapter(getActivity(), null, 0);
                setEmptyText("No " + getString(R.string.phones));
                break;
            case Constants.CursorLoaderIds.EMAIL_ID:
                cursorAdapter = new EmailsCursorAdapter(getActivity(), null, 0);
                setEmptyText("No " + getString(R.string.emails));
                break;
        }
        setListAdapter(cursorAdapter);
        initLoader();
    }

    public void openPhoneView(int id) {
        final int phoneId = id;
        String[] list = {"Home", "Work", "Cell"};
        LayoutInflater li = LayoutInflater.from(getActivity());
        final View dialogView = li.inflate(R.layout.contact_info_dialog, null);
        final TextView phoneLbl = dialogView.findViewById(R.id.inputText);
        final TextView typeLbl = dialogView.findViewById(R.id.spinnerText);
        final TextView phoneNum = dialogView.findViewById(R.id.input);
        phoneLbl.setText(R.string.phone_number);
        typeLbl.setText(R.string.phone_type);
        phoneNum.setInputType(InputType.TYPE_CLASS_PHONE);

        final Spinner type = (Spinner) dialogView.findViewById(R.id.spinner);
        final ArrayAdapter<String> adp = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, list);
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type.setAdapter(adp);

        if (phoneId > 0) {
            Cursor c = getActivity().getContentResolver().query(PhonesProvider.CONTENT_URI, null, Constants.ID + "=" + phoneId, null, null);
            c.moveToFirst();
            phoneNum.setText(c.getString(c.getColumnIndex(Constants.Professor.PHONE)));
            String typeText = c.getString(c.getColumnIndex(Constants.Professor.PHONE_TYPE));
            type.setSelection(0);
            for (int i = 0; i < type.getCount(); i++) {
                if (type.getItemAtPosition(i).equals(typeText)) {
                    type.setSelection(i);
                    break;
                }
            }
            c.close();
        }

        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        if (button == DialogInterface.BUTTON_POSITIVE) {
                            if (phoneNum.getText() != null && !phoneNum.getText().toString().trim().equals("")) {
                                ContentValues values = new ContentValues();
                                values.put(Constants.Professor.PHONE, phoneNum.getText().toString());
                                values.put(Constants.Professor.PHONE_TYPE, type.getSelectedItem().toString());
                                if (phoneId > 0) {
                                    getActivity().getContentResolver().update(PhonesProvider.CONTENT_URI, values, Constants.ID + "=" + phoneId, null);
                                } else {
                                    values.put(Constants.Ids.PROF_ID, ((GenericActivity) getActivity()).getCurrentUriId());
                                    getActivity().getContentResolver().insert(PhonesProvider.CONTENT_URI, values);
                                }
                                restartLoader();
                                Snackbar.make(mCoordinatorLayout, R.string.saved, Snackbar.LENGTH_LONG).show();
                            } else {
                                Snackbar.make(mCoordinatorLayout, R.string.error_empty_phone_num, Snackbar.LENGTH_LONG).show();
                            }
                        }
                    }
                };

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(dialogView);
        alertDialogBuilder.setTitle(R.string.phone_editor)
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        alertDialog.setCanceledOnTouchOutside(false);
    }

    public void openEmailView(int id) {
        final int emailId = id;
        String[] list = {"Personal", "Work"};
        LayoutInflater li = LayoutInflater.from(getActivity());
        final View dialogView = li.inflate(R.layout.contact_info_dialog, null);
        final TextView emailLbl = dialogView.findViewById(R.id.inputText);
        final TextView typeLbl = dialogView.findViewById(R.id.spinnerText);
        final TextView email = dialogView.findViewById(R.id.input);
        emailLbl.setText(R.string.email_address);
        typeLbl.setText(R.string.email_type);
        email.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        final Spinner type = (Spinner) dialogView.findViewById(R.id.spinner);
        final ArrayAdapter<String> adp = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, list);
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type.setAdapter(adp);

        if (emailId > 0) {
            Cursor c = getActivity().getContentResolver().query(EmailsProvider.CONTENT_URI, null, Constants.ID + "=" + emailId, null, null);
            c.moveToFirst();
            email.setText(c.getString(c.getColumnIndex(Constants.Professor.EMAIL)));
            String typeText = c.getString(c.getColumnIndex(Constants.Professor.EMAIL_TYPE));
            type.setSelection(0);
            for (int i = 0; i < type.getCount(); i++) {
                if (type.getItemAtPosition(i).equals(typeText)) {
                    type.setSelection(i);
                    break;
                }
            }
            c.close();
        }

        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        if (button == DialogInterface.BUTTON_POSITIVE) {
                            if (email.getText() != null && !email.getText().toString().trim().equals("")) {
                                ContentValues values = new ContentValues();
                                values.put(Constants.Professor.EMAIL, email.getText().toString());
                                values.put(Constants.Professor.EMAIL_TYPE, type.getSelectedItem().toString());
                                if (emailId > 0) {
                                    getActivity().getContentResolver().update(EmailsProvider.CONTENT_URI, values, Constants.ID + "=" + emailId, null);
                                } else {
                                    values.put(Constants.Ids.PROF_ID, ((GenericActivity) getActivity()).getCurrentUriId());
                                    getActivity().getContentResolver().insert(EmailsProvider.CONTENT_URI, values);
                                }
                                restartLoader();
                                Snackbar.make(mCoordinatorLayout, R.string.saved, Snackbar.LENGTH_LONG).show();
                            } else {
                                Snackbar.make(mCoordinatorLayout, R.string.error_empty_email, Snackbar.LENGTH_LONG).show();
                            }
                        }
                    }
                };

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(dialogView);
        alertDialogBuilder.setTitle(R.string.email_editor)
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        alertDialog.setCanceledOnTouchOutside(false);
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        String[] cols = null;
        String where = null;
        String order = null;
        if (bundle != null) {
            Uri uri = Uri.parse((String) bundle.get(Constants.CONTENT_URI));
            if (uri.equals(PhonesProvider.CONTENT_URI) || uri.equals(EmailsProvider.CONTENT_URI)) {
                where = Constants.Ids.PROF_ID + "=" + ((GenericActivity) getActivity()).getCurrentUriId();
            }
            return new CursorLoader(getActivity(), uri,
                    cols, where, null, order);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        cursorAdapter.swapCursor(cursor);
        msg = "";
        if (loader.getId() == Constants.CursorLoaderIds.PHONE_ID) {
            if (cursor.getCount() >= 1) {
                while (cursor.moveToNext()) {
                    msg += cursor.getString(cursor.getColumnIndex(Constants.Professor.PHONE_TYPE)) + " Phone: " +
                            cursor.getString(cursor.getColumnIndex(Constants.Professor.PHONE)) + "\n";
                }
            }
        } else if (loader.getId() == Constants.CursorLoaderIds.EMAIL_ID) {
            if (cursor.getCount() >= 1) {
                while (cursor.moveToNext()) {
                    msg += cursor.getString(cursor.getColumnIndex(Constants.Professor.EMAIL_TYPE)) + " Email: " +
                            cursor.getString(cursor.getColumnIndex(Constants.Professor.EMAIL)) + "\n";
                }
            }
        }
    }

    @Override
    public void onListItemClick(ListView parent, View view, int position, long id) {
        if (id > 0) {
            Long l = new Long(id);
            switch (initializer.getInt(Constants.CURSOR_LOADER_ID)) {
                case Constants.CursorLoaderIds.PHONE_ID:
                    openPhoneView(l.intValue());
                    break;
                case Constants.CursorLoaderIds.EMAIL_ID:
                    openEmailView(l.intValue());
                    break;
                default:
                    break;
            }
        }
    }
}
