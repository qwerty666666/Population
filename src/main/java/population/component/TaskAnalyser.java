package population.component;


import population.model.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;


/**
 * Try predict task result, i.e find states whose values will be greatest in system
 * when task is stabilized. States counts in tasks must be non-negative.
 * The algorithm is based on finding a monotonicity of states dynamics. If by the end
 * of the calculation the monotonicity is not strictly defined then the result will be
 * determined at the end of calculation. Sometimes computation errors affect the system
 * dynamic, so it doesn't provide precise result, but it guarantee that calculated result
 * is based on task calculation and actual result is subset of calculated result.
 */
public class TaskAnalyser {
    /* used to determine the same states in case of computation error */
    private final static double LAST_STEPS_PERCENTAGE = 0.2;
    private final static double EPS = 0.01;

    /** task for analyse */
    private Task task;
    /** state's count after modeling [step][stateInd] */
    private double[][] states;
    /** state's count after modeling higher accuracy[step][stateInd] */
    private LinkedList<BigDecimal[]> bdStates = new LinkedList<>();
    /** is calculating with higher accuracy */
    private boolean higherAccuracy;
    /** steps count which was modeled */
    public int calculatedStepsCnt;
    /** states vertexes in graph */
    private Set<StateVertex> statesSet;
    /** transitions vertexes in graph */
    private Set<TransitionVertex> transitionsSet;
    private State externalState;
    private StateVertex externalVertex;
    /** state vertexes whose stable was calculated */
    private Set<StateVertex> calculatedStateVertexes;
    /** transition vertexes whose stable was calculated */
    private Set<TransitionVertex> calculatedTransitionsVertexes;
    /** states for which calculateParametricPortrait predicted value */
    private List<State> analysedStatesList;
    /** the reason of calculateParametricPortrait was interrupted */
    private CalculationFinishedReason calculationFinishedReason = null;
    private CalculationMode calculationMode = CalculationMode.SIMPLE;
    private double stablePrecision;


    /**
     * the reason of calculateParametricPortrait was interrupted
     */
    public enum CalculationFinishedReason {
        /** can predict alive states  from analysedStatesList by their trends*/
        ALL_STATES_TRENDS_HAVE_BEEN_IDENTIFIED,
        /** task is stable and can't be changed */
        TASK_STABLE,
        /** task cyclic */
        TASK_CYCLIC
    }

    public CalculationFinishedReason getCalculationFinishedReason() {
        return calculationFinishedReason;
    }

    public enum CalculationMode {
        /** can get dominants only in case of task stable or task cyclic */
        SIMPLE,
        /** try predict dominants by states trends */
        STATES_TRENDS
    }

    public CalculationMode getCalculationMode() {
        return calculationMode;
    }

    public void setCalculationMode(CalculationMode calculationMode) {
        this.calculationMode = calculationMode;
    }

    public TaskAnalyser(Task task){
        analysedStatesList = new ArrayList<>();
        calculatedStateVertexes = new HashSet<>();
        calculatedTransitionsVertexes = new HashSet<>();
        calculationFinishedReason = null;
        higherAccuracy = task.isHigherAccuracy();
        this.task = task;

        clearCalculatedData();
    }

    public List<State> getAnalysedStatesList() {
        return analysedStatesList;
    }

    /**
     * 
     * @param analysedStatesList list of states which can be in the result
     */
    public void setAnalysedStatesList(List<State> analysedStatesList) {
        this.analysedStatesList = analysedStatesList;
    }

    public Task getTask() {
        return task;
    }
    

    /**
     * clear all calculated data
     * (graph base remain)
     */
    public void clearCalculatedData() {
        states = null;
        bdStates = new LinkedList<>();
        calculatedStateVertexes.clear();
        calculatedTransitionsVertexes.clear();
    }
    

    /**
     * set pointer to states count calculated data
     * @param states states
     */
    public void setCalculatedStates(double[][] states) {
        this.states = states;
        updateStates(0);
    }


    /**
     *
     * @return max delay in transitionSet
     */
    private int getMaxDelay() {
        return transitionsSet.stream()
                .map(x -> Math.max(x.getTransition().getSourceDelay(), x.getTransition().getOperandDelay()))
                //.map(x -> x.getTransition().getSourceDelay() + x.getTransition().getOperandDelay())
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }


    public void setStablePrecision(int precision) {
        this.stablePrecision = Math.pow(10., precision);
    }
    

