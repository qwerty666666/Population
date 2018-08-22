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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Transition {
    private final IntegerProperty mSourceState;
    private final DoubleProperty mSourceCoefficient;
    private final IntegerProperty mSourceDelay;
    private final IntegerProperty mOperandState;
    private final DoubleProperty mOperandCoefficient;
    private final IntegerProperty mOperandDelay;
    private final IntegerProperty mResultState;
    private final DoubleProperty mResultCoefficient;
    private final DoubleProperty mProbability;
    private final IntegerProperty mType;
    private final IntegerProperty mMode;
    private final StringProperty mDescription;

    public Transition() {
        mSourceState = new SimpleIntegerProperty();
        mSourceCoefficient = new SimpleDoubleProperty(1);
        mSourceDelay = new SimpleIntegerProperty();
        mOperandState = new SimpleIntegerProperty();
        mOperandCoefficient = new SimpleDoubleProperty(1);
        mOperandDelay = new SimpleIntegerProperty();
        mResultState = new SimpleIntegerProperty();
        mResultCoefficient = new SimpleDoubleProperty(1);
        mProbability = new SimpleDoubleProperty();
        mType = new SimpleIntegerProperty();
        mMode = new SimpleIntegerProperty();
        mDescription = new SimpleStringProperty("");
    }

    public Transition(int sourceState, double sourceCoefficient, int sourceDelay, int operandState,
            double operandCoefficient, int operandDelay, int resultState, double resultCoefficient,
            double probability, int type, int mode, String description) {
        mSourceState = new SimpleIntegerProperty(sourceState);
        mSourceCoefficient = new SimpleDoubleProperty(sourceCoefficient);
        mSourceDelay = new SimpleIntegerProperty(sourceDelay);
        mOperandState = new SimpleIntegerProperty(operandState);
        mOperandCoefficient = new SimpleDoubleProperty(operandCoefficient);
        mOperandDelay = new SimpleIntegerProperty(operandDelay);
        mResultState = new SimpleIntegerProperty(resultState);
        mResultCoefficient = new SimpleDoubleProperty(resultCoefficient);
        mProbability = new SimpleDoubleProperty(probability);
        mType = new SimpleIntegerProperty(type);
        mMode = new SimpleIntegerProperty(mode);
        mDescription = new SimpleStringProperty(description);
    }

    public int getSourceState() {
        return mSourceState.get();
    }

    public IntegerProperty sourceStateProperty() {
        return mSourceState;
    }

    public void setSourceState(int sourceState) {
        mSourceState.set(sourceState);
    }

    public double getSourceCoefficient() {
        return mSourceCoefficient.get();
    }

    public DoubleProperty sourceCoefficientProperty() {
        return mSourceCoefficient;
    }

    public void setSourceCoefficient(double sourceCoefficient) {
        mSourceCoefficient.set(sourceCoefficient);
    }

    public int getSourceDelay() {
        return mSourceDelay.get();
    }

    public IntegerProperty sourceDelayProperty() {
        return mSourceDelay;
    }

    public void setSourceDelay(int sourceDelay) {
        mSourceDelay.set(sourceDelay);
    }

    public int getOperandState() {
        return mOperandState.get();
    }

    public IntegerProperty operandStateProperty() {
        return mOperandState;
    }

    public void setOperandState(int operandState) {
        mOperandState.set(operandState);
    }

    public double getOperandCoefficient() {
        return mOperandCoefficient.get();
    }

    public DoubleProperty operandCoefficientProperty() {
        return mOperandCoefficient;
    }

    public void setOperandCoefficient(double operandCoefficient) {
        mOperandCoefficient.set(operandCoefficient);
    }

    public int getOperandDelay() {
        return mOperandDelay.get();
    }

    public IntegerProperty operandDelayProperty() {
        return mOperandDelay;
    }

    public void setOperandDelay(int operandDelay) {
        mOperandDelay.set(operandDelay);
    }

    public int getResultState() {
        return mResultState.get();
    }

    public IntegerProperty resultStateProperty() {
        return mResultState;
    }

    public void setResultState(int resultState) {
        mResultState.set(resultState);
    }

    public double getResultCoefficient() {
        return mResultCoefficient.get();
    }

    public DoubleProperty resultCoefficientProperty() {
        return mResultCoefficient;
    }

    public void setResultCoefficient(double resultCoefficient) {
        mResultCoefficient.set(resultCoefficient);
    }

    public double getProbability() {
        return mProbability.get();
    }

    public DoubleProperty probabilityProperty() {
        return mProbability;
    }

    public void setProbability(double probability) {
        mProbability.set(probability);
    }

    public int getType() {
        return mType.get();
    }

    public IntegerProperty typeProperty() {
        return mType;
    }

    public void setType(int type) {
        mType.set(type);
    }

    public int getMode() {
        return mMode.get();
    }

    public IntegerProperty modeProperty() {
        return mMode;
    }

    public void setMode(int mode) {
        mMode.set(mode);
    }

    public String getDescription() {
        return mDescription.get();
    }

    public StringProperty descriptionProperty() {
        return mDescription;
    }

    public void setDescription(String description) {
        this.mDescription.set(description);
    }

    @Override
    public Transition clone() {
        return new Transition(getSourceState(),getSourceCoefficient(),getSourceDelay(),getOperandState(),getOperandCoefficient(),
                getOperandDelay(),getResultState(),getResultCoefficient(),getProbability(),getType(), getMode(),getDescription());
    }
}
