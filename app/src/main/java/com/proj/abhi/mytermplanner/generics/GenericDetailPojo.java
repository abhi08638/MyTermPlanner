package com.proj.abhi.mytermplanner.generics;

import android.database.Cursor;

import com.google.gson.Gson;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.DateUtils;

import java.util.Date;

/**
 * Created by Abhi on 2/19/2018.
 */

public abstract class GenericDetailPojo {
    public String className;

    public GenericDetailPojo() {
    }

    public abstract void reset();

    public abstract void initPojo(Cursor c);

    public String getGson(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public Object initJson(String string) {
        Gson gson = new Gson();
        return gson.fromJson(string, this.getClass());
    }
}