    /**
     * is states count was changes in last steps
     * @param state state
     * @return true if states count was changed on last steps (steps count equal max state delay + 1),
     * false otherwise. If no information about state return false
     */
    public boolean isStateStable(State state) {
        int ind = task.getStates().indexOf(state);
        if (ind == -1)
            return false;

        int maxDelay = getMaxDelay();

        if (calculatedStepsCnt <= maxDelay)
            return false;

        if (higherAccuracy) {
            BigDecimal val = bdStates.getLast()[ind];
            BigDecimal precision = new BigDecimal(this.stablePrecision);
            for (BigDecimal[] bd: bdStates) {
                if (bd[ind].subtract(val).abs().compareTo(precision) > 0)
                    return false;
            }
        }
        else {
            double val = states[calculatedStepsCnt - 1][ind];
            for (int i = Math.max(0, calculatedStepsCnt - maxDelay - 1); i < calculatedStepsCnt; i++) {
                if (Math.abs(states[i][ind] - val) > this.stablePrecision)
                    return false;
            }
        }

        return true;
    }


    /**
     * remain in stateList only states which
     * @param stateList states
     * @param steps last steps count on which states can be considered as the same
     */
    private void remainDominantsWithEps(List<State> stateList, int steps) {
        if (!canPredictDominantStates()) {
            stateList.removeIf(state -> !isStateAlive(state));
            return;
        }

        List<Integer> indexes = getStatesIndexes(stateList);

        // remain only biggest state and all states in EPS neighbourhood for
        // prevent calculation error in the same graphics
        // the neighbourhoods finding in last LAST_STEPS_PERCENTAGE steps
        for (int ind1: indexes) {
            for (int ind2: indexes) {
                if (ind2 == ind1)
                    continue;

                boolean ind1Lower = true;
                boolean near = true;
                if (higherAccuracy) {
                    int j = bdStates.size();
                    ListIterator<BigDecimal[]> iterator = bdStates.listIterator(bdStates.size());
                    while ( iterator.hasPrevious() && (j-- >= 0) ) {
                        BigDecimal[] bd = iterator.previous();
                        if (bd[ind1].compareTo(bd[ind2]) > 0) {
                            ind1Lower = false;
                            break;
                        }
                        if (near && bd[ind1].subtract(bd[ind2]).abs().divide(bd[ind1].max(bd[ind2])).compareTo(new BigDecimal(EPS)) > 0) {
                            near = false;
                        }
                    }
                }
                else {
                    for (int step = calculatedStepsCnt - steps; step < calculatedStepsCnt; step++) {
                        if (states[step][ind1] > states[step][ind2]) {
                            ind1Lower = false;
                            break;
                        }
                        if (near && Math.abs(states[step][ind1] - states[step][ind2]) / Math.max(states[step][ind1], states[step][ind2]) > EPS) {
                            near = false;
                        }
                    }
                }
                if (ind1Lower && !near) {
                    stateList.removeIf(x -> x.getId() == task.getStates().get(ind1).getId());
                    break;
                }
            }
        }
    }


    /**
     *
     * @param states states
     * @return states indexes in states
     */
    private List<Integer> getStatesIndexes(List<State> states) {
        return states.stream()
                .map(this::getStateIndex)
                .collect(Collectors.toList());
    }


    /**
     *
     * @param stateIndex index of state in states
     * @return steps in state cycle period, -1 if there is no cycle
     */
    private int getStateCycleSteps(int stateIndex) {
        double cnt = this.states[calculatedStepsCnt - 1][stateIndex];

        for (int step = calculatedStepsCnt - 2; step >= calculatedStepsCnt / 2 + 1; step--) {
            if (this.states[step][stateIndex] == cnt) {
                boolean isCyclic = true;
                for (int i = calculatedStepsCnt - step; i >= 0; i--) {
                    if (states[calculatedStepsCnt - 1 - i][stateIndex] != states[step - i][stateIndex]) {
                        isCyclic = false;
                        break;
                    }
                }
                if (isCyclic) {
                    return calculatedStepsCnt - step - 1;
                }
            }
        }

        return -1;
    }


    /**
     *
     * @param states states which must be considered in cycle
     * @return steps count in task cycle period, -1 if task non-cyclic
     */
    private int getTaskCycleSteps(List<State> states) {
        List<Integer> indexes = getStatesIndexes(states);
        int maxCycleSteps = -1;
        for (int ind: indexes) {
            int steps = getStateCycleSteps(ind);
            if (steps == -1)
                return -1;
            maxCycleSteps = Math.max(maxCycleSteps, steps);
        }

        if (maxCycleSteps == -1 || calculatedStepsCnt - maxCycleSteps <= getMaxDelay())
            return -1;

        for (int step = calculatedStepsCnt - 1; step >= calculatedStepsCnt - maxCycleSteps; step--) {
            for (int ind: indexes) {
                if (this.states[step][ind] != this.states[step - maxCycleSteps][ind])
                    return -1;
            }
        }

        return maxCycleSteps;
    }

    /**
     *
     * @param states states list
     * @return all states which can be alive from states
     */
    private List<State> getAliveStates(List<State> states) {
        return states.stream()
                .filter(this::isStateAlive)
                .collect(Collectors.toList());
    }


