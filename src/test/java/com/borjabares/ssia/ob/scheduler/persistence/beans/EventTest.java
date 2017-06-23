package com.borjabares.ssia.ob.scheduler.persistence.beans;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EventTest {

    @Test
    public void calculateSlots() {
        Event event = new ReservedEvent.Builder(1).start("2015-01-01T00:00:00.000Z").end("2015-01-01T00:30:00.000Z").build();
        event.calculateSlots();
        assertEquals(event.getSlots(), 1);

        event = new ReservedEvent.Builder(2).start("2015-01-01T01:00:00.000Z").end("2015-01-01T03:30:00.000Z").build();
        event.calculateSlots();
        assertEquals(event.getSlots(), 5);
    }

}
