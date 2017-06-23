package com.borjabares.ssia.ob.scheduler.persistence.beans;

public class ReservedEvent implements Event {
    private long id;
    private String title;
    private String start;
    private String end;
    private int resourceId;
    private int type;
    private int slots;

    public ReservedEvent() {
        super();
        this.title = "Hueco reservado";
        this.type = 1;
    }

    public ReservedEvent(Builder builder) {
        this();
        this.id = builder.id;
        this.start = builder.start;
        this.end = builder.end;
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

    public String getEnd() {
        return end;
    }

    public int getResourceId() {
        return resourceId;
    }

    public int getType() {
        return type;
    }

    public int getSlots() {
        return slots;
    }

    public void setSlots(int slots) {
        this.slots = slots;
    }

    public static class Builder {
        private long id;
        private String start;
        private String end;

        public Builder(long id) {
            this.id = id;
        }

        public Builder start(String start) {
            this.start = start;
            return this;
        }

        public Builder end(String end) {
            this.end = end;
            return this;
        }

        public ReservedEvent build() {
            return new ReservedEvent(this);
        }
    }
}
