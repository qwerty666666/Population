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
package population.model;

import population.component.Calculator;
import population.util.Utils;

import java.util.List;
import java.util.Map;

public class Task {
    private int mId;
    private String mName;
    private List<State> mStates;
    private List<Transition> mTransitions;
    private int mStartPoint;
    private int mStepsCount;
    private boolean mParallel;
    private boolean mHigherAccuracy;
    private boolean mAllowNegative;
    private char mColumnSeparator;
    private char mDecimalSeparator;
    private String mLineSeparator;
    private String mEncoding;
    private int mScale;

    public Task(){
    }

    public Task(Task task) {
        setName(task.getName());
        setStates(task.getStates());
        setTransitions(task.getTransitions());
        setStartPoint(task.getStartPoint());
        setStepsCount(task.getStepsCount());
        setParallel(task.isParallel());
        setHigherAccuracy(task.isHigherAccuracy());
        setAllowNegative(task.isAllowNegative());
        setColumnSeparator(task.getColumnSeparator());
        setDecimalSeparator(task.getDecimalSeparator());
        setLineSeparator(task.getLineSeparator());
        setEncoding(task.getEncoding());
        setScale(task.getScale());
    }

    public int getScale() {
        return mScale;
    }

    public void setScale(int scale) {
        this.mScale = scale;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public List<State> getStates() {
        return mStates;
    }

    public void setStates(List<State> states) {
        mStates = states;
    }

    public List<Transition> getTransitions() {
        return mTransitions;
    }

    public void setTransitions(List<Transition> transitions) {
        mTransitions = transitions;
    }

    public int getStartPoint() {
        return mStartPoint;
    }

    public void setStartPoint(int startPoint) {
        mStartPoint = startPoint;
    }

    public int getStepsCount() {
        return mStepsCount;
    }

    public void setStepsCount(int stepsCount) {
        mStepsCount = stepsCount;
    }

    public boolean isParallel() {
        return mParallel;
    }

    public void setParallel(boolean parallel) {
        mParallel = parallel;
    }

    public boolean isHigherAccuracy() {
        return mHigherAccuracy;
    }

    public void setHigherAccuracy(boolean higherAccuracy) {
        mHigherAccuracy = higherAccuracy;
    }

    public boolean isAllowNegative() {
        return mAllowNegative;
    }

    public void setAllowNegative(boolean allowNegative) {
        mAllowNegative = allowNegative;
    }

    public char getColumnSeparator() {
        return mColumnSeparator;
    }

    public void setColumnSeparator(char columnSeparator) {
        mColumnSeparator = columnSeparator;
    }

    public char getDecimalSeparator() {
        return mDecimalSeparator;
    }

    public void setDecimalSeparator(char decimalSeparator) {
        mDecimalSeparator = decimalSeparator;
    }

    public String getLineSeparator() {
        return mLineSeparator;
    }

    public void setLineSeparator(String lineSeparator) {
        mLineSeparator = lineSeparator;
    }

    public String getEncoding() {
        return mEncoding;
    }

    public void setEncoding(String encoding) {
        mEncoding = encoding;
    }

    /**
     * Считать настройки из задачи
     *
     * @param task     задача
     * @param settings настройки
     */
    public static void readSettings(Task task, Map<String, String> settings) {
        settings.put(Keys.START_POINT, String.valueOf(task.getStartPoint()));
        settings.put(Keys.STEPS_COUNT, String.valueOf(task.getStepsCount()));
        settings.put(Keys.PARALLEL, String.valueOf(task.isParallel()));
        settings.put(Keys.HIGHER_ACCURACY, String.valueOf(task.isHigherAccuracy()));
        settings.put(Keys.ALLOW_NEGATIVE, String.valueOf(task.isAllowNegative()));
        settings.put(Keys.COLUMN_SEPARATOR, String.valueOf(task.getColumnSeparator()));
        settings.put(Keys.DECIMAL_SEPARATOR, String.valueOf(task.getDecimalSeparator()));
        settings.put(Keys.LINE_SEPARATOR, task.getLineSeparator());
        settings.put(Keys.ENCODING, task.getEncoding());
        settings.put(Keys.SCALE, String.valueOf(task.getScale()));
    }

    /**
     * Записать настройки в задачу
     *
     * @param task     задача
     * @param settings настройки
     */
    public static void writeSettings(Task task, Map<String, String> settings) {
        task.setStartPoint(Integer.parseInt(settings.get(Keys.START_POINT)));
        task.setStepsCount(Integer.parseInt(settings.get(Keys.STEPS_COUNT)));
        task.setParallel(Boolean.parseBoolean(settings.get(Keys.PARALLEL)));
        task.setHigherAccuracy(Boolean.parseBoolean(settings.get(Keys.HIGHER_ACCURACY)));
        task.setAllowNegative(Boolean.parseBoolean(settings.get(Keys.ALLOW_NEGATIVE)));
        task.setColumnSeparator(settings.get(Keys.COLUMN_SEPARATOR).charAt(0));
        task.setDecimalSeparator(settings.get(Keys.DECIMAL_SEPARATOR).charAt(0));
        task.setLineSeparator(settings.get(Keys.LINE_SEPARATOR));
        task.setEncoding(settings.get(Keys.ENCODING));
        if (settings.containsKey(Keys.SCALE)) {
            task.setScale(new Integer(settings.get(Keys.SCALE)));
        } else {
            int scale = task.isHigherAccuracy() ? Calculator.HIGHER_ACCURACY_SCALE : Utils.MAX_PRECISION;
            task.setScale(scale);
        }
    }

    public static final class Keys {
        public static final String START_POINT = "StartPoint";
        public static final String STEPS_COUNT = "StepsCount";
        public static final String PARALLEL = "Parallel";
        public static final String HIGHER_ACCURACY = "HigherAccuracy";
        public static final String ALLOW_NEGATIVE = "AllowNegative";
        public static final String COLUMN_SEPARATOR = "ColumnSeparator";
        public static final String DECIMAL_SEPARATOR = "DecimalSeparator";
        public static final String LINE_SEPARATOR = "LineSeparator";
        public static final String ENCODING = "Encoding";
        public static final String SCALE = "Scale";

        private Keys() {
        }
    }
}
