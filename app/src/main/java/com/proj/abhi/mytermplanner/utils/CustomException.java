package com.proj.abhi.mytermplanner.utils;

/**
 * Created by Abhi on 2/9/2018.
 */

public class CustomException extends  Exception{
    public CustomException(String message){
        this.message=message;
    }

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
