package com.borjabares.ssia.ob.scheduler.scheduler;

import com.borjabares.ssia.ob.scheduler.persistence.beans.Event;
import com.borjabares.ssia.ob.scheduler.persistence.beans.OperationEvent;
import com.borjabares.ssia.ob.scheduler.persistence.beans.OperationMaster;

import java.util.*;

import static com.borjabares.ssia.ob.scheduler.scheduler.Scheduler.SLOTS_IN_A_DAY;
import static com.borjabares.ssia.ob.scheduler.scheduler.Scheduler.random;

/**
 * Clase de representación de un cromosoma
 */
public class Chromosome {
    private static final int END_SLOT_FACTOR = 6;
    private static final int OVERFLOW_PENALTY_FACTOR = 1;
    private static final int COLLISION_PENALTY_FACTOR = 90;
    private static final int UNIT_CHANGE_PENALTY_FACTOR = 6;
    private static final int EMPTY_SLOTS_IN_BETWEEN_FACTOR = 1;
    private static final int EMPTY_SLOTS_AT_BEGINNING = 12;

    private Event[] chromosome;
    private Map<OperationEvent, Integer> operationSlots;
    private int unitsSize;
    private int chromosomeSize;
    private int quality;
    // Penalizaciones para la calidad de un cromosoma, se almacenan para poder ajustar mejor
    // los pesos de las penalizaciones
    private int endSlot;
    private int overflowPenalty;
    private int collisionPenalty;
    private int unitChangePenalty;
    private int emptySlotsInBetweenPenalty;
    private int emptySlotsAtBeginning;

    /**
     * @param chromosome     Cromosoma ya creado
     * @param operationSlots Dónde están ubicadas las operaciones
     * @param unitsSize      Tamaño de las unidades (Empleados)
     * @param chromosomeSize Tamaño del cromosoma
     */
    public Chromosome(Event[] chromosome, Map<OperationEvent, Integer> operationSlots, int unitsSize, int chromosomeSize) {
        this.chromosome = chromosome;
        this.operationSlots = operationSlots;
        this.unitsSize = unitsSize;
        this.chromosomeSize = chromosomeSize;

        calculateQuality();
    }

    /**
     * Constructor que genera un cromosoma aleatorio a partir de los maestros de operaciones
     *
     * @param reservedChromosome El cromosoma vacío con los huecos reservados
     * @param operationMasters   Las operaciones maestras que se quieren planificar
     * @param unitsSize          Número de empleados
     */
    public Chromosome(Event[] reservedChromosome, List<OperationMaster> operationMasters, int unitsSize) {
        this.unitsSize = unitsSize;
        this.chromosome = new Event[reservedChromosome.length];
        this.operationSlots = new HashMap<>();
        this.chromosomeSize = reservedChromosome.length;

        System.arraycopy(reservedChromosome, 0, chromosome, 0, chromosomeSize);

        for (OperationMaster operationMaster : operationMasters) {
            int randomSlot;
            OperationEvent operationEvent = new OperationEvent(operationMaster);

            // Buscamos una posición aleatoria en la que entre la operación
            do {
                randomSlot = random.nextInt(chromosomeSize);
            } while (!canPutOperationInSlot(operationEvent, randomSlot));

            // Almacenamos la posición de la operación en un mapa para facilitar cálculos posteriores
            operationSlots.put(operationEvent, randomSlot);

            // Guardamos la operación en los slots que le corresponden del cromosoma
            for (int slotOffset = 0; slotOffset < operationEvent.getSlots(); slotOffset++) {
                chromosome[randomSlot + slotOffset] = operationEvent;
            }
        }

        calculateQuality();
    }

