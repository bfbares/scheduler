package com.borjabares.ssia.ob.scheduler.persistence.beans;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public interface Event {

    long getId();

    String getTitle();

    String getStart();

    String getEnd();

    int getResourceId();

    int getType();

    int getSlots();

    void setSlots(int slots);

    default void calculateSlots() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;

        ZonedDateTime startDate = ZonedDateTime.parse(getStart(), dateTimeFormatter);
        ZonedDateTime endDate = ZonedDateTime.parse(getEnd(), dateTimeFormatter);

        long differenceSeconds = endDate.toEpochSecond() - startDate.toEpochSecond();

        setSlots((int) (differenceSeconds / (60 * 30)));
    }

}

