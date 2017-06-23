package com.borjabares.ssia.ob.scheduler.persistence.beans;

public class Unit {
    private int id;
    private String title;

    public Unit() {
    }

    public Unit(int id, String title) {
        this();
        this.id = id;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}