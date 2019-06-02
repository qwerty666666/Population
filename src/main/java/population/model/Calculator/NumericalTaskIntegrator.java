package population.model.Calculator;


import expression.FunctionExecutorProvider;
import expression.OperatorExecutorProvider;
import expression.RPNExpression;
import population.App;
import population.model.RPNConverter.*;
import population.model.StateModel.State;
import population.model.TaskV4;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Interface for all numerical integration multi stepSize methods implementations.
 */
public abstract class NumericalTaskIntegrator extends TaskCalculator {
    protected Integer roundScale = null;
    protected boolean isAllowNegative = false;
    protected double stepSize;
    protected List<VariableOperandSupplier<Double>> variables;
    protected List<RPNExpression<Double>> ODEs;
    protected DifferentialEquationSystem<Double> differentialEquationSystem;
    protected boolean isFinished = false;
    protected int currentCalculatedStep = 1;
    protected FunctionExecutorProvider<Double> fep;
    protected OperatorExecutorProvider<Double> oep;
    /** variables representing expressions in system. Sorted by expressions order in system */
    protected List<StateOperandSupplier> stateVariables;



    /**
     * The passed task will be cloned, therefore any subsequent task changes will be ignored.
     * So be aware of create calculator instance right before calling {@link TaskCalculator#calculate()} method.
     * It is done in order to maintain consistency between {@link CalculationResult} and its task reference.
     *
     * @param task
     */
    public NumericalTaskIntegrator(TaskV4 task, double stepSize, FunctionExecutorProvider<Double> fep, OperatorExecutorProvider<Double> oep) {
        super(task);

        this.oep = oep;
        this.fep = fep;

        this.stepSize = stepSize;

        this.differentialEquationSystem = new RPNConverter(task).convert();
        this.variables = differentialEquationSystem.getVariables();
        this.ODEs = differentialEquationSystem.getODEs();

        this.statesCount = new double[task.getStepsCount()][ODEs.size()];
        for (int i = 0; i < ODEs.size(); i++) {
            this.statesCount[0][i] = differentialEquationSystem.getOdeVariable(ODEs.get(i)).get();
        }

        this.stateVariables = this.variables.stream()
            .filter(var -> var instanceof StateOperandSupplier)
            .map(StateOperandSupplier.class::cast)
            .sorted(Comparator.comparingInt(o -> task.getStates().indexOf(o.getState())))
            .collect(Collectors.toList());
    }


    /**
     * Do calculation for a given stepSize
     */
    protected abstract void applyStep(int step);


    @Override
    public void calculateToStep(int toStep) {
        for (int step = this.currentCalculatedStep; step <= toStep; step += 1) {
            applyStep(step);

            if (this.roundScale != null) {
                this.roundStates(roundScale, this.statesCount[step]);
            }

            if (!this.isAllowNegative) {
                this.restrictNegativeness(this.statesCount[step]);
            }

            this.progress.set((double)this.currentCalculatedStep / this.task.getStepsCount());

            this.currentCalculatedStep++;
        }

        this.isFinished = true;
    }


    @Override
    public boolean isFinished() {
        return this.isFinished;
    }


    /**
     * Set variable values from the stepSize i.e. set Yi = f(Xstep)
     */
    protected void setVariablesFromStep(int step) {
        this.stateVariables.forEach(var -> var.setVal(getDelayedStateCount(step, var.getState(), var.getDelay())));
    }


    public void setRoundScale(int scale) {
        this.roundScale = scale;
    }


    /**
     * round states count in statesCount on stepSize with given precision
     * @param scale precision
     * @param vals values to be rounded
     */
    protected void roundStates(Integer scale, double[] vals) {
        for (int i = 0; i < vals.length; i++) {
            vals[i] = new BigDecimal(vals[i]).setScale(scale, RoundingMode.HALF_UP).doubleValue();
        }
    }


    /**
     * Make val 0 if its values is negatives
     */
    protected void restrictNegativeness(double[] vals) {
        for (int i = 0; i < vals.length; i++) {
            if (vals[i] < 0) {
                vals[i] = 0;
            }
        }
    }


    /**
     * Returns state count with delay influence
     */
    protected double getDelayedStateCount(int step, State state, int delay) {
        int ind = this.task.getStates().indexOf(state);
        if (step - delay < 0) {
            return statesCount[0][ind];
        }
        return statesCount[step - delay][ind];
    }


    @Override
    public CalculationResult getCalculationResult() {
        if (!this.isFinished()) {
            throw new IllegalStateException("Calculation should be finished before getCalculationResult() call");
        }

        // linearly approximate result to int steps (1, 2, 3...)
        int steps = (int)(this.task.getStepsCount() * this.stepSize);
        int statesCnt = this.task.getStates().size();
        double[][] states = new double[steps][statesCnt];
        for (int i = 0; i < statesCnt; i++) {
            for (int step = 0; step < steps; step++) {
                int s = (int)(step / this.stepSize);
                states[step][i] = this.statesCount[s][i] + (this.statesCount[Math.min(s + 1, this.task.getStepsCount() - 1)][i] - this.statesCount[s][i]) * (step - this.stepSize * s) / this.stepSize;
            }
        }

        return new CalculationResult(this.task, states);
    }
}