    /**
     *
     * @return strictly ordered alive states
     * @see #getDominantsOrdered
     */
    private List<List<State>> getStableDominants() {
        List<State> aliveStates = getAliveStates(analysedStatesList);

        // remain states whose relative count greater then EPS
        double totalCnt = 0;
        BigDecimal bdTotalCnt = BigDecimal.ZERO;
        if (higherAccuracy) {
            for (int i = 0; i < bdStates.getLast().length; i++)
                bdTotalCnt = bdTotalCnt.add(bdStates.getLast()[i]);
        } else {
            for (int i = 0; i < states[0].length; i++)
                totalCnt += states[calculatedStepsCnt - 1][i];
        }

        final BigDecimal fBdTotalCnt = bdTotalCnt;
        final double fTotalCnt = totalCnt;
        aliveStates = aliveStates.stream().filter(state -> {
            state = getStateById(state.getId());
            if (higherAccuracy) {
                return getStateCountHigherAccuracy(state).divide(fBdTotalCnt).compareTo(new BigDecimal(EPS)) > 0;
            } else {
                return getStateCount(state) / fTotalCnt > EPS;
            }
        }).collect(Collectors.toList());


        List<List<State>> result = new ArrayList<>();
        List<State> dominants = new ArrayList<>(aliveStates);

        while (dominants.size() > 0) {
            remainDominantsWithEps(dominants, 1);
            result.add(0,dominants);
            aliveStates.removeAll(dominants);
            dominants = new ArrayList<>(aliveStates);
        }

        return result;
    }

    /**
     *
     * @return alive states ordered by integral sum from last half steps
     * @see #getDominantsOrdered
     */
    private List<List<State>> getCyclicDominants() {
        Map<State, Double> map = new HashMap<>();

        for (int ind = 0; ind < states[0].length; ind++) {
            double sum = 0;
            for (int step = calculatedStepsCnt / 2; step < calculatedStepsCnt; step++) {
                sum += states[step][ind];
            }
            map.put(task.getStates().get(ind), sum);
        }

        /*map = map.entrySet().stream()
                .filter(entry -> isStateAlive(entry.getKey()))
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));*/
        List<Map.Entry<State, Double>> entries = map.entrySet().stream()
                .filter(entry -> isStateAlive(entry.getKey()))
                .sorted(Comparator.comparing(Map.Entry::getValue))
                .collect(Collectors.toList());

        double lastVal = entries.iterator().next().getValue();
        List<List<State>> result = new ArrayList<>();
        List<State> eq = new ArrayList<>();
        for (Map.Entry<State, Double> entry: entries) {
            if (entry.getValue() / lastVal > 1.1) {
                result.add(eq);
                eq = new ArrayList<>();
                lastVal = entry.getValue();
            }
            eq.add(entry.getKey());
        }
        if (eq.size() > 0)
            result.add(eq);

        return result;
    }


    /**
     *
     * @return  list which contains Lists of states with the same (near) states count on last step
     *          first element contains lower states, last element - biggest
     */
    public List<List<State>> getDominantsOrdered() {
        if (calculationFinishedReason == CalculationFinishedReason.TASK_STABLE) {
            return getStableDominants();
        }

        /*
         * try to find states trends in last half steps
         * if all trends monotonic consider task as stable
         * and return result with all states higher trends
         */
        if (calculatedStepsCnt == task.getStepsCount() && calculationMode == CalculationMode.SIMPLE) {
            Map<State, Integer> trends = new HashMap<>();
            boolean isTrendsDetermined = true;
            cycle:
            for (State state: getAliveStates(analysedStatesList)) {
                int ind = getStateIndex(state);
                int trend = (int)Math.signum(states[calculatedStepsCnt / 2][ind] - states[calculatedStepsCnt / 2 - 1][ind]);
                trends.put(state, trend);
                for (int step = calculatedStepsCnt / 2 + 1; step < calculatedStepsCnt; step++) {
                    if (trend != (int)Math.signum(states[step][ind] - states[step - 1][ind])) {
                        isTrendsDetermined = false;
                        break cycle;
                    }
                }
            }

            if (isTrendsDetermined) {
                calculationFinishedReason = CalculationFinishedReason.TASK_STABLE;
                List<List<State>> result = new ArrayList<>();
                for (int val : new int[]{-1, 0, 1}) {
                    if (trends.containsValue(val)) {
                        result.add(
                                trends.entrySet().stream()
                                        .filter(entry -> entry.getValue() == val)
                                        .map(Map.Entry::getKey)
                                        .collect(Collectors.toList())
                        );
                        if (calculationMode == CalculationMode.STATES_TRENDS) {
                            return result;
                        }
                    }
                }
                return result;
            }
        }

        return getCyclicDominants();
    }


