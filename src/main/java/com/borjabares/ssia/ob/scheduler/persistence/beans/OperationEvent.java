package com.borjabares.ssia.ob.scheduler.persistence.beans;

public class OperationEvent implements Event {

    private long id;
    private String title;
    private String start;
    private String end;
    private int resourceId;
    private int type;
    private int slots;
    private boolean editable;

    public OperationEvent() {
        this.type = 2;
        this.editable = false;
    }

    public OperationEvent(OperationMaster master) {
        this();
        this.id = master.getId();
        this.title = master.getName();
        this.slots = master.getSlots();
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getSlots() {
        return this.slots;
    }

    public void setSlots(int slots) {
        this.slots = slots;
    }

    public boolean isEditable() {
        return editable;
    }
}
