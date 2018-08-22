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
package population.component;

import population.model.Result;
import population.model.State;
import population.model.Task;
import population.model.Transition;
import population.model.TransitionMode;
import population.model.TransitionType;
import population.util.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Calculator {
    /**
     * ���������� ���������� ������ ����� ����������� � ������������ ������
     * � ������ ���������� ��������
     */
    public static final int HIGHER_ACCURACY_SCALE = 384;
    private final Task mTask; // ������
    private final double[][] mStates; // ���������
    private final BigDecimal[][] mStatesBig; // ��������� ��� ������ ���������� ��������
    private final Lock mStatesLock = new ReentrantLock();
    private final int[] mStateIds; // �������������� ���������
    private final int mStatesCount; // ���������� ���������
    private final ExecutorService mExecutor; // ����������� (��� ������������� ������)
    private final ResultCallback mResultCallback; // �������� ����� ����������
    private final ProgressCallback mProgressCallback; // �������� ����� ��������� ����������
    private final ThreadFactory mThreadFactory; // ������� �������
    private final boolean mPrepareResultsTableData; //  ��������� � ����������� ����
    private volatile double mProgress; // �������� ����������
    private int mCurStep = 1;   // current calculation step (used o����������� ��������� � ��������� ����
    private final boolean mPrepareResultsChartData; // �����������nly in step by step calculation)
    private Integer scale = null;                               // scale with which states will be rounded
    private RoundingMode roundingMode = RoundingMode.HALF_UP;   // rounding mode using with scale
    private double[][] statesRounded = new double[0][0];        // states counts rounded to scale
    private int maxDelay;
    private boolean isTaskStable = false;

    /**
     * �����������
     *
     * @param task                    ������
     * @param prepareResultsTableData ����������� ��������� � ��������� ����
     * @param prepareResultsChartData ����������� ��������� � ����������� ����
     * @param resultCallback          �������� ����� ����������
     * @param progressCallback        �������� ����� ��������� ����������
     * @param threadFactory           ������� ������� ��� ����������� � ������������ ����������
     */
    private Calculator(Task task, boolean prepareResultsTableData, boolean prepareResultsChartData,
                       ResultCallback resultCallback, ProgressCallback progressCallback,
                       ThreadFactory threadFactory) {
        mTask = task;
        mPrepareResultsTableData = prepareResultsTableData;
        mPrepareResultsChartData = prepareResultsChartData;
        mResultCallback = resultCallback;
        mProgressCallback = progressCallback;
        mThreadFactory = threadFactory;
        List<State> statesList = task.getStates();
        int statesCount = statesList.size();
        mStatesCount = statesCount;
        double[][] states = new double[task.getStepsCount()][statesCount];
        statesRounded = new double[task.getStepsCount()][statesCount];
        int[] stateIds = new int[statesCount];
        for (int i = 0; i < statesCount; i++) {
            State state = statesList.get(i);
            states[0][i] = state.getCount();
            stateIds[i] = state.getId();
            statesRounded[0][i] = mTask.getStates().get(i).getCount();
        }
        this.scale = 16;
        mStates = states;
        mStateIds = stateIds;
        this.maxDelay = getMaxDelay();
        if (task.isHigherAccuracy()) {
            BigDecimal[][] statesBig = new BigDecimal[maxDelay + 2][statesCount];
            for (int i = 0; i < statesCount; i++) {
                BigDecimal value = decimalValue(statesList.get(i).getCount());
                statesBig[0][i] = value;
                statesBig[1][i] = value;
            }
            mStatesBig = statesBig;
        } else {
            mStatesBig = null;
        }
        if (task.isParallel()) {
            mExecutor = Utils.newExecutor(threadFactory);
        } else {
            mExecutor = null;
        }
    }

    private int getMaxDelay() {
        int maxDelay = 0;
        for (Transition transition : mTask.getTransitions()) {
            maxDelay = Math.max(maxDelay, transition.getSourceDelay());
            maxDelay = Math.max(maxDelay, transition.getOperandDelay());
        }
        return maxDelay;
    }

    private double getTotalCount(int step) {
        double totalCount = 0;
        for (int state = 0; state < mStatesCount; state++) {
            totalCount += mStates[step][state];
        }
        return totalCount;
    }

    private void copyPreviousStep(int step) {
        System.arraycopy(mStates[step - 1], 0, mStates[step], 0, mStatesCount);
    }

    private BigDecimal getTotalCountBig(int step, int currentStep) {
        BigDecimal totalCount = BigDecimal.ZERO;
        for (int state = 0; state < mStatesCount; state++) {
            totalCount = totalCount.add(mStatesBig[currentStep - step][state]);
        }
        return totalCount;
    }

    private void copyPreviousStepBig(int step, int currentStep) {
        int index = currentStep - step;
        if (index == 0) {
            for (int i = mStatesBig.length - 1; i >= 1; i--) {
                System.arraycopy(mStatesBig[i - 1], 0, mStatesBig[i], 0, mStatesBig[i].length);
            }
        }
        for (int state = 0; state < mStatesCount; state++) {
            BigDecimal value = mStatesBig[index + 1][state];
            mStatesBig[index][state] = value;
            mStates[step][state] = doubleValue(value);
        }
    }

    /**
     * �������� ��������� � ����
     *
     * @param step  ����� ����
     * @param state ������������� ���������
     * @return �������� ���������
     */
    private double getState(int step, int state) {
        mStatesLock.lock();
        try {
            return mStates[step][state];
        } finally {
            mStatesLock.unlock();
        }
    }

    private BigDecimal getStateBig(int step, int currentStep, int state) {
        mStatesLock.lock();
        try {
            return mStatesBig[currentStep - step][state];
        } finally {
            mStatesLock.unlock();
        }
    }

    /**
     * �������� ��������� �� ��������������� � ������������� ��������, ���� ��� ����������
     *
     * @param step  ����� ����
     * @param state ������������� ���������
     */
    private void checkStateNegativeness(int step, int state) {
        if (!mTask.isAllowNegative()) {
            mStatesLock.lock();
            try {
                if (mStates[step][state] < 0) {
                    mStates[step][state] = 0;
                }
            } finally {
                mStatesLock.unlock();
            }
        }
    }

    private void checkStateNegativenessBig(int step, int currentStep, int state) {
        if (!mTask.isAllowNegative()) {
            mStatesLock.lock();
            try {
                int index = currentStep - step;
                if (mStatesBig[index][state].compareTo(BigDecimal.ZERO) < 0) {
                    mStatesBig[index][state] = BigDecimal.ZERO;
                }
                if (mStates[step][state] < 0) {
                    mStates[step][state] = 0;
                }
            } finally {
                mStatesLock.unlock();
            }
        }
    }

    /**
     * ���������� �������� ��������� ��������� �� �������� ���� �� �������� ��������
     *
     * @param step  ����� ����
     * @param state ������������� ���������
     * @param value ��������
     */
    private void incrementState(int step, int state, double value) {
        mStatesLock.lock();
        try {
            mStates[step][state] += value;
        } finally {
            mStatesLock.unlock();
        }
    }

    private void incrementStateBig(int step, int currentStep, int state, BigDecimal value) {
        mStatesLock.lock();
        try {
            int index = currentStep - step;
            BigDecimal result = mStatesBig[index][state].add(value);
            mStatesBig[index][state] = result;
            mStates[step][state] = doubleValue(result);
        } finally {
            mStatesLock.unlock();
        }
    }

    /**
     * ���������� �������� ��������� ��������� �� �������� ���� �� �������� ��������
     *
     * @param step  ����� ����
     * @param state ������������� ���������
     * @param value ��������
     */
    private void decrementState(int step, int state, double value) {
        incrementState(step, state, -value);
    }

    private void decrementStateBig(int step, int currentStep, int state, BigDecimal value) {
        mStatesLock.lock();
        try {
            int index = currentStep - step;
            BigDecimal result = mStatesBig[index][state].subtract(value);
            mStatesBig[index][state] = result;
            mStates[step][state] = doubleValue(result);
        } finally {
            mStatesLock.unlock();
        }
    }

    /**
     * ����� ������� ��������� �� ��������������
     *
     * @param id ������������� ���������
     * @return ������� ���������
     */
    private int findState(int id) {
        if (id == State.EXTERNAL) {
            return State.EXTERNAL;
        }
        for (int i = 0; i < mStateIds.length; i++) {
            if (mStateIds[i] == id) {
                return i;
            }
        }
        return -1;
    }

    private void clearBigStates() {
        for (int i = 0; i < mStatesBig.length; i++) {
            for (int j = 0; j < mStatesBig[i].length; j++) {
                mStatesBig[i][j] = null;
            }
            mStatesBig[i] = null;
        }
    }

    /**
     * ���������� ��������� ������ ����������, ���� �� �����
     *
     * @param result ����������
     */
    private void callbackResults(Result result) {
        ResultCallback resultCallback = mResultCallback;
        if (resultCallback != null) {
            resultCallback.onResult(result);
        }
    }

    /**
     * ���������� ��������� ������ ��������� ����������, ���� ��� ���������� � �� �����
     *
     * @param step ����� ����
     */
    private void callbackProgress(int step) {
        if (mProgressCallback == null) {
            return;
        }
        double progress;
        boolean needUpdate;
        int stepsCount = mTask.getStepsCount();
        if (step == 0 || stepsCount == 0) {
            progress = 0;
            needUpdate = true;
        } else if (step == stepsCount - 1 || stepsCount == 1) {
            progress = 1;
            needUpdate = true;
        } else {
            progress = (double) step / (double) (stepsCount - 1);
            needUpdate = progress - mProgress > 0.005;
        }
        if (needUpdate) {
            mProgress = progress;
            mProgressCallback.onProgressUpdate(progress);
        }
    }

    /**
     * ���������� � ������� ���������
     */
    private Result calculateNormalAccuracy() {
        callbackProgress(0);
        List<Transition> transitions = mTask.getTransitions();
        int stepsCount = mTask.getStepsCount();
        if (mTask.isParallel()) {
            List<Future<?>> futures = new ArrayList<>(transitions.size());
            for (int step = 1; step < stepsCount; step++) {
                copyPreviousStep(step);
                double totalCount = getTotalCount(step);
                for (Transition transition : transitions) {
                    futures.add(mExecutor
                            .submit(new TransitionActionNormalAccuracy(step, totalCount,
                                    transition)));
                }
                for (Future<?> future : futures) {
                    await(future);
                }
                futures.clear();
                for (int stateId = 0; stateId < mStateIds.length; stateId++) {
                    checkStateNegativeness(step, stateId);
                }

                if (scale != null) {
                    roundStates(step, scale);
                }

                callbackProgress(step);
            }
        } else {
            for (int step = 1; step < stepsCount; step++) {
                copyPreviousStep(step);
                double totalCount = getTotalCount(step);
                for (Transition transition : transitions) {
                    transitionNormalAccuracy(step, totalCount, transition);
                }
                for (int stateId = 0; stateId < mStateIds.length; stateId++) {
                    checkStateNegativeness(step, stateId);
                }

                if (scale != null) {
                    roundStates(step, scale);
                }

                callbackProgress(step);
            }
        }
        return new Result(mTask.getStartPoint(), scale == null ? mStates : statesRounded, mTask.getStates(),
                mPrepareResultsTableData, mPrepareResultsChartData);
    }

    /**
     * round states count in mStates on step with given precision
     * @param step  step
     * @param scale precision
     */
    private void roundStates(int step, Integer scale) {
        if (!isTaskStable && step >= this.maxDelay + 2) {
            boolean isStable = true;
            if (step >= this.maxDelay + 2) {
                cycle:
                for (int i = 0; i < statesRounded[step].length; i++) {
                    double val = statesRounded[step - maxDelay - 2][i];
                    for (int j = 0; j <= maxDelay; j++) {
                        if (statesRounded[step - j - 1][i] != val) {
                            isStable = false;
                            break cycle;
                        }
                    }
                }
            } else {
                isStable = false;
            }
            isTaskStable = isStable;
        }

        if (isTaskStable) {
            for (int i = 0 ; i < statesRounded[step].length; i++) {
                statesRounded[step][i] = statesRounded[step - 1][i];
            }
        } else {
            for (int i = 0; i < mStates[step].length; i++) {
                if (mStates[step][i] <= Math.pow(10., -scale)) {
                    mStates[step][i] = 0;
                }
                if (mTask.isHigherAccuracy() || scale != Utils.MAX_PRECISION) {
                    statesRounded[step][i] = new BigDecimal(mStates[step][i]).setScale(scale, roundingMode).doubleValue();
                } else {
                    statesRounded[step][i] = mStates[step][i];
                }
            }
        }
    }

    /**
     * round last states in higher accuracy mode
     * @param scale precision
     */
    private void roundLastBigStates(Integer scale) {
        if (mStatesBig.length > 0) {
            for (int i = 0; i < mStatesBig[0].length; i++) {
                if (mStatesBig[mStatesBig.length - 1][i].compareTo(new BigDecimal(Math.pow(10., -scale))) <= 0) {
                    mStatesBig[mStatesBig.length - 1][i] = BigDecimal.ZERO;
                } else {
                    mStatesBig[mStatesBig.length - 1][i] = mStatesBig[mStatesBig.length - 1][i]
                            .setScale(scale, roundingMode);
                }
            }
        }
    }

    /**
     * ���������� � ���������� ���������
     */
    private Result calculateHigherAccuracy() {
        callbackProgress(0);
        List<Transition> transitions = mTask.getTransitions();
        int stepsCount = mTask.getStepsCount();
        if (mTask.isParallel()) {
            List<Future<?>> futures = new ArrayList<>(transitions.size());
            for (int step = 1; step < stepsCount; step++) {
                copyPreviousStepBig(step, step);
                BigDecimal totalCount = getTotalCountBig(step, step);
                for (Transition transition : transitions) {
                    futures.add(mExecutor
                            .submit(new TransitionActionHigherAccuracy(step, totalCount,
                                    transition)));
                }
                for (Future<?> future : futures) {
                    await(future);
                }
                futures.clear();
                for (int stateId = 0; stateId < mStateIds.length; stateId++) {
                    checkStateNegativenessBig(step, step, stateId);
                }

                if (scale != null) {
                    roundLastBigStates(scale);
                    roundStates(step, scale);
                }

                callbackProgress(step);
            }
        } else {
            for (int step = 1; step < stepsCount; step++) {
                copyPreviousStepBig(step, step);
                BigDecimal totalCount = getTotalCountBig(step, step);
                for (Transition transition : transitions) {
                    transitionHigherAccuracy(step, totalCount, transition);
                }

                if (scale != null) {
                    roundLastBigStates(scale);
                    roundStates(step, scale);
                }

                callbackProgress(step);
            }
        }
        clearBigStates();
        return new Result(mTask.getStartPoint(), scale == null ? mStates : statesRounded, mTask.getStates(),
                mPrepareResultsTableData, mPrepareResultsChartData);
    }

    /**
     * ���������� �������� � ������� ���������
     *
     * @param step       ����� ����
     * @param totalCount ����� ���������� ��������� �� ������� ����
     * @param transition �������
     */
    private void transitionNormalAccuracy(int step, double totalCount, Transition transition) {
        int sourceState = findState(transition.getSourceState());
        int operandState = findState(transition.getOperandState());
        int resultState = findState(transition.getResultState());
        boolean sourceExternal = isStateExternal(sourceState);
        boolean operandExternal = isStateExternal(operandState);
        boolean resultExternal = isStateExternal(resultState);
        if (sourceExternal && operandExternal) {
            return;
        }
        int sourceIndex = delay(step - 1, transition.getSourceDelay());
        int operandIndex = delay(step - 1, transition.getOperandDelay());
        int transitionType = transition.getType();
        int transitionMode = transition.getMode();
        double sourceCoefficient = transition.getSourceCoefficient();
        double operandCoefficient = transition.getOperandCoefficient();
        double probability = transition.getProbability();
        double value = 0;
        if (transitionType == TransitionType.LINEAR) {
            if (sourceExternal) {
                double operandDensity = applyCoefficientLinear(getState(operandIndex, operandState),
                        operandCoefficient);
                value = operandDensity * probability;
                if (transitionMode == TransitionMode.RESIDUAL) {
                    value = operandDensity - value * operandCoefficient;
                }
            } else if (operandExternal) {
                value = applyCoefficientLinear(getState(sourceIndex, sourceState),
                        sourceCoefficient) * probability;
            } else if (sourceState == operandState) {
                double density = applyCoefficientLinear(getState(sourceIndex, sourceState),
                        Math.max(sourceCoefficient, operandCoefficient));
                value = applyTransitionCommon(density, density, transition);
            } else {
                double sourceDensity = applyCoefficientLinear(getState(sourceIndex, sourceState),
                        sourceCoefficient);
                double operandDensity = applyCoefficientLinear(getState(operandIndex, operandState),
                        operandCoefficient);
                value = applyTransitionCommon(Math.min(sourceDensity, operandDensity),
                        operandDensity, transition);
            }
        } else if (transitionType == TransitionType.SOLUTE) {
            if (totalCount > 0) {
                if (sourceExternal) {
                    double operandDensity =
                            applyCoefficientPower(getState(operandIndex, operandState),
                                    operandCoefficient);
                    value = operandDensity;
                    if (operandCoefficient > 1) {
                        value /= Math.pow(totalCount, operandCoefficient - 1);
                    }
                    value = applyTransitionCommon(value, operandDensity, transition);
                } else if (operandExternal) {
                    value = applyCoefficientPower(getState(sourceIndex, sourceState),
                            sourceCoefficient);
                    if (sourceCoefficient > 1) {
                        value /= Math.pow(totalCount, sourceCoefficient - 1);
                    }
                    value *= probability;
                } else if (sourceState == operandState) {
                    double density = applyCoefficientPower(getState(sourceIndex, sourceState),
                            sourceCoefficient + operandCoefficient);
                    value = density /
                            Math.pow(totalCount, sourceCoefficient + operandCoefficient - 1);
                    double operandDensity = applyCoefficientPower(getState(operandIndex, operandState),
                            operandCoefficient);
                    value = applyTransitionCommon(value, operandDensity, transition);
                } else {
                    double sourceDensity = applyCoefficientPower(getState(sourceIndex, sourceState),
                            sourceCoefficient);
                    double operandDensity =
                            applyCoefficientPower(getState(operandIndex, operandState),
                                    operandCoefficient);
                    value = sourceDensity * operandDensity /
                            Math.pow(totalCount, sourceCoefficient + operandCoefficient - 1);
                    value = applyTransitionCommon(value, operandDensity, transition);
                }
            }
        } else if (transitionType == TransitionType.BLEND) {
            if (sourceExternal) {
                double operandCount = getState(operandIndex, operandState);
                if (operandCount > 0) {
                    double operandDensity = applyCoefficientPower(operandCount, operandCoefficient);
                    value = operandDensity;
                    if (operandCoefficient > 1) {
                        value /= Math.pow(operandCount, operandCoefficient - 1);
                    }
                    value = applyTransitionCommon(value, operandDensity, transition);
                }
            } else if (operandExternal) {
                double sourceCount = getState(sourceIndex, sourceState);
                if (sourceCount > 0) {
                    value = applyCoefficientPower(sourceCount, sourceCoefficient);
                    if (sourceCoefficient > 1) {
                        value /= Math.pow(sourceCount, sourceCoefficient - 1);
                    }
                    value *= probability;
                }
            } else if (sourceState == operandState) {
                double count = getState(sourceIndex, sourceState);
                if (count > 0) {
                    double density =
                            applyCoefficientPower(count, sourceCoefficient + operandCoefficient);
                    value = density / Math.pow(count, sourceCoefficient + operandCoefficient - 1);
                    double operandDensity = applyCoefficientPower(getState(operandIndex, operandState),
                            operandCoefficient);
                    value = applyTransitionCommon(value, operandDensity, transition);
                }
            } else {
                double sourceCount = getState(sourceIndex, sourceState);
                double operandCount = getState(operandIndex, operandState);
                double sum = sourceCount + operandCount;
                if (sum > 0) {
                    double sourceDensity = applyCoefficientPower(sourceCount, sourceCoefficient);
                    double operandDensity = applyCoefficientPower(operandCount, operandCoefficient);
                    value = sourceDensity * operandDensity /
                            Math.pow(sum, sourceCoefficient + operandCoefficient - 1);
                    value = applyTransitionCommon(value, operandDensity, transition);
                }
            }
        }
        if (!sourceExternal && transitionMode == TransitionMode.REMOVING) {
            decrementState(step, sourceState, value * sourceCoefficient);
        }
        if (!operandExternal &&
                !(transitionMode == TransitionMode.REMOVING && sourceState == operandState)
                ) {
            if (transitionMode == TransitionMode.INHIBITOR ||
                    transitionMode == TransitionMode.RESIDUAL) {
                decrementState(step, operandState, value);
            } else if (transitionMode != TransitionMode.RETAINING) {
                decrementState(step, operandState, value * operandCoefficient);
            }
        }
        if (!resultExternal) {
            incrementState(step, resultState, value * transition.getResultCoefficient());
        }
    }

    /**
     * ���������� �������� � ���������� ���������
     *
     * @param step       ����� ����
     * @param totalCount ����� ���������� ��������� �� ������� ����
     * @param transition �������
     */
    private void transitionHigherAccuracy(int step, BigDecimal totalCount, Transition transition) {
        int sourceState = findState(transition.getSourceState());
        int operandState = findState(transition.getOperandState());
        int resultState = findState(transition.getResultState());
        boolean sourceExternal = isStateExternal(sourceState);
        boolean operandExternal = isStateExternal(operandState);
        boolean resultExternal = isStateExternal(resultState);
        if (sourceExternal && operandExternal) {
            return;
        }
        int sourceIndex = delay(step - 1, transition.getSourceDelay());
        int operandIndex = delay(step - 1, transition.getOperandDelay());
        int transitionType = transition.getType();
        int transitionMode = transition.getMode();
        double sourceCoefficient = transition.getSourceCoefficient();
        double operandCoefficient = transition.getOperandCoefficient();
        double probability = transition.getProbability();
        BigDecimal value = BigDecimal.ZERO;
        if (transitionType == TransitionType.LINEAR) {
            if (sourceExternal) {
                BigDecimal operandDensity =
                        applyCoefficientLinear(getStateBig(operandIndex, step, operandState),
                                operandCoefficient);
                value = multiply(operandDensity, decimalValue(probability));
                if (transitionMode == TransitionMode.RESIDUAL) {
                    value = operandDensity
                            .subtract(multiply(value, decimalValue(operandCoefficient)));
                }
            } else if (operandExternal) {
                value = multiply(applyCoefficientLinear(getStateBig(sourceIndex, step, sourceState),
                        sourceCoefficient), decimalValue(probability));
            } else if (sourceState == operandState) {
                BigDecimal density =
                        applyCoefficientLinear(getStateBig(sourceIndex, step, sourceState),
                                sourceCoefficient + operandCoefficient - 1);
                value = applyTransitionCommon(density, density, transition);
            } else {
                BigDecimal sourceDensity =
                        applyCoefficientLinear(getStateBig(sourceIndex, step, sourceState),
                                sourceCoefficient);
                BigDecimal operandDensity =
                        applyCoefficientLinear(getStateBig(operandIndex, step, operandState),
                                operandCoefficient);
                value = applyTransitionCommon(sourceDensity.min(operandDensity), operandDensity,
                        transition);
            }
        } else if (transitionType == TransitionType.SOLUTE) {
            if (totalCount.compareTo(BigDecimal.ZERO) > 0) {
                if (sourceExternal) {
                    BigDecimal operandDensity =
                            applyCoefficientPower(getStateBig(operandIndex, step, operandState),
                                    operandCoefficient);
                    value = operandDensity;
                    if (operandCoefficient > 1) {
                        value = divide(value, power(totalCount, operandCoefficient - 1));
                    }
                    value = applyTransitionCommon(value, operandDensity, transition);
                } else if (operandExternal) {
                    value = applyCoefficientPower(getStateBig(sourceIndex, step, sourceState),
                            sourceCoefficient);
                    if (sourceCoefficient > 1) {
                        value = divide(value, power(totalCount, sourceCoefficient - 1));
                    }
                    value = multiply(value, decimalValue(probability));
                } else if (sourceState == operandState) {
                    BigDecimal density =
                            applyCoefficientPower(getStateBig(sourceIndex, step, sourceState),
                                    sourceCoefficient + operandCoefficient);
                    value = divide(density,
                            power(totalCount, sourceCoefficient + operandCoefficient - 1));
                    value = applyTransitionCommon(value, density, transition);
                } else {
                    BigDecimal sourceDensity =
                            applyCoefficientPower(getStateBig(sourceIndex, step, sourceState),
                                    sourceCoefficient);
                    BigDecimal operandDensity =
                            applyCoefficientPower(getStateBig(operandIndex, step, operandState),
                                    operandCoefficient);
                    value = divide(multiply(sourceDensity, operandDensity),
                            power(totalCount, sourceCoefficient + operandCoefficient - 1));
                    value = applyTransitionCommon(value, operandDensity, transition);
                }
            }
        } else if (transitionType == TransitionType.BLEND) {
            if (sourceExternal) {
                BigDecimal operandCount = getStateBig(operandIndex, step, operandState);
                if (operandCount.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal operandDensity =
                            applyCoefficientPower(operandCount, operandCoefficient);
                    value = operandDensity;
                    if (operandCoefficient > 1) {
                        value = divide(value, power(operandCount, operandCoefficient - 1));
                    }
                    value = applyTransitionCommon(value, operandDensity, transition);
                }
            } else if (operandExternal) {
                BigDecimal sourceCount = getStateBig(sourceIndex, step, sourceState);
                if (sourceCount.compareTo(BigDecimal.ZERO) > 0) {
                    value = applyCoefficientPower(sourceCount, sourceCoefficient);
                    if (sourceCoefficient > 1) {
                        value = divide(value, power(sourceCount, sourceCoefficient - 1));
                    }
                    value = multiply(value, decimalValue(probability));
                }
            } else if (sourceState == operandState) {
                BigDecimal count = getStateBig(sourceIndex, step, sourceState);
                if (count.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal density =
                            applyCoefficientPower(count, sourceCoefficient + operandCoefficient);
                    value = divide(density,
                            power(count, sourceCoefficient + operandCoefficient - 1));
                    value = applyTransitionCommon(value, density, transition);
                }
            } else {
                BigDecimal sourceCount = getStateBig(sourceIndex, step, sourceState);
                BigDecimal operandCount = getStateBig(operandIndex, step, operandState);
                BigDecimal sum = sourceCount.add(operandCount);
                if (sum.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal sourceDensity =
                            applyCoefficientPower(sourceCount, sourceCoefficient);
                    BigDecimal operandDensity =
                            applyCoefficientPower(operandCount, operandCoefficient);
                    value = divide(multiply(sourceDensity, operandDensity),
                            power(sum, sourceCoefficient + operandCoefficient - 1));
                    value = applyTransitionCommon(value, operandDensity, transition);
                }
            }
        }
        if (!sourceExternal && transitionMode == TransitionMode.REMOVING) {
            decrementStateBig(step, step, sourceState,
                    multiply(value, decimalValue(sourceCoefficient)));
        }
        if (!operandExternal) {
            if (transitionMode == TransitionMode.INHIBITOR ||
                    transitionMode == TransitionMode.RESIDUAL) {
                decrementStateBig(step, step, operandState, value);
            } else if (transitionMode != TransitionMode.RETAINING) {
                decrementStateBig(step, step, operandState,
                        multiply(value, decimalValue(operandCoefficient)));
            }
        }
        if (!resultExternal) {
            incrementStateBig(step, step, resultState,
                    multiply(value, decimalValue(transition.getResultCoefficient())));
        }
    }

    /**
     * ���������� �������� ���������
     *
     * @return ���������� ����������
     */
    public Result calculateSync() {
        Result result;
        if (mTask.isHigherAccuracy()) {
            result = calculateHigherAccuracy();
        } else {
            result = calculateNormalAccuracy();
        }
        callbackResults(result);
        return result;
    }

    /**
     * ���������� �������� ����������
     */
    public void calculateAsync() {
        mThreadFactory.newThread(() -> {
            Result result;
            if (mTask.isHigherAccuracy()) {
                result = calculateHigherAccuracy();
            } else {
                result = calculateNormalAccuracy();
            }
            callbackResults(result);
        }).start();
    }

    /**
     * ��������� ���������� ������
     *
     * @param future ������ {@link Future} ������ ������
     */
    private static void await(Future<?> future) {
        try {
            future.get();
        } catch (InterruptedException | CancellationException ignored) {
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    /**
     * ���������� ��������
     *
     * @param step  ����� ����
     * @param delay ��������
     * @return ����� ���� � ���������
     */
    private static int delay(int step, int delay) {
        if (step > delay) {
            return step - delay;
        } else {
            return 0;
        }
    }

    /**
     * ���������� ���������� ������������
     */
    private static double applyCoefficientPower(double u, double coefficient) {
        if (coefficient <= 1) {
            return u;
        }
        return Math.pow(u, coefficient) / probabilisticFactorial(coefficient);
    }

    /**
     * ���������� ��������� ������������
     */
    private static double applyCoefficientLinear(double u, double coefficient) {
        if (coefficient <= 1) {
            return u;
        }
        return u / coefficient;
    }

    /**
     * �����������, �������� ��������� ������� ��� ���
     */
    private static boolean isStateExternal(int stateId) {
        return stateId == State.EXTERNAL;
    }

    /**
     * ���������� ���������� ������������
     */
    private static BigDecimal applyCoefficientPower(BigDecimal u, double coefficient) {
        if (coefficient <= 1) {
            return u;
        }
        return divide(power(u, coefficient), probabilisticFactorialBig(coefficient));
    }

    /**
     * ���������� ��������� ������������
     */
    private static BigDecimal applyCoefficientLinear(BigDecimal u, double coefficient) {
        if (coefficient <= 1) {
            return u;
        }
        return divide(u, decimalValue(coefficient));
    }

    /**
     * ���������� �������� �������� ��������
     */
    private static BigDecimal applyTransitionCommon(BigDecimal u, BigDecimal operandDensity,
                                                    Transition transition) {
        int mode = transition.getMode();
        if (mode == TransitionMode.INHIBITOR) {
            u = operandDensity
                    .subtract(multiply(u, decimalValue(transition.getOperandCoefficient())));
        }
        u = multiply(u, decimalValue(transition.getProbability()));
        if (mode == TransitionMode.RESIDUAL) {
            u = operandDensity
                    .subtract(multiply(u, decimalValue(transition.getOperandCoefficient())));
        }
        return u;
    }

    /**
     * ���������� �������� �������� ��������
     */
    private static double applyTransitionCommon(double u, double operandDensity,
                                                Transition transition) {
        int mode = transition.getMode();
        if (mode == TransitionMode.INHIBITOR) {
            u = operandDensity - u * transition.getOperandCoefficient();
        }
        u *= transition.getProbability();
        if (mode == TransitionMode.RESIDUAL) {
            u = operandDensity - u * transition.getOperandCoefficient();
        }
        return u;
    }

    /**
     * ������� � ��������� ��-��������� ��� ������ ���������� ��������
     *
     * @param u �������
     * @param v ��������
     * @return �������
     */
    private static BigDecimal divide(BigDecimal u, BigDecimal v) {
        return divide(u, v, HIGHER_ACCURACY_SCALE);
    }

    /**
     * ��������� � ��������� ��-��������� ��� ������ ���������� ��������
     *
     * @param u ���������
     * @param v ���������
     * @return ������������
     */
    private static BigDecimal multiply(BigDecimal u, BigDecimal v) {
        return multiply(u, v, HIGHER_ACCURACY_SCALE);
    }

    /**
     * ���������� � ������� � ��������� ��-��������� ��� ������ ���������� ��������
     *
     * @param u        ���������
     * @param exponent ����������
     * @return ���������
     */
    private static BigDecimal power(BigDecimal u, double exponent) {
        return power(u, exponent, HIGHER_ACCURACY_SCALE);
    }

    private static BigDecimal exponent0(BigDecimal u, int scale) {
        BigDecimal a = BigDecimal.ONE;
        BigDecimal b = u;
        BigDecimal c = u.add(BigDecimal.ONE);
        BigDecimal d;
        for (int i = 2; ; i++) {
            b = multiply(b, u, scale);
            a = a.multiply(BigDecimal.valueOf(i));
            BigDecimal e = divide(b, a, scale);
            d = c;
            c = c.add(e);
            if (c.compareTo(d) == 0) {
                break;
            }
        }
        return c;
    }

    private static BigDecimal naturalLogarithm0(BigDecimal u, int scale) {
        int s = scale + 1;
        BigDecimal a = u;
        BigDecimal b;
        BigDecimal c = decimalValue(5).movePointLeft(s);
        for (; ; ) {
            BigDecimal d = exponent(u, s);
            b = d.subtract(a).divide(d, s, RoundingMode.DOWN);
            u = u.subtract(b);
            if (b.compareTo(c) <= 0) {
                break;
            }
        }
        return u.setScale(scale, RoundingMode.HALF_EVEN);
    }

    /**
     * �������������� int � BigDecimal
     *
     * @param u �������� �������� ���� int
     * @return ��������� ���� BigDecimal
     */
    public static BigDecimal decimalValue(int u) {
        return new BigDecimal(u);
    }

    /**
     * �������������� long � BigDecimal
     *
     * @param u �������� �������� ���� long
     * @return ��������� ���� BigDecimal
     */
    public static BigDecimal decimalValue(long u) {
        return new BigDecimal(u);
    }

    /**
     * �������������� double � BigDecimal
     *
     * @param u �������� �������� ���� double
     * @return ��������� ���� BigDecimal
     */
    public static BigDecimal decimalValue(double u) {
        return new BigDecimal(u);
    }

    /**
     * �������������� BigDecimal � double
     *
     * @param u �������� �������� ���� BigDecimal
     * @return ���������
     */
    public static double doubleValue(BigDecimal u) {
        return u.doubleValue();
    }

    /**
     * ������������� ���������.
     * ��������� ������������� ����� ��� �������������� ��������
     * �� ����������� ���� �������� �����.
     *
     * @param u �������� ��������
     * @return ���������
     */
    public static double probabilisticFactorial(double u) {
        double result = 1;
        double r = u % 1;
        if (r > 0) {
            double v = Math.floor(u);
            for (double i = 2; i <= v; i++) {
                result *= i;
            }
            result = result * (1 - r) + result * (v + 1) * r;
        } else {
            for (double i = 2; i <= u; i++) {
                result *= i;
            }
        }
        return result;
    }

    /**
     * ������������� ���������.
     * ��������� ������������� ����� ��� �������������� ��������
     * �� ����������� ���� �������� �����.
     *
     * @param u     �������� ��������
     * @param scale ���������� ������ � ������� ����� ����������
     * @return ���������
     */
    public static BigDecimal probabilisticFactorialBig(double u, int scale) {
        BigDecimal result = BigDecimal.ONE;
        double r = u % 1;
        if (r > 0) {
            double v = Math.floor(u);
            for (double i = 2; i <= v; i++) {
                result = result.multiply(decimalValue(i));
            }
            result = result.multiply(BigDecimal.ONE.subtract(decimalValue(r)))
                    .add(result.multiply(decimalValue(v).add(BigDecimal.ONE))
                            .multiply(decimalValue(r)));
        } else {
            for (double i = 2; i <= u; i++) {
                result = result.multiply(decimalValue(i));
            }
        }
        return result.setScale(scale, RoundingMode.HALF_EVEN);
    }

    private static BigDecimal probabilisticFactorialBig(double u) {
        return probabilisticFactorialBig(u, HIGHER_ACCURACY_SCALE);
    }

    /**
     * �������
     *
     * @param u     �������
     * @param v     ��������
     * @param scale ���������� ������ � ������� ����� ����������
     * @return �������
     */
    public static BigDecimal divide(BigDecimal u, BigDecimal v, int scale) {
        return u.divide(v, scale, RoundingMode.HALF_EVEN);
    }

    /**
     * ���������
     *
     * @param u     ���������
     * @param v     ���������
     * @param scale ���������� ������ � ������� ����� ����������
     * @return ������������
     */
    public static BigDecimal multiply(BigDecimal u, BigDecimal v, int scale) {
        return u.multiply(v).setScale(scale, RoundingMode.HALF_EVEN);
    }

    /**
     * ���������� � ������������� �������
     *
     * @param u        ���������
     * @param exponent ����������
     * @param scale    ���������� ������ � ������� ����� ����������
     * @return ���������
     */
    public static BigDecimal power(BigDecimal u, long exponent, int scale) {
        if (u.signum() == 0) {
            return BigDecimal.ZERO;
        }
        if (exponent < 0) {
            return BigDecimal.ONE.divide(power(u, -exponent, scale), scale, RoundingMode.HALF_EVEN);
        }
        BigDecimal p = BigDecimal.ONE;
        for (; exponent > 0; exponent >>= 1) {
            if ((exponent & 1) == 1) {
                p = p.multiply(u).setScale(scale, RoundingMode.HALF_EVEN);
            }
            u = u.multiply(u).setScale(scale, RoundingMode.HALF_EVEN);
        }
        return p;
    }

    /**
     * ���������� � ������������ �������
     *
     * @param u        ���������
     * @param exponent ����������
     * @param scale    ���������� ������ � ������� ����� ����������
     * @return ���������
     */
    public static BigDecimal power(BigDecimal u, double exponent, int scale) {
        if (u.signum() == 0) {
            return BigDecimal.ZERO;
        }
        if (exponent % 1 == 0 && exponent <= Long.MAX_VALUE) {
            return power(u, (long) exponent, scale);
        }
        return exponent(decimalValue(exponent).multiply(naturalLogarithm(u, scale)), scale);
    }

    /**
     * ���������� �������������� �����
     *
     * @param u     �������� ��������
     * @param index ������� �����
     * @param scale ���������� ������ � ������� ����� ����������
     * @return ���������
     */
    public static BigDecimal root(BigDecimal u, long index, int scale) {
        if (u.signum() == 0) {
            return BigDecimal.ZERO;
        }
        int s = scale + 1;
        BigDecimal a = u;
        BigDecimal b = decimalValue(index);
        BigDecimal c = decimalValue(index - 1);
        BigDecimal d = decimalValue(5).movePointLeft(s);
        BigDecimal e;
        u = divide(u, b, scale);
        for (; ; ) {
            BigDecimal f = power(u, index - 1, s);
            BigDecimal g = multiply(u, f, s);
            BigDecimal h = a.add(c.multiply(g)).setScale(s, RoundingMode.HALF_EVEN);
            BigDecimal l = multiply(b, f, s);
            e = u;
            u = h.divide(l, s, RoundingMode.DOWN);
            if (u.subtract(e).abs().compareTo(d) <= 0) {
                break;
            }
        }
        return u;
    }

    /**
     * ���������� ����� ������ � ��������� �������
     *
     * @param u     �������� ��������
     * @param scale ���������� ������ � ������� ����� ����������
     * @return ���������
     */
    public static BigDecimal exponent(BigDecimal u, int scale) {
        if (u.signum() == 0) {
            return BigDecimal.ONE;
        } else if (u.signum() == -1) {
            return divide(BigDecimal.ONE, exponent(u.negate(), scale));
        }
        BigDecimal a = u.setScale(0, RoundingMode.DOWN);
        if (a.signum() == 0) {
            return exponent0(u, scale);
        }
        BigDecimal b = u.subtract(a);
        BigDecimal c = BigDecimal.ONE.add(divide(b, a, scale));
        BigDecimal d = exponent0(c, scale);
        BigDecimal e = decimalValue(Long.MAX_VALUE);
        BigDecimal f = BigDecimal.ONE;
        for (; a.compareTo(e) >= 0; ) {
            f = multiply(f, power(d, Long.MAX_VALUE, scale), scale);
            a = a.subtract(e);
        }
        return multiply(f, power(d, a.longValue(), scale), scale);
    }

    /**
     * ���������� ������������ ��������� �� ���������� ��������
     *
     * @param u     �������� ��������
     * @param scale ���������� ������ � ������� ����� ����������
     * @return ���������
     */
    public static BigDecimal naturalLogarithm(BigDecimal u, int scale) {
        if (u.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Natural logarithm is defined only on positive values.");
        }
        int a = u.toString().length() - u.scale() - 1;
        if (a < 3) {
            return naturalLogarithm0(u, scale);
        } else {
            BigDecimal b = root(u, a, scale);
            BigDecimal c = naturalLogarithm0(b, scale);
            return multiply(decimalValue(a), c, scale);
        }
    }

    /**
     * ������������ ������� � ��������� ��������
     *
     * @param start      ������
     * @param end        �����
     * @param resultSize ������ ����������
     * @return ���������
     */
    public static int[] interpolateIndexes(int start, int end, int resultSize) {
        int[] array = new int[resultSize];
        for (int i = 0; i < resultSize; ++i) {
            array[i] = (int) Math.round(interpolate(start, end, i / (double) resultSize));
        }
        return array;
    }

    /**
     * �������� ������������
     */
    public static double interpolate(double u, double v, double f) {
        return u * (1D - f) + v * f;
    }

    /**
     * ���������� (���������)
     *
     * @param task                    ������
     * @param prepareResultsTableData ����������� ��������� ��� ������ � ��������� ����
     * @param prepareResultsChartData ����������� ��������� ��� ������ � ����������� ����
     * @param threadFactory           ������� ������� ��� ����������� � ������������ ����������
     * @return ���������� ����������
     */
    public static Result calculateSync(Task task, boolean prepareResultsTableData,
                                       boolean prepareResultsChartData, ThreadFactory threadFactory) {
        return new Calculator(task, prepareResultsTableData, prepareResultsChartData, null, null,
                threadFactory).calculateSync();
    }

    /**
     * ���������� (����������)
     *
     * @param task                    ������
     * @param prepareResultsTableData ����������� ��������� ��� ������ � ��������� ����
     * @param prepareResultsChartData ����������� ��������� ��� ������ � ����������� ����
     * @param resultCallback          �������� ����� ����������
     * @param progressCallback        �������� ����� ��������� ����������
     * @param threadFactory           ������� ������� ��� ����������� � ������������ ����������
     */
    public static void calculateAsync(Task task, boolean prepareResultsTableData,
                                      boolean prepareResultsChartData, ResultCallback resultCallback,
                                      ProgressCallback progressCallback, ThreadFactory threadFactory) {
        new Calculator(task, prepareResultsTableData, prepareResultsChartData, resultCallback,
                progressCallback, threadFactory).calculateAsync();
    }


    public static void calculateAsyncWithPrecision(Task task, boolean prepareResultsTableData,
                                                   boolean prepareResultsChartData, ResultCallback resultCallback,
                                                   ProgressCallback progressCallback, ThreadFactory threadFactory) {
        new Calculator(task, prepareResultsTableData, prepareResultsChartData, resultCallback,
                progressCallback, threadFactory).calculateAsync();
    }


    /**
     * calculate taskAnalyser until canPredictDominantStates() or reached max steps count
     * @param taskAnalyser
     * @param progressCallback
     * @param resultCallback
     * @param threadFactory
     * @param completeCallback
     */
    public static void calculateTaskAnalyser(
            TaskAnalyser taskAnalyser,
            ThreadFactory threadFactory,
            ResultCallback resultCallback,
            ProgressCallback progressCallback,
            CompleteCallback completeCallback
    ) {
        new Calculator(taskAnalyser.getTask(),false, false,
                resultCallback, progressCallback, threadFactory)
                .calculateTaskAnalyser(taskAnalyser, completeCallback);
    }


    private void calculateTaskAnalyser(TaskAnalyser taskAnalyser, CompleteCallback completeCallback) {
        boolean higherAccuracy = mTask.isHigherAccuracy();
        if (higherAccuracy)
            taskAnalyser.updateHigherAccuracyStatesCount(mStatesBig);
        taskAnalyser.setCalculatedStates(statesRounded);

        do {
            int steps = Math.min(mTask.getStepsCount() - mCurStep, 100);
            if (higherAccuracy) {
                for (int i = 0; i < steps; i++) {
                    calculateHigherAccuracySteps(steps);
                    taskAnalyser.updateHigherAccuracyStatesCount(mStatesBig);
                    //mStatesBig[0][0].setScale(1000);
                }
            }
            else {
                calculateNormalAccuracySteps(steps);
            }

            taskAnalyser.update(mCurStep);
        } while (!taskAnalyser.canPredictDominantStates() && mCurStep < mTask.getStepsCount());

        taskAnalyser.update(mCurStep);
        completeCallback.onComplete(null);
    }


    /**
     * calculateParametricPortrait stepsCnt steps from mCurStep
     * @param steps steps count to calculateParametricPortrait
     */
    public void calculateNormalAccuracySteps(int steps) {
        callbackProgress(mCurStep);
        List<Transition> transitions = mTask.getTransitions();
        int stepsCount = Math.min(mCurStep + steps, mTask.getStepsCount());

        if (mTask.isParallel()) {
            List<Future<?>> futures = new ArrayList<>(transitions.size());
            for (int step = mCurStep; step < stepsCount; step++) {
                copyPreviousStep(step);
                double totalCount = getTotalCount(step);

                for (Transition transition : transitions) {
                    futures.add(mExecutor
                            .submit(new TransitionActionNormalAccuracy(step, totalCount,
                                    transition)));
                }

                for (Future<?> future : futures) {
                    try {
                        future.get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }
                futures.clear();

                for (int stateId = 0; stateId < mStateIds.length; stateId++) {
                    checkStateNegativeness(step, stateId);
                }

                if (scale != null) {
                    roundStates(step, scale);
                }

                callbackProgress(step);

                mCurStep++;
            }
        } else {
            for (int step = mCurStep; step < stepsCount; step++) {
                copyPreviousStep(step);
                double totalCount = getTotalCount(step);

                for (Transition transition : transitions) {
                    transitionNormalAccuracy(step, totalCount, transition);
                }
                for (int stateId = 0; stateId < mStateIds.length; stateId++) {
                    checkStateNegativeness(step, stateId);
                }

                if (scale != null) {
                    roundStates(step, scale);
                }

                callbackProgress(step);

                mCurStep++;
            }
        }
    }

    /**
     * calculateParametricPortrait stepsCnt steps from mCurStep with higher accuracy
     * @param steps steps count to calculateParametricPortrait
     */
    private void calculateHigherAccuracySteps(int steps) {
        callbackProgress(mCurStep);
        List<Transition> transitions = mTask.getTransitions();
        int stepsCount = Math.min(mCurStep + steps, mTask.getStepsCount());

        if (mTask.isParallel()) {
            List<Future<?>> futures = new ArrayList<>(transitions.size());
            for (int step = mCurStep; step < stepsCount; step++) {
                copyPreviousStepBig(step, step);
                BigDecimal totalCount = getTotalCountBig(step, step);
                for (Transition transition : transitions) {
                    futures.add(mExecutor
                            .submit(new TransitionActionHigherAccuracy(step, totalCount,
                                    transition)));
                }
                for (Future<?> future : futures) {
                    try {
                        future.get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }
                futures.clear();
                for (int stateId = 0; stateId < mStateIds.length; stateId++) {
                    checkStateNegativenessBig(step, step, stateId);
                }

                if (scale != null) {
                    roundLastBigStates(scale);
                    roundStates(step, scale);
                }

                callbackProgress(step);
                mCurStep++;
            }
        } else {
            for (int step = mCurStep; step < stepsCount; step++) {
                copyPreviousStepBig(step, step);
                BigDecimal totalCount = getTotalCountBig(step, step);
                for (Transition transition : transitions) {
                    transitionHigherAccuracy(step, totalCount, transition);
                }

                if (scale != null) {
                    roundLastBigStates(scale);
                    roundStates(step, scale);
                }

                callbackProgress(step);
                mCurStep++;
            }
        }
    }



    /**
     * ��������, �������������� ����� ���������� �������� � ������� ���������
     */
    private class TransitionActionNormalAccuracy implements Runnable {
        private final int mStep;
        private final double mTotalCount;
        private final Transition mTransition;

        /**
         * @param step       ����� ����
         * @param totalCount ����� ���������� ��������� �� ������� ����
         * @param transition �������
         */
        private TransitionActionNormalAccuracy(int step, double totalCount, Transition transition) {
            mStep = step;
            mTotalCount = totalCount;
            mTransition = transition;
        }

        @Override
        public void run() {
            transitionNormalAccuracy(mStep, mTotalCount, mTransition);
        }
    }

    /**
     * ��������, �������������� ����� ���������� �������� � ���������� ���������
     */
    private class TransitionActionHigherAccuracy implements Runnable {
        private final int mStep;
        private final BigDecimal mTotalCount;
        private final Transition mTransition;

        /**
         * @param step       ����� ����
         * @param totalCount ����� ���������� ��������� �� ������� ����
         * @param transition �������
         */
        private TransitionActionHigherAccuracy(int step, BigDecimal totalCount,
                                               Transition transition) {
            mStep = step;
            mTotalCount = totalCount;
            mTransition = transition;
        }

        @Override
        public void run() {
            transitionHigherAccuracy(mStep, mTotalCount, mTransition);
        }
    }

    public interface ResultCallback {
        /**
         * ���������� ��� ���������� ����������
         *
         * @param result ����������
         */
        void onResult(Result result);
    }

    public interface ProgressCallback {
        /**
         * ���������� ��� ���������� ��������� ����������
         *
         * @param progress �������� (0 - 1)
         */
        void onProgressUpdate(double progress);
    }

    public interface CompleteCallback<T> {
        void onComplete(T result);
    }
}