    private boolean isTaskCyclic() {
        return getTaskCycleSteps(getAliveStates(analysedStatesList)) != -1;
    }


    /**
     * 
     * @return states whose predicted values are greatest in system
     */
    public List<State> getPredictedDominants() {
        if (calculationMode != CalculationMode.STATES_TRENDS) {
            throw new RuntimeException("invalid getPredictedDominants function call");
        }

        List<State> aliveStates = getAliveStates(analysedStatesList);
        int taskCycleSteps = -1;
        if (task.getStepsCount() <= calculatedStepsCnt && calculationFinishedReason == null) {
            taskCycleSteps = getTaskCycleSteps(aliveStates);
            if (taskCycleSteps != -1) {
                calculationFinishedReason = CalculationFinishedReason.TASK_CYCLIC;
            }
        }
           /* if (task.getTransitions().get(0).getProbability() == 0.4 && task.getTransitions().get(1).getProbability() == 0.43333) {
                int k = 0;
            }*/
        /*System.out.println(task.getTransitions().get(0).getProbability() + " " +
                task.getTransitions().get(1).getProbability() + " " +
                calculationFinishedReason + " " + calculatedStepsCnt

        );*/

        int remainDominantsSteps = (int)(calculatedStepsCnt * (1 - LAST_STEPS_PERCENTAGE));
        if (calculationFinishedReason == CalculationFinishedReason.TASK_CYCLIC) {
            remainDominantsSteps = taskCycleSteps;
        }
        else if (calculationFinishedReason == CalculationFinishedReason.TASK_STABLE) {
            remainDominantsSteps = 1;
        }

        remainDominantsWithEps(aliveStates, remainDominantsSteps);
        //remainDominantsWithEps(analysedStatesList);

        if (aliveStates.size() > 0) {
            return aliveStates;
        } else {
            return /*analysedStatesList.stream()
                    .filter(this::isStateAlive)
                    .collect(Collectors.toList());*/
                    analysedStatesList;
        }
    }


    /**
     * is predicted state count can be above zero
     * @param state state
     * @return if predicted state count > 0 return true, otherwise return false
     */
    public boolean isStateAlive(State state) {
        state = getStateById(state.getId());

        double stateCnt = 0;
        BigDecimal bdStateCnt = BigDecimal.ZERO;
        if (higherAccuracy) {
            bdStateCnt = getStateCountHigherAccuracy(state);
        } else {
            stateCnt = getStateCount(state);
        }

        if (!getGraphVertexByState(state).canStateChange
                || calculationFinishedReason == null
                || calculationFinishedReason == CalculationFinishedReason.TASK_STABLE
                || calculatedStepsCnt >= task.getStepsCount()
        ) {
            if (higherAccuracy) {
                return bdStateCnt.compareTo(BigDecimal.ZERO) > 0;
            } else {
                return stateCnt > 0;
            }
        }

        if (calculationMode == CalculationMode.STATES_TRENDS
                && calculationFinishedReason == CalculationFinishedReason.ALL_STATES_TRENDS_HAVE_BEEN_IDENTIFIED
        ) {
            StateVertex sv = getGraphVertexByState(state);
            if (sv.getStateTrend() == StateTrend.DECREASE)
                return sv.canBeStable;
            return true;
        }

        return true;
    }


    /**
     * get state count on last step with normal accuracy
     * @param state state
     * @return state count on last calculated step
     */
    private double getStateCount(State state) {
        return states[calculatedStepsCnt - 1][task.getStates().indexOf(state)];
    }

    /**
     * get state count on last step with higher accuracy
     * @param state state
     * @return state count on last calculated step
     */
    private BigDecimal getStateCountHigherAccuracy(State state) {
        return bdStates.getLast()[task.getStates().indexOf(state)];
    }

    /**
     * updates states count in graph by new calculated values
     */
    private void updateStates(int calculatedStepsCnt) {
        this.calculatedStepsCnt = Math.max(calculatedStepsCnt, 1);

        // update states count in statesSet
        for (int i = 0; i < task.getStates().size(); i++) {
            StateVertex vertex = getGraphVertexByState(getStateById(task.getStates().get(i).getId()));
            if (vertex != null && vertex != externalVertex) {
                if (higherAccuracy) {
                    vertex.setStateCount(bdStates.getLast()[i]);
                } else {
                    vertex.setStateCount(states[this.calculatedStepsCnt - 1][i]);
                }
            }
        }
    }


    /**
     * update analyser data from calculated data
     * @param calculatedStepsCnt calculated steps from 0
     */
    public void update(int calculatedStepsCnt) {
        updateStates(calculatedStepsCnt);
        updateGraph();
        updateCalculationFinishedReason();
    }


