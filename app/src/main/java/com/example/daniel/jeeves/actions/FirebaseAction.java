package com.example.daniel.jeeves.actions;

import com.shaded.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by Daniel on 26/05/15.
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.MINIMAL_CLASS, include= JsonTypeInfo.As.PROPERTY, property="@class")
public abstract class FirebaseAction implements Serializable,IAction {
    public String getdescription() {
        return description;
    }

 //   public abstract void execute();

    public long getid() {
        return id;
    }

    public String getname() {
        return name;
    }

    public Map<String, Object> getparams() {
        return params;
    }

    public String gettype() {
        return type;
    }

    public long getxPos() {
        return xPos;
    }

    public long getyPos() {
        return yPos;
    }

    private String description;
    private long id;
    private String name;
    private Map<String,Object> params;
    private String type;
    private long xPos;
    private long yPos;

    public void execute(){

    }
}