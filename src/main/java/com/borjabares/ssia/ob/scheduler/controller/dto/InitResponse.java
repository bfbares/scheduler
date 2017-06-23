package com.borjabares.ssia.ob.scheduler.controller.dto;

import com.borjabares.ssia.ob.scheduler.persistence.beans.OperationMaster;
import com.borjabares.ssia.ob.scheduler.persistence.beans.ReservedEvent;
import com.borjabares.ssia.ob.scheduler.persistence.beans.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Información de respuesta que se devuelve a una petición de inicialización del calendario
 */
public class InitResponse {
    private List<Unit> units;
    private List<ReservedEvent> events;
    private List<OperationMaster> operations;

    public InitResponse() {
    }

    /**
     * @param units      Unidades del calendario
     * @param events     Eventos reservados del calendario
     * @param operations Maestro de operaciones
     */
    public InitResponse(List<Unit> units, Map<Long, ReservedEvent> events, Map<Integer, OperationMaster> operations) {
        this();
        this.units = units;
        this.events = new ArrayList<>(events.values());
        this.operations = new ArrayList<>(operations.values());
    }

    public List<Unit> getUnits() {
        return units;
    }

    public List<ReservedEvent> getEvents() {
        return events;
    }

    public List<OperationMaster> getOperations() {
        return operations;
    }
}
