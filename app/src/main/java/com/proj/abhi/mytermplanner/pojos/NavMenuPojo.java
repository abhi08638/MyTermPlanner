package com.proj.abhi.mytermplanner.pojos;

import android.database.Cursor;

import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.DateUtils;

import java.util.Date;

/**
 * Created by Abhi on 2/19/2018.
 */

public class NavMenuPojo {
    private int menuGroup;
    private String groupName;
    private String groupHeaderName;
    private String itemName;

    public NavMenuPojo(int menuGroup, String groupName, String groupHeaderName, String itemName) {
        this.menuGroup = menuGroup;
        this.groupName = groupName;
        this.groupHeaderName = groupHeaderName;
        this.itemName = itemName;
    }

    public int getMenuGroup() {
        return menuGroup;
    }

    public void setMenuGroup(int menuGroup) {
        this.menuGroup = menuGroup;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupHeaderName() {
        return groupHeaderName;
    }

    public void setGroupHeaderName(String groupHeaderName) {
        this.groupHeaderName = groupHeaderName;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
}
