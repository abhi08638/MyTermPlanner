package com.proj.abhi.mytermplanner.pojos;

/**
 * Created by Abhi on 2/19/2018.
 */

public class ProfPojo {
    private int id;
    private String fullName;

    public ProfPojo(int id, String fullName) {
        this.id = id;
        this.fullName = fullName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public String toString(){
        return this.fullName;
    }

}
