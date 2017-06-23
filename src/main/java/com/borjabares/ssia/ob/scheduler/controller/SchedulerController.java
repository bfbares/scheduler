package com.borjabares.ssia.ob.scheduler.controller;

import com.borjabares.ssia.ob.scheduler.controller.dto.InitResponse;
import com.borjabares.ssia.ob.scheduler.controller.dto.ScheduleRequest;
import com.borjabares.ssia.ob.scheduler.persistence.beans.OperationEvent;
import com.borjabares.ssia.ob.scheduler.persistence.beans.OperationMaster;
import com.borjabares.ssia.ob.scheduler.persistence.beans.ReservedEvent;
import com.borjabares.ssia.ob.scheduler.persistence.beans.Unit;
import com.borjabares.ssia.ob.scheduler.scheduler.Scheduler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
public class SchedulerController {

    private final List<Unit> units;
    private final Map<Integer, OperationMaster> operations;
    private final Map<Long, ReservedEvent> events;
    private final SimpMessagingTemplate template;

    public SchedulerController(SimpMessagingTemplate template) {
        this.template = template;

        units = new ArrayList<>();
        units.add(new Unit(1, "Empleado 1"));
        units.add(new Unit(2, "Empleado 2"));
        units.add(new Unit(3, "Empleado 3"));
        units.add(new Unit(4, "Empleado 4"));
        units.add(new Unit(5, "Empleado 5"));
        units.add(new Unit(6, "Empleado 6"));

        operations = new HashMap<>();
        operations.put(1, new OperationMaster(1, "Sustitucion de ruedas", 5));
        operations.put(2, new OperationMaster(2, "Sustitucion de aceite", 2));
        operations.put(3, new OperationMaster(3, "Pintar puerta", 3));
        operations.put(4, new OperationMaster(4, "Sustitucion de limpiaparabrisas", 1));
        operations.put(5, new OperationMaster(5, "Sustitucion de llantas", 4));
        operations.put(6, new OperationMaster(6, "Revision motor", 3));
        operations.put(7, new OperationMaster(7, "Revision electrica", 6));
        operations.put(8, new OperationMaster(8, "Sustitucion caja de cambios", 7));
        operations.put(9, new OperationMaster(9, "Sustitucion de pastillas de freno", 2));
        operations.put(10, new OperationMaster(10, "Sustitucion escape", 3));

        events = new HashMap<>();
    }

    @MessageMapping("/add")
    @SendTo("/response/add")
    public List<ReservedEvent> add(ReservedEvent event) throws Exception {
        event.calculateSlots();
        events.put(event.getId(), event);
        List<ReservedEvent> result = new ArrayList<>();
        result.add(event);
        return result;
    }

    @MessageMapping("/schedule")
    @SendTo("/response/add")
    public List<OperationEvent> schedule(ScheduleRequest request) throws Exception {
        List<OperationMaster> operationsToSchedule = request.getOperations().stream().map(operations::get).collect(Collectors.toList());
        return Scheduler.schedule(units, new ArrayList<>(events.values()), operationsToSchedule);
    }

    @MessageMapping("/clear")
    @SendTo("/response/clear")
    public String clear() {
        events.clear();
        return "clear";
    }

    // 12 hours
    @Scheduled(fixedDelay = 12*60*60*1000)
    public void scheduledClear() {
        events.clear();
        template.convertAndSend("/response/clear", "clear");
    }

    @RequestMapping(value = "/init", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(OK)
    @ResponseBody
    public InitResponse init() {
        return new InitResponse(units, events, operations);
    }
}
