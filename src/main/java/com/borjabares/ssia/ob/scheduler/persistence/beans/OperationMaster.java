package com.borjabares.ssia.ob.scheduler.persistence.beans;

import java.io.Serializable;

public class OperationMaster implements Serializable {

    private static final long serialVersionUID = 7113700912350766771L;
    private static final int MINUTES_IN_SLOT = 30;
    private static final long MILLISECONDS_IN_MINUTE = 60L * 1000;

    private long id;
    private String name;
    private long duration;
    private int slots;

    public OperationMaster() {
    }

    public OperationMaster(long id, String name, int slots) {
        this();
        this.id = id;
        this.name = name;
        this.slots = slots;
        this.duration = slots * MINUTES_IN_SLOT * MILLISECONDS_IN_MINUTE;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getSlots() {
        return slots;
    }

    public void setSlots(int slots) {
        this.slots = slots;
    }
}
