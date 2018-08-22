/*
 * Population
 * Copyright (C) 2016 Yuriy Budiyev [yuriy.budiyev@yandex.ru]
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package population.util;

import population.model.*;
import population.model.State;
import population.model.Transition;
import population.model.TransitionModel.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public final class TaskParser {
    private static final String FORMAT_NAME = "PopulationModelingTask";
    private static final int FORMAT_VERSION = 4;
    private static final String KEY_STATES_OPEN = "States";
    private static final String KEY_STATES_CLOSE = "//States";
    private static final String KEY_STATES_IN_TRANSITION_OPEN = "StatesInTransition";
    private static final String KEY_STATES_IN_TRANSITION_CLOSE = "//StatesInTransition";
    private static final String KEY_TRANSITIONS_OPEN = "Transitions";
    private static final String KEY_TRANSITIONS_CLOSE = "//Transitions";
    private static final char SEPARATOR = ',';

    private TaskParser() {
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void encodeV4(File file, TaskV4 task) {
        StringTable table = new StringTable();
        table.add(new StringRow(FORMAT_NAME, FORMAT_VERSION));
        table.add(new StringRow(Task.Keys.START_POINT, task.getStartPoint()));
        table.add(new StringRow(Task.Keys.STEPS_COUNT, task.getStepsCount()));

        // STATES
        table.add(new StringRow(KEY_STATES_OPEN));
        for (population.model.StateModel.State state : task.getStates()) {
            table.add(new StringRow(
                    state.getId(),
                    state.getName(),
                    state.getAlias(),
                    state.getCount()
            ));
        }
        table.add(new StringRow(KEY_STATES_CLOSE));

        // TRANSITIONS
        table.add(new StringRow(KEY_TRANSITIONS_OPEN));
        for (population.model.TransitionModel.Transition transition : task.getTransitions()) {
            table.add(new StringRow(
                    transition.getId(),
                    transition.getProbability(),
                    transition.getType(),
                    transition.getBlock()
            ));
            // STATE IN TRANSITION
            table.add(new StringRow(KEY_STATES_IN_TRANSITION_OPEN));
            for (StateInTransition state: transition.getStates()) {
                table.add(new StringRow(
                        state.getState() == null ? -1 : state.getState().getId(),
                        state.getIn(),
                        state.getOut(),
                        state.getDelay(),
                        state.getMode()
                ));
            }
            table.add(new StringRow(KEY_STATES_IN_TRANSITION_CLOSE));
        }
        table.add(new StringRow(KEY_TRANSITIONS_CLOSE));

        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            CsvParser.encode(table, new FileOutputStream(file), SEPARATOR, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void encode(File file, Task task) {
        StringTable table = new StringTable();
        table.add(new StringRow(FORMAT_NAME, FORMAT_VERSION));
        table.add(new StringRow(Task.Keys.START_POINT, task.getStartPoint()));
        table.add(new StringRow(Task.Keys.STEPS_COUNT, task.getStepsCount()));
        table.add(new StringRow(Task.Keys.PARALLEL, task.isParallel()));
        table.add(new StringRow(Task.Keys.HIGHER_ACCURACY, task.isHigherAccuracy()));
        table.add(new StringRow(Task.Keys.ALLOW_NEGATIVE, task.isAllowNegative()));
        table.add(new StringRow(Task.Keys.COLUMN_SEPARATOR, task.getColumnSeparator()));
        table.add(new StringRow(Task.Keys.DECIMAL_SEPARATOR, task.getDecimalSeparator()));
        table.add(new StringRow(Task.Keys.LINE_SEPARATOR, task.getLineSeparator()));
        table.add(new StringRow(Task.Keys.ENCODING, task.getEncoding()));
        table.add(new StringRow(Task.Keys.SCALE, task.getScale()));
        table.add(new StringRow(KEY_STATES_OPEN));
        for (State state : task.getStates()) {
            table.add(new StringRow(state.getId(), state.getName(), state.getCount(),
                    state.getDescription()));
        }
        table.add(new StringRow(KEY_STATES_CLOSE));
        table.add(new StringRow(KEY_TRANSITIONS_OPEN));
        for (Transition transition : task.getTransitions()) {
            table.add(new StringRow(transition.getSourceState(), transition.getSourceCoefficient(),
                    transition.getSourceDelay(), transition.getOperandState(),
                    transition.getOperandCoefficient(), transition.getOperandDelay(),
                    transition.getResultState(), transition.getResultCoefficient(),
                    transition.getProbability(), transition.getType(), transition.getMode(),
                    transition.getDescription()));
        }
        table.add(new StringRow(KEY_TRANSITIONS_CLOSE));
        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            CsvParser.encode(table, new FileOutputStream(file), SEPARATOR, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static TaskV4 parse(File file) {
        StringTable table = null;
        try {
            table = CsvParser.parse(new FileInputStream(file), SEPARATOR, "UTF-8");
        } catch (FileNotFoundException ignored) {
        }
        if (table == null) {
            return null;
        }

        TaskV4 task = null;
        switch(Integer.parseInt(table.row(0).cell(1))) {
            case 1: {
                task = parseV1(table);
                break;
            }
            case 4: {
                task = parseV4(table);
                break;
            }
        }
        if (task != null) {
            task.setName(file.getAbsolutePath());
        }
        return task;
    }

    public static TaskV4 parseV1(StringTable table) {
        TaskV4 task = new TaskV4();

        ObservableList<population.model.StateModel.State> states = FXCollections.observableArrayList();
        ObservableList<population.model.TransitionModel.Transition> transitions = FXCollections.observableArrayList();

        boolean readingStates = false;
        boolean readingTransitions = false;

        Map<Integer, population.model.StateModel.State> idStateMap = new HashMap<>();

        for (StringRow row : table) {
            if (Objects.equals(row.cell(0), KEY_STATES_OPEN)) {
                readingStates = true;
                continue;
            } else if (Objects.equals(row.cell(0), KEY_STATES_CLOSE)) {
                readingStates = false;
                continue;
            } else if (Objects.equals(row.cell(0), KEY_TRANSITIONS_OPEN)) {
                readingTransitions = true;
                continue;
            } else if (Objects.equals(row.cell(0), KEY_TRANSITIONS_CLOSE)) {
                readingTransitions = false;
                continue;
            } else if (Objects.equals(row.cell(0), Task.Keys.START_POINT)) {
                task.setStartPoint(Integer.parseInt(row.cell(1)));
                continue;
            } else if (Objects.equals(row.cell(0), Task.Keys.STEPS_COUNT)) {
                task.setStepsCount(Integer.parseInt(row.cell(1)));
                continue;
            } else if (Objects.equals(row.cell(0), Task.Keys.ALLOW_NEGATIVE)) {
                task.setIsAllowNegative(Boolean.parseBoolean(row.cell(1)));
                continue;
            }

            if (readingStates) {
                population.model.StateModel.State state = new population.model.StateModel.State();
                state.setName(row.cell(1));
                state.setCount(Double.parseDouble(row.cell(2)));
                idStateMap.put(Integer.parseInt(row.cell(0)), state);
                states.add(state);
            }

            if (readingTransitions) {
                // read states
                population.model.StateModel.State sourceState = idStateMap.get(Integer.parseInt(row.cell(0)));
                double sourceCoefficient = Double.parseDouble(row.cell(1));
                int sourceDelay = Integer.parseInt(row.cell(2));

                population.model.StateModel.State operandState = idStateMap.get(Integer.parseInt(row.cell(3)));
                double operandCoefficient = Double.parseDouble(row.cell(4));
                int operandDelay = Integer.parseInt(row.cell(5));

                population.model.StateModel.State resultState = idStateMap.get(Integer.parseInt(row.cell(6)));
                double resultCoefficient = Double.parseDouble(row.cell(7));

                // read transition data
                double probability = Double.parseDouble(row.cell(8));
                int type = Integer.parseInt(row.cell(9));
                int mode = Integer.parseInt(row.cell(10));


                population.model.TransitionModel.Transition transition = new population.model.TransitionModel.Transition();
                transition.setType(type);
                transition.setProbability(probability);

                StateInTransition source = new StateInTransition();
                source.setState(sourceState);
                source.setDelay(sourceDelay);

                StateInTransition operand = new StateInTransition();
                operand.setState(operandState);
                operand.setDelay(operandDelay);

                StateInTransition result = new StateInTransition();
                result.setState(resultState);

                if (mode == TransitionMode.INHIBITOR) {
                    operand.setMode(StateMode.INHIBITOR);
                } else if (mode == TransitionMode.RESIDUAL) {
                    operand.setMode(StateMode.RESIDUAL);
                }

                switch (mode) {
                    case TransitionMode.SIMPLE:
                    case TransitionMode.RESIDUAL:
                    case TransitionMode.INHIBITOR: {
                        source.setIn(sourceCoefficient);
                        source.setOut(sourceCoefficient);
                        operand.setIn(operandCoefficient);
                        operand.setOut(0);
                        result.setIn(0);
                        result.setOut(resultCoefficient);
                        break;
                    }
                    case TransitionMode.RETAINING: {
                        source.setIn(sourceCoefficient);
                        source.setOut(sourceCoefficient);
                        operand.setIn(operandCoefficient);
                        operand.setOut(operandCoefficient);
                        result.setIn(0);
                        result.setOut(resultCoefficient);
                        break;
                    }
                    case TransitionMode.REMOVING: {
                        source.setIn(sourceCoefficient);
                        source.setOut(0);
                        operand.setIn(operandCoefficient);
                        operand.setOut(0);
                        result.setIn(0);
                        result.setOut(resultCoefficient);
                        break;
                    }
                }

//                if (sourceState == operandState && (type == TransitionType.BLEND || type == TransitionType.SOLUTE)) {
//                    source.setIn(sourceCoefficient + operandCoefficient);
//                    source.setMode(operand.getMode());
//                    transition.getStates().addAll(source, result, new StateInTransition());
//                } else {
//                    transition.getStates().addAll(source, operand, result);
//                }
                transition.getStates().addAll(source, operand, result);
                transition.normalizeStates();

                transitions.add(transition);
            }
        }

        task.setStates(states);
        task.setTransitions(transitions);

        return task;
    }

    public static TaskV4 parseV4(StringTable table) {
        TaskV4 task = new TaskV4();

        ObservableList<population.model.StateModel.State> states = FXCollections.observableArrayList();
        ObservableList<population.model.TransitionModel.Transition> transitions = FXCollections.observableArrayList();
        population.model.TransitionModel.Transition transition = new population.model.TransitionModel.Transition();

        boolean readingStates = false;
        boolean readingTransitions = false;
        boolean readingStateInTransition = false;

        Map<Integer, population.model.StateModel.State> idStateMap = new HashMap<>();

        for (StringRow row : table) {
            if (Objects.equals(row.cell(0), KEY_STATES_OPEN)) {
                readingStates = true;
                continue;
            } else if (Objects.equals(row.cell(0), KEY_STATES_CLOSE)) {
                readingStates = false;
                continue;
            } else if (Objects.equals(row.cell(0), KEY_TRANSITIONS_OPEN)) {
                readingTransitions = true;
                continue;
            } else if (Objects.equals(row.cell(0), KEY_TRANSITIONS_CLOSE)) {
                readingTransitions = false;
                continue;
            } else if (Objects.equals(row.cell(0), KEY_STATES_IN_TRANSITION_OPEN)) {
                readingStateInTransition = true;
                continue;
            } else if (Objects.equals(row.cell(0), KEY_STATES_IN_TRANSITION_CLOSE)) {
                readingStateInTransition = false;
                continue;
            } else if (Objects.equals(row.cell(0), Task.Keys.START_POINT)) {
                task.setStartPoint(Integer.parseInt(row.cell(1)));
                continue;
            } else if (Objects.equals(row.cell(0), Task.Keys.STEPS_COUNT)) {
                task.setStepsCount(Integer.parseInt(row.cell(1)));
                continue;
            } else if (Objects.equals(row.cell(0), Task.Keys.ALLOW_NEGATIVE)) {
                task.setIsAllowNegative(Boolean.parseBoolean(row.cell(1)));
                continue;
            }

            if (readingStates) {
                population.model.StateModel.State state = new population.model.StateModel.State();
                state.setName(row.cell(1));
                state.setAlias(row.cell(2));
                state.setCount(Double.parseDouble(row.cell(3)));
                idStateMap.put(Integer.parseInt(row.cell(0)), state);
                states.add(state);
            }

            if (readingTransitions) {
                if (!readingStateInTransition) {
                    transition = new population.model.TransitionModel.Transition();
                    transition.setProbability(Double.parseDouble(row.cell(1)));
                    transition.setType(Integer.parseInt(row.cell(2)));
                    transition.setBlock(row.cell(3));
                    transitions.add(transition);

                } else {
                    StateInTransition state = new StateInTransition();
                    int id = Integer.parseInt(row.cell(0));
                    state.setState(id == -1 ? null : idStateMap.get(id));
                    state.setIn(Double.parseDouble(row.cell(1)));
                    state.setOut(Double.parseDouble(row.cell(2)));
                    state.setDelay(Integer.parseInt(row.cell(3)));
                    state.setMode(Integer.parseInt(row.cell(4)));
                    transition.getStates().add(state);
                }
            }
        }

        task.setStates(states);
        task.setTransitions(transitions);

        return task;
    }

    public static Task parseLegacy(File file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "windows-1251"))) {
            Task task = new Task();
            task.setName(file.getAbsolutePath());
            int statesCount = Integer.parseInt(reader.readLine());
            List<State> states = new ArrayList<>(statesCount);
            task.setStates(states);
            for (int i = 0; i < statesCount; i++) {
                State state = new State();
                state.setName(reader.readLine());
                state.setCount(Double.parseDouble(reader.readLine().replace(',', '.')));
                states.add(state);
            }
            int transitionsCount = Integer.parseInt(reader.readLine()) - 1;
            List<Transition> transitions = new ArrayList<>(transitionsCount);
            task.setTransitions(transitions);
            for (int i = 0; i < transitionsCount; i++) {
                Transition transition = new Transition();
                transition.setSourceState(findState(reader.readLine(), states));
                transition.setOperandState(findState(reader.readLine(), states));
                transition.setResultState(findState(reader.readLine(), states));
                double intensity = Double.parseDouble(reader.readLine().replace(',', '.'));
                if (intensity > 1) {
                    transition.setProbability(1);
                    transition.setResultCoefficient(intensity);
                } else {
                    transition.setProbability(intensity);
                }
                String typeAndModeString = reader.readLine();
                if (typeAndModeString.trim().length() == 0) {
                    transition.setType(TransitionType.LINEAR);
                    transition.setMode(TransitionMode.SIMPLE);
                } else {
                    int typeAndMode = Integer.parseInt(typeAndModeString);
                    transition.setType(getTransitionType(typeAndMode));
                    transition.setMode(getTransitionMode(typeAndMode));
                }
                transition.setSourceDelay(getDelay(reader.readLine()));
                transition.setOperandDelay(getDelay(reader.readLine()));
                transitions.add(transition);
            }
            return task;
        } catch (IOException e) {
            return null;
        }
    }

    private static int findState(String name, List<State> states) {
        if ("*".equals(name)) {
            return State.EXTERNAL;
        }
        for (State state : states) {
            if (Objects.equals(name, state.getName())) {
                return state.getId();
            }
        }
        return State.UNDEFINED;
    }

    private static int getTransitionMode(int typeAndMode) {
        switch (typeAndMode / 3) {
            case 1: {
                return TransitionMode.RETAINING;
            }
            case 2: {
                return TransitionMode.REMOVING;
            }
            case 3: {
                return TransitionMode.RESIDUAL;
            }
            case 4: {
                return TransitionMode.INHIBITOR;
            }
            default: {
                return TransitionMode.SIMPLE;
            }
        }
    }

    private static int getTransitionType(int typeAndMode) {
        switch (typeAndMode) {
            case 1:
            case 4:
            case 7:
            case 10:
            case 13: {
                return TransitionType.SOLUTE;
            }
            case 2:
            case 5:
            case 8:
            case 11:
            case 14: {
                return TransitionType.BLEND;
            }
            default: {
                return TransitionType.LINEAR;
            }
        }
    }

    private static int getDelay(String string) {
        if (string.trim().length() != 0) {
            return Integer.parseInt(string);
        } else {
            return 0;
        }
    }
}
