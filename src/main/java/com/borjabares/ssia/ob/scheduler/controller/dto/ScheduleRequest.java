package com.borjabares.ssia.ob.scheduler.controller.dto;

import java.util.List;

public class ScheduleRequest {
    public String day;
    public List<Integer> operations;

    public ScheduleRequest() {
    }

    public String getDay() {
        return day;
    }

    public List<Integer> getOperations() {
        return operations;
    }
}
