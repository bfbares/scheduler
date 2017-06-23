package com.borjabares.ssia.ob.scheduler.scheduler;

import com.borjabares.ssia.ob.scheduler.persistence.beans.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Scheduler {
    public static final Random random = new Random();
    public static final int SLOTS_IN_A_DAY = 24 * 2;
    private static final int SECONDS_IN_HALF_HOUR = 60 * 30;

    private static final int MINUTES_TO_END_SCHEDULER = 1;
    private static final int ITERATIONS_TO_END_SCHEDULER = 350000;
    private static final int WATER_LEVEL_SPEED = 3;
    private static final int PROBABILITY_OF_WORST = 15;

    // Mapa de equivalencia entre el id del mecánico y su orden dentro del cromosoma
    private static Map<Integer, Integer> orderToUnitId;
    // Mapa de equivalencia inversa a orderToUnitID
    private static Map<Integer, Integer> unitIdToOrder;
    private static int unitsSize;
    private static ZonedDateTime firstDayHour;

    /**
     * El algoritmo encargado de planificar
     *
     * @param employees  Lista con los empleados del calendario
     * @param reserved   Lista con los huecos reservados
     * @param operations Lista de operaciones que se quieren planificar
     * @return La lista de las operaciones planificadas
     */
    public static List<OperationEvent> schedule(List<Unit> employees, List<ReservedEvent> reserved, List<OperationMaster> operations){

        Event[] reservedChromosome = createEmptyChromosome(employees, reserved);
        Chromosome best = new Chromosome(reservedChromosome, operations, unitsSize);

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime oneMinuteFromNow = now.plusMinutes(MINUTES_TO_END_SCHEDULER);

        int waterLevel = (best.getQuality() + (best.getQuality() / 6));
        int iterationsToEnd = 0;

        do {
            iterationsToEnd++;
            now = ZonedDateTime.now();

            Chromosome mutation = best.mutate((iterationsToEnd * 8) / ITERATIONS_TO_END_SCHEDULER);

            int newRandom = random.nextInt(100);

            // Con cierta probabilidad cogemos una solución peor que la actual pero mejor que la que marca waterLevel
            if (mutation.getQuality() < best.getQuality() || (newRandom <= PROBABILITY_OF_WORST && mutation.getQuality() < waterLevel)) {
                best = mutation;
                iterationsToEnd = 0;
            }

            waterLevel -= WATER_LEVEL_SPEED;

        } while (now.isBefore(oneMinuteFromNow) && iterationsToEnd < ITERATIONS_TO_END_SCHEDULER);

        return toOperationEventList(best.getOperationSlots(), true);
    }

    /**
     * Crea un array con los espacios vacios y reservados
     *
     * @param employees La lista de unidades
     * @param reserved  La lista de espacios reservados
     * @return El array con los espacios vacios y reservados
     */
    private static Event[] createEmptyChromosome(List<Unit> employees, List<ReservedEvent> reserved) {
        int chromosomeSize = (24 * 60 / 30) * employees.size();
        Event[] chromosome = new Event[chromosomeSize];
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;

        createUnitEquivalences(employees);

        for (ReservedEvent event : reserved) {
            ZonedDateTime startTime = ZonedDateTime.parse(event.getStart(), dateTimeFormatter).plusHours(1);

            if (firstDayHour == null) {
                firstDayHour = startTime.withHour(0).withMinute(0);
            }

            int slot = calculateFirstSlot(startTime, event.getResourceId());

            for (int i = 0; i < event.getSlots(); i++) {
                chromosome[slot + i] = event;
            }
        }

        return chromosome;
    }

    /**
     * Calcula el slot en el array del cromosoma dada una fecha y la unidad
     *
     * @param startTime La hora de inicio
     * @param unitId    El ID de la unidad
     * @return El slot inicial
     */
    private static int calculateFirstSlot(ZonedDateTime startTime, int unitId) {
        return (int) (((startTime.toEpochSecond() - firstDayHour.toEpochSecond()) / SECONDS_IN_HALF_HOUR) + (SLOTS_IN_A_DAY * unitIdToOrder.get(unitId)));
    }

    /**
     * Crea una serie de mapas con equivalencias del ID de la unidad y el slot que realmente ocupa
     *
     * @param units La lista de unidades
     */
    private static void createUnitEquivalences(List<Unit> units) {
        orderToUnitId = new HashMap<>();
        unitIdToOrder = new HashMap<>();

        unitsSize = units.size();

        for (int i = 0; i < unitsSize; i++) {
            orderToUnitId.put(i, units.get(i).getId());
            unitIdToOrder.put(units.get(i).getId(), i);
        }
    }

    /**
     * Transforma las operaciones de un cromosoma a una lista de OperationEvent para el calendario w2ui
     *
     * @param operationSlots El mapa de las operaciones con su slot de inicio
     * @param ended          Si ha finalizado la planificación para poner las operaciones de otro color
     * @return La lista para el calendario
     */
    private static List<OperationEvent> toOperationEventList(Map<OperationEvent, Integer> operationSlots, boolean ended) {
        List<OperationEvent> operationEventList = new ArrayList<>();

        for (Map.Entry<OperationEvent, Integer> operationEventSlotEntry : operationSlots.entrySet()) {
            int slot = operationEventSlotEntry.getValue();
            OperationEvent operationEvent = operationEventSlotEntry.getKey();

            int unitSlot = slot % SLOTS_IN_A_DAY;
            int unit = slot / SLOTS_IN_A_DAY;
            operationEvent.setResourceId(orderToUnitId.get(unit));

            ZonedDateTime eventStartTime = firstDayHour.plusMinutes(30 * unitSlot).minusHours(1);
            ZonedDateTime eventEndTime = eventStartTime.plusMinutes(30 * operationEvent.getSlots());

            operationEvent.setStart(eventStartTime.format(DateTimeFormatter.ISO_DATE_TIME));
            operationEvent.setEnd(eventEndTime.format(DateTimeFormatter.ISO_DATE_TIME));

            if (ended) {
                operationEvent.setType(3);
            }

            operationEventList.add(operationEvent);
        }

        return operationEventList;
    }

}