    /**
     * add states count from first element
     * call it each calculated step
     * @param statesCount calculated states count where first element match last calculated step
     */
    public void updateHigherAccuracyStatesCount(BigDecimal[][] statesCount) {
        BigDecimal[] newVals = new BigDecimal[statesCount[0].length];
        int i = 0;
        for (BigDecimal bd: statesCount[0]) {
            newVals[i] = bd.add(BigDecimal.ZERO);
            i++;
        }
        this.bdStates.add(newVals);

        int maxSize = (int)(task.getStepsCount() * LAST_STEPS_PERCENTAGE);
        int size = bdStates.size();
        if (size > maxSize) {
            while (size-- > maxSize) {
                bdStates.removeFirst();
            }
        }
    }
    

    /**
     * check task stable
     * @return true if all states in task stable
     */
    private boolean isTaskStable() {
        for (State state: task.getStates()) {
            if (!isStateStable(state))
                return false;
        }

        return true;
    }

    
    /**
     * (call it only after update, it doesn't update implicit)
     * update {@link TaskAnalyser#calculationFinishedReason calculationFinishedReason} value
     */
    public void updateCalculationFinishedReason() {
        /*if (calculationFinishedReason != null)
            return;*/

        if (isTaskStable()) {
            calculationFinishedReason = CalculationFinishedReason.TASK_STABLE;
            return;
        }

        if (calculatedStepsCnt == task.getStepsCount()) {
            int cycle = getTaskCycleSteps(getAliveStates(analysedStatesList));
            if (cycle == 2) {
                calculationFinishedReason = CalculationFinishedReason.TASK_STABLE;
                return;
            }
        }

        if (calculationMode == CalculationMode.STATES_TRENDS) {
            for (StateVertex vertex : statesSet) {
                if (vertex == externalVertex) {
                    continue;
                }

                // dead state can be alive
                if (vertex.canStateChange
                        && ((higherAccuracy && vertex.getStateCountHigherAccuracy().compareTo(BigDecimal.ZERO) == 0)
                        || (!higherAccuracy && vertex.getStateCount() == 0))
                        ) {
                    calculationFinishedReason = null;
                    return;
                }

                if (((higherAccuracy && vertex.getStateCountHigherAccuracy().compareTo(BigDecimal.ZERO) > 0)
                        || (!higherAccuracy && vertex.getStateCount() > 0))
                        && !isStateStable(vertex.getState())
                        ) {
                    // check whether states trends determinate and can be stabilized
                    for (State state : analysedStatesList) {
                        StateVertex sv = getGraphVertexByState(getStateById(state.getId()));
                        StateTrend stateTrend = sv.getStateTrend();

                        if (stateTrend == StateTrend.ANY) {
                            calculationFinishedReason = null;
                            return;
                        }

                        // if state decrease check can it stabilize or not
                        if (stateTrend == StateTrend.DECREASE) {
                            if (sv.canBeStable()) {
                                calculationFinishedReason = null;
                                return;
                            }
                        }
                    }
                }
            }

            calculationFinishedReason = CalculationFinishedReason.ALL_STATES_TRENDS_HAVE_BEEN_IDENTIFIED;
        }
    }


    public boolean canPredictDominantStates() {
        if (calculationFinishedReason == null) {
            return false;
        }

        if (calculationFinishedReason == CalculationFinishedReason.TASK_STABLE) {
            return true;
        }

        if (calculationFinishedReason == CalculationFinishedReason.TASK_CYCLIC) {
            return true;
        }

        if (calculationMode == CalculationMode.STATES_TRENDS
                && calculationFinishedReason == CalculationFinishedReason.ALL_STATES_TRENDS_HAVE_BEEN_IDENTIFIED) {
            // return true when there is only one increase trend and all decrease states which
            // can be stabilized lower than increase state
            List<State> aliveStates = getAliveStates(analysedStatesList);
            List<State> decreaseAndCanBeStable = aliveStates.stream()
                    .filter(state -> {
                        StateVertex sv = getGraphVertexByState(getStateById(state.getId()));
                        return sv.getStateTrend() == StateTrend.DECREASE && sv.canBeStable();
                    })
                    .collect(Collectors.toList());
            List<State> increase = aliveStates.stream()
                    .filter(state -> getGraphVertexByState(getStateById(state.getId())).getStateTrend() == StateTrend.INCREASE)
                    .collect(Collectors.toList());

            if (increase.size() != 1)
                return false;

            Double inc = increase.get(0).getCount();
            for (State dec: decreaseAndCanBeStable) {
                if (dec.getCount() >= inc)
                    return false;
            }
            return true;
        }

        return false;
    }