    /**
     * Comprueba si una operación puede colocarse en ese slot del cromosoma
     *
     * @param operationEvent La operación a ubicar
     * @param slot           Slot en donde se quiere ubicar
     * @return Si se puede ubicar en esas posiciones
     */
    private boolean canPutOperationInSlot(OperationEvent operationEvent, Integer slot) {
        // No se puede colocar si la operación acaba fuera del cromosoma
        if (slot + operationEvent.getSlots() > chromosomeSize) {
            return false;
        }

        for (int slotOffset = 0; slotOffset < operationEvent.getSlots(); slotOffset++) {
            Event slotEvent = chromosome[slot + slotOffset];
            // No se puede colocar si cualquiera de sus slots está ocupado por una operación
            // que no sea ella misma
            if (slotEvent != null && (!(slotEvent instanceof OperationEvent) || slotEvent.getId() != operationEvent.getId())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Genera una mutación del cromosoma
     *
     * @param changeUnitProbability probabilidad de que haya un cambio de unidad
     * @return La mutación del cromosoma
     */
    public Chromosome mutate(int changeUnitProbability) {
        int newSlot;

        // Selecionamos una operación al azar y guardamos su posición actual
        int operation = random.nextInt(operationSlots.size());
        OperationEvent operationEventToMove = new ArrayList<>(operationSlots.keySet()).get(operation);
        int actualSlot = operationSlots.get(operationEventToMove);

        // Variable aleatoria para realizar simplemente un cambio de unidad
        int changeUnit = random.nextInt(10);

        if (changeUnit <= changeUnitProbability) {
            // Se realiza el cambio de unidad sin cambiar la hora
            Set<Integer> unitsTried = new HashSet<>();
            int actualUnit = actualSlot / SLOTS_IN_A_DAY;
            unitsTried.add(actualUnit);

            int slotInUnit = actualSlot % SLOTS_IN_A_DAY;

            // Hasta que se pueda colocar la operación en una nueva unidad o ya no queden unidades para mover
            do {
                int newUnit = random.nextInt(unitsSize - 1);
                // Evitar que vuelva al mismo sitio
                if (newUnit >= actualUnit) {
                    newUnit++;
                }
                newSlot = slotInUnit + (newUnit * SLOTS_IN_A_DAY);
                unitsTried.add(newUnit);
            } while (!canPutOperationInSlot(operationEventToMove, newSlot) && unitsTried.size() < unitsSize);

            // Sino se puede cambiar de unidad se devuelve el mismo cromosoma
            if (!canPutOperationInSlot(operationEventToMove, newSlot)) {
                return this;
            }
        } else {
            // Mover la operación a un slot nuevo dentro del cromosoma
            do {
                newSlot = random.nextInt(chromosomeSize);
            } while (!canPutOperationInSlot(operationEventToMove, newSlot));
        }

        return moveOperation(operationEventToMove, actualSlot, newSlot);
    }

    /**
     * Mueve una operación de un slot a otro nuevo
     *
     * @param operationEventToMove La operación que se quiere mover
     * @param actualSlot           Slot en el que está actualmente la operación
     * @param newSlot              Slot al que se quiere mover
     * @return El cromosoma con la operación desplazada
     */
    private Chromosome moveOperation(OperationEvent operationEventToMove, int actualSlot, int newSlot) {
        Event[] newChromosome = new Event[chromosomeSize];
        int slots = operationEventToMove.getSlots();

        System.arraycopy(chromosome, 0, newChromosome, 0, chromosomeSize);

        for (int slotOffset = 0; slotOffset < slots; slotOffset++) {
            newChromosome[actualSlot + slotOffset] = null;
        }

        for (int slotOffset = 0; slotOffset < slots; slotOffset++) {
            newChromosome[newSlot + slotOffset] = operationEventToMove;
        }

        Map<OperationEvent, Integer> newOperationSlots = new HashMap<>(operationSlots);

        newOperationSlots.put(operationEventToMove, newSlot);

        return new Chromosome(newChromosome, newOperationSlots, unitsSize, chromosomeSize);
    }

    /**
     * Calcula la calidad del cromosoma.
     */
    private void calculateQuality() {
        // Penalizaciones
        endSlot = 0;
        overflowPenalty = 0;
        collisionPenalty = 0;
        unitChangePenalty = 0;
        emptySlotsInBetweenPenalty = 0;
        emptySlotsAtBeginning = 0;

        // Array que superpone todos los empleados en uno marcando como true los huecos
        // que están ocupados con operaciones planificadas para este cromosoma
        boolean[] operationCollisionArray = new boolean[SLOTS_IN_A_DAY];
        // Array que superpone todos los empleados en un solo array guardando el empleado
        // de esta forma se puede calcular cuantos cambios de empleado realiza el vehículo
        int[] operationUnits = new int[SLOTS_IN_A_DAY];

        // Rellenar los arrays
        for (Map.Entry<OperationEvent, Integer> operationSlot : operationSlots.entrySet()) {
            OperationEvent operation = operationSlot.getKey();
            Integer operationSlotValue = operationSlot.getValue();
            int operationStartSlot = operationSlotValue % SLOTS_IN_A_DAY;
            int operationEndSlot = operationStartSlot + operation.getSlots();

            if (operationEndSlot > endSlot) {
                // Almacenamos el último slot de la última operación para saber cuándo
                // se acaban de realizar operaciones sobre un vehículo
                endSlot = operationEndSlot;
            }

            if (operationEndSlot > SLOTS_IN_A_DAY) {
                // Una operación acaba al día siguiente, esta situación se da cuando una
                // operación empieza al final de un empleado y acaba en el siguiente
                overflowPenalty++;
            }

            for (int slotOffset = 0; slotOffset < operation.getSlots(); slotOffset++) {
                if (operationStartSlot + slotOffset < SLOTS_IN_A_DAY) {
                    if (!operationCollisionArray[operationStartSlot + slotOffset]) {
                        operationCollisionArray[operationStartSlot + slotOffset] = true;
                    } else {
                        // Hay dos operaciones planificadas a la misma hora
                        // con dos empleados distintos
                        collisionPenalty++;
                    }
                    operationUnits[operationStartSlot + slotOffset] = (operationSlotValue / SLOTS_IN_A_DAY) + 1;
                }
            }

        }

        int lastUnit = 0;
        boolean firstOperation = false;
        for (int i = 0; i < operationUnits.length; i++) {
            int operationUnit = operationUnits[i];
            boolean occupied = operationCollisionArray[i];

            if (occupied && !firstOperation) {
                firstOperation = true;
            }

            if (operationUnit != 0) {
                if (lastUnit != 0 && lastUnit != operationUnit) {
                    // Hay un cambio de empleado
                    unitChangePenalty++;
                }

                lastUnit = operationUnit;
            }

            if (i < endSlot && !occupied) {
                // Hay un hueco vacío entre dos operaciones, la penalización va en aumento a medida
                // que se avanza. Esto es así para favorecer que si una operación tiene un hueco antes
                // y otro despues se mueva al hueco que tiene antes dejando más espacio después para otras operaciones
                emptySlotsInBetweenPenalty += (endSlot - i + 1);
                if (!firstOperation) {
                    // Huecos vacíos antes de que se comiencen las operaciones
                    emptySlotsAtBeginning++;
                }
            }
        }

        quality = (endSlot * END_SLOT_FACTOR)
                + (overflowPenalty * OVERFLOW_PENALTY_FACTOR)
                + (collisionPenalty * COLLISION_PENALTY_FACTOR)
                + (unitChangePenalty * UNIT_CHANGE_PENALTY_FACTOR)
                + (emptySlotsInBetweenPenalty * EMPTY_SLOTS_IN_BETWEEN_FACTOR)
                + (emptySlotsAtBeginning * EMPTY_SLOTS_AT_BEGINNING);
    }

    public int getQuality() {
        return quality;
    }

    public Map<OperationEvent, Integer> getOperationSlots() {
        return operationSlots;
    }

    @Override
    public String toString() {
        return "Chromosome{" +
                "endSlot=" + endSlot +
                ", overflowPenalty=" + overflowPenalty +
                ", collisionPenalty=" + collisionPenalty +
                ", unitChangePenalty=" + unitChangePenalty +
                ", emptySlotsInBetweenPenalty=" + emptySlotsInBetweenPenalty +
                ", emptySlotsAtBeginning=" + emptySlotsAtBeginning +
                ", quality=" + quality +
                '}';
    }
}
