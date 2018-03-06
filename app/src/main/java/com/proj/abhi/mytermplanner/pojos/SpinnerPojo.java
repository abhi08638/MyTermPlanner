package com.proj.abhi.mytermplanner.pojos;

/**
 * Created by Abhi on 2/19/2018.
 */

public class SpinnerPojo {
    private int id;

    private String value;

    public SpinnerPojo(int id, String value) {
        this.id = id;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString(){
        return this.value;
    }

}