    /**
     * check can state decrease stop before state count == 0
     * (use only for decrease trend)
     * @param stateVertex state vertex
     * @return can state count be stable and be > 0
     */
    private boolean canStateStabilize(StateVertex stateVertex) {
        // at least one transition must be alive all time
        boolean aliveTransitionExist = false;

        for (TransitionVertex tv: stateVertex.getTransitions()) {
            if (!canTransitionBeDead(tv, Collections.singletonList(stateVertex))) {
                aliveTransitionExist = true;
                break;
            }
        }

        return !aliveTransitionExist;
    }



    /**
     * can transition will not occur
     * @param transitionVertex transition vertex
     * @param exclusions stateVertexes that shouldn't be considered in transition dependencies
     * @return true if transition can be dead
     */
    private boolean canTransitionBeDead(TransitionVertex transitionVertex, List<StateVertex> exclusions) {
        for (StateVertex tvDependency: transitionVertex.getTransitionDependencies()) {
            if (exclusions.contains(tvDependency)) {
                continue;
            }

            StateTrend st = tvDependency.getStateTrend();
            if (st == StateTrend.ANY || st == StateTrend.DECREASE)
                return true;
        }

        return false;
    }


    /**
     * update vertex data depended on calculated data
     */
    private void updateGraph() {
        calculatedStateVertexes.clear();
        calculatedTransitionsVertexes.clear();

        // update states count changes
        for (StateVertex vertex: statesSet) {
            if (vertex == externalVertex)
                continue;

            vertex.setCanStateChange(isStateVertexCanBeChanged(vertex));
        }


        // update decreased states can be stable
        for (State state: analysedStatesList) {
            StateVertex sv = getGraphVertexByState(getStateById(state.getId()));
            if (sv.getStateTrend() == StateTrend.DECREASE && sv.canBeStable()) {
                sv.setCanBeStable(canStateStabilize(sv));
            }
        }
    }



    /**
     * determine could state vertex count be changed or not
     * @param stateVertex state vertex
     * @return true if vertex state count can be changed, false otherwise
     */
    private boolean isStateVertexCanBeChanged(StateVertex stateVertex) {
        if (calculatedStateVertexes.contains(stateVertex))
            return stateVertex.canStateChange();

        // state stable
        if (!stateVertex.canStateChange)
            return false;


        // set intermediate value to false to prevent cycle path in statesSet
        stateVertex.setCanStateChange(false);
        boolean canBeChanged = false;

        for (TransitionVertex transitionVertex: stateVertex.getTransitions()) {
            if (isTransitionVertexCanBeInvoked(transitionVertex)) {
                canBeChanged = true;
            }
        }

        stateVertex.setCanStateChange(canBeChanged);
        calculatedStateVertexes.add(stateVertex);
        return canBeChanged;
    }



    /**
     * determine could transition vertex be invoked
     * @param transitionVertex transition vertex
     * @return true if transition vertex can be invoked, false otherwise
     */
    private boolean isTransitionVertexCanBeInvoked(TransitionVertex transitionVertex) {
        if (calculatedTransitionsVertexes.contains(transitionVertex))
            return transitionVertex.canBeInvoked();

        if (!transitionVertex.canBeInvoked())
            return false;

        if (transitionVertex.getTransition().getProbability() == 0) {
            transitionVertex.setCanBeInvoked(false);
            return false;
        }

        // set intermediate value to false to prevent cycle path in statesSet
        transitionVertex.setCanBeInvoked(false);
        boolean canBeInvoked = true;

        for (StateVertex stateVertex: transitionVertex.getTransitionDependencies()) {
            if (isStateZeroLastSteps(stateVertex.getState(), transitionVertex.getStateDelay(stateVertex.getState()))
                            && !isStateVertexCanBeChanged(stateVertex))
            {
                canBeInvoked = false;
                break;
            }
        }

        transitionVertex.setCanBeInvoked(canBeInvoked);
        calculatedTransitionsVertexes.add(transitionVertex);

        return canBeInvoked;
    }


    /**
     * check that state count was zero for last steps
     * @param state state
     * @param steps steps count
     * @return true if count was 0 in all steps, false otherwise
     */
    private boolean isStateZeroLastSteps(State state, int steps) {
        int ind = task.getStates().indexOf(state);
        if (ind == -1) {
            return true;
        }

        if (higherAccuracy) {
            int i = bdStates.size();
            ListIterator<BigDecimal[]> iterator = bdStates.listIterator(bdStates.size());
            while ( iterator.hasPrevious() && (i-- >= 0) ) {
                if (iterator.previous()[ind].compareTo(BigDecimal.ZERO) != 0)
                    return false;
            }
        } else {
            for (int i = Math.max(0, calculatedStepsCnt - 1 - steps); i < calculatedStepsCnt; i++) {
                if (states[i][ind] != 0)
                    return false;
            }
        }
        return true;
    }



    /**
     * init task statesSet
     * call it once for a single task before calculating
     */
    public void buildGraph() {
        if (this.task == null) {
            return;
        }

        // add all sates to graph
        statesSet = new HashSet<>();
        externalState = new State();
        externalVertex = new StateVertex(externalState, Integer.MAX_VALUE);
        statesSet.add(externalVertex);
        for (State state: task.getStates()) {
            statesSet.add(new StateVertex(state, state.getCount()));
        }

        // add all transitions to graph
        transitionsSet = new HashSet<>();
        for (Transition transition: task.getTransitions()) {
            transitionsSet.add(new TransitionVertex(transition));
        }

        // set statesSet edges
        for (TransitionVertex transitionVertex: transitionsSet) {
            Transition transition = transitionVertex.getTransition();
            transitionVertex.setTransitionDependencies(
                    getTransitionDependencies(transition).stream()
                            .map(this::getGraphVertexByState)
                            .collect(Collectors.toList())
            );

            for (State state: getTransitionChanges(transition)) {
                getGraphVertexByState(state).addTransition(transitionVertex);
            }
        }
    }


    /**
     *
     * @param state state
     * @return state index in states
     */
    private int getStateIndex(State state) {
        return task.getStates().indexOf(getStateById(state.getId()));
    }





    /**
     * get state by id from task states
     * @param id state id
     * @return state if exist; externalState for id == StateModel.EXTERNAL, null otherwise
     */
    private State getStateById(int id) {
        if (id == State.EXTERNAL)
            return externalState;

        return  task.getStates().stream()
                .filter(state -> state.getId() == id)
                .findFirst()
                .orElse(null);
    }



    /**
     * returns List of States whose count will be changed in transition
     * @param transition transition
     * @return returns List of States which count will changed in transition
     */
    private List<State> getTransitionChanges(Transition transition) {
        List<State> result = new ArrayList<>();

        switch (transition.getMode()) {
            case TransitionMode.SIMPLE:
            case TransitionMode.RESIDUAL:
            case TransitionMode.INHIBITOR: {
                Collections.addAll(result,
                        getStateById(transition.getOperandState()),
                        getStateById(transition.getResultState()));
                break;
            }

            case TransitionMode.RETAINING: {
                Collections.addAll(result,
                        getStateById(transition.getResultState()));
                break;
            }

            case TransitionMode.REMOVING: {
                Collections.addAll(result,
                        getStateById(transition.getOperandState()),
                        getStateById(transition.getSourceState()));
                break;
            }

            default:{
            }
        }

        return result;
    }



    /**
     * returns List of States which must be alive for transition occur
     * @param transition transition
     * @return returns List of States which must be alive for transition occur
     */
    private List<State> getTransitionDependencies(Transition transition) {
        List<State> result = new ArrayList<>();

        Collections.addAll(result,
                getStateById(transition.getOperandState()),
                getStateById(transition.getSourceState()));

        return result;
    }


    /**
     * if StateVertex with state exists in statesSet, returns him, otherwise return null
     * @param state state
     * @return StateVertex from statesSet which contains state
     */
    private StateVertex getGraphVertexByState(State state) {
        return statesSet.stream()
                .filter(a -> a.getState() == state)
                .findFirst()
                .orElse(null);
    }


    /**
     * if TransitionVertex with transition exists in transitionsSet, returns him, otherwise return null
     * @param transition transition
     * @return StateVertex from statesSet which contains state
     */
    private TransitionVertex getGraphVertexByTransition(Transition transition) {
        return transitionsSet.stream()
                .filter(a -> a.getTransition() == transition)
                .findFirst()
                .orElse(null);
    }



    /**
     * state count change direction
     */
    private enum StateTrend {
        /** only can increase */
        INCREASE,
        /** only decrease */
        DECREASE,
        /** can increase or decrease */
        ANY,
        /** can't be changed */
        STABLE
    }



    /**
     * StateModel Vertex in {@link TaskAnalyser#statesSet statesSet}
     */
    private class StateVertex {
        private State state;
        private double stateCount;
        private BigDecimal bdStateCount; // state count for higher accuracy
        /** specify if state count can be changed */
        private boolean canStateChange = true;
        /** specify if state can stabilize and be alive (used for decrease trend) */
        private boolean canBeStable = true;
        /** List of transitions which can change state count */
        private Set<TransitionVertex> transitions = new HashSet<>();
        /** List of transitions which increase state count */
        private Set<TransitionVertex> increaseTransitions = new HashSet<>();
        /** List of transitions which decrease state count */
        private Set<TransitionVertex> decreaseTransitions = new HashSet<>();


        StateVertex(State state, double stateCount) {
            this.state = state;
            if (higherAccuracy)
                this.bdStateCount = new BigDecimal(stateCount);
            else
                this.stateCount = stateCount;
        }

        void addTransition(TransitionVertex transition) {
            this.transitions.add(transition);
            this.setStateTrendByTransition(transition);
        }


        /**
         * set state trend and add transition to transitions list
         * @param transitionVertex transition vertex
         */
        private void setStateTrendByTransition(TransitionVertex transitionVertex) {
            StateTrend stateTrend = null;

            Transition transition = transitionVertex.getTransition();
            switch (transition.getMode()) {
                case TransitionMode.SIMPLE:
                case TransitionMode.RESIDUAL:
                case TransitionMode.INHIBITOR: {
                    if (getStateById(transition.getOperandState()) == this.state)
                        stateTrend = StateTrend.DECREASE;
                    if (getStateById(transition.getResultState()) == this.state) {
                        stateTrend = (stateTrend == StateTrend.DECREASE) ?
                                null :
                                StateTrend.INCREASE;
                    }
                    break;
                }

                case TransitionMode.RETAINING: {
                    if (getStateById(transition.getResultState()) == this.state)
                        stateTrend = StateTrend.INCREASE;
                    break;
                }

                case TransitionMode.REMOVING: {
                    if (getStateById(transition.getSourceState()) == this.state
                            || getStateById(transition.getOperandState()) == this.state)
                        stateTrend = StateTrend.DECREASE;
                    if (getStateById(transition.getResultState()) == this.state)
                        stateTrend = (stateTrend == StateTrend.DECREASE) ?
                                null :
                                StateTrend.INCREASE;
                    break;
                }
            }

            if (stateTrend == null)
                return;

            switch (stateTrend) {
                case INCREASE: {
                    increaseTransitions.add(transitionVertex);
                    break;
                }
                case DECREASE: {
                    decreaseTransitions.add(transitionVertex);
                    break;
                }
                case ANY: {
                    increaseTransitions.add(transitionVertex);
                    decreaseTransitions.add(transitionVertex);
                    break;
                }
            }
        }


        /**
         * get state trend
         * @return state trend
         */
        private StateTrend getStateTrend() {
            if (!canStateChange())
                return StateTrend.STABLE;

            boolean increase = false, decrease = false;

            for (TransitionVertex tv: increaseTransitions) {
                if (isTransitionVertexCanBeInvoked(tv)) {
                    increase = true;
                    break;
                }
            }

            for (TransitionVertex tv: decreaseTransitions) {
                if (isTransitionVertexCanBeInvoked(tv)) {
                    decrease = true;
                    break;
                }
            }

            if (increase && decrease)
                return StateTrend.ANY;
            if (increase)
                return StateTrend.INCREASE;
            if (decrease)
                return StateTrend.DECREASE;
            return StateTrend.STABLE;
        }


        Set<TransitionVertex> getTransitions() {
            return transitions;
        }


        State getState() {
            return state;
        }


        boolean canStateChange() {
            return canStateChange;
        }


        void setCanStateChange(boolean val) {
            canStateChange = val;
        }


        double getStateCount() {
            return stateCount;
        }

        BigDecimal getStateCountHigherAccuracy() {
            return bdStateCount;
        }


        void setStateCount(double stateCount) {
            this.stateCount = stateCount;
        }

        void setStateCount(BigDecimal stateCount) {
            this.bdStateCount = stateCount;
        }

        boolean canBeStable() {
            return this.canBeStable;
        }


        void setCanBeStable(boolean canBeStable) {
            this.canBeStable = canBeStable;
        }
    }



    /**
     * Transition Vertex in {@link TaskAnalyser#statesSet statesSet}
     */
    class TransitionVertex {
        private Transition transition;
        /** can transition proceed */
        private boolean canBeInvoked;
        /** states on which this transitions depends */
        private List<StateVertex> transitionDependencies;



        TransitionVertex(Transition transition) {
            this.setCanBeInvoked(true);
            this.setTransition(transition);
            this.transitionDependencies = new ArrayList<>();
        }


        Transition getTransition() {
            return transition;
        }


        void setTransition(Transition transition) {
            this.transition = transition;
        }


        boolean canBeInvoked() {
            return canBeInvoked;
        }


        void setCanBeInvoked(boolean canBeInvoked) {
            this.canBeInvoked = canBeInvoked;
        }


        List<StateVertex> getTransitionDependencies() {
            return transitionDependencies;
        }


        private void setTransitionDependencies(List<StateVertex> transitionDependencies) {
            this.transitionDependencies = transitionDependencies;
        }


        /**
         * get delay for state in transition
         * operand delay if state equals operandState, source delay if state equals sourceState,
         * @param state state
         * @return delay for state in transition
         */
        int getStateDelay(State state) {
            if (transition.getSourceState() == state.getId())
                return transition.getSourceDelay();
            if (transition.getOperandDelay() == state.getId())
                return transition.getOperandDelay();
            return 0;
        }
    }

}