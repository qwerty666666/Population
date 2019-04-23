package population.model.Calculator;


import expression.FunctionExecutorProvider;
import expression.OperatorExecutorProvider;
import expression.RPNExpression;
import population.model.RPNConverter.RPNConverter;
import population.model.RPNConverter.StateOperandSupplier;
import population.model.RPNConverter.VariableOperandSupplier;
import population.model.StateModel.State;
import population.model.TaskV4;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Interface for all numerical integration multi stepSize methods implementations.
 * @param <T>
 */
public abstract class NumericalMultiStepIntegrator<T> extends TaskCalculator {
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
    /** calculated delta on one stepSize */
    protected double[] deltas;



    /**
     * The passed task will be cloned, therefore any subsequent task changes will be ignored.
     * So be aware of create calculator instance right before calling {@link TaskCalculator#calculate()} method.
     * It is done in order to maintain consistency between {@link CalculationResult} and its task reference.
     *
     * @param task
     */
    public NumericalMultiStepIntegrator(TaskV4 task, FunctionExecutorProvider<Double> fep, OperatorExecutorProvider<Double> oep) {
        super(task);

        this.oep = oep;
        this.fep = fep;

        this.stepSize = 1;

        this.differentialEquationSystem = new RPNConverter(task).convert();
        this.variables = differentialEquationSystem.getVariables();
        this.ODEs = differentialEquationSystem.getODEs();

        this.statesCount = new double[task.getStepsCount()][ODEs.size()];
        for (int i = 0; i < ODEs.size(); i++) {
            this.statesCount[0][i] = differentialEquationSystem.getOdeVariable(ODEs.get(i)).get();
        }

        this.deltas = new double[task.getStates().size()];
    }


    /**
     * Do calculation for a given stepSize
     */
    protected abstract void applyStep(int step);


    @Override
    public void calculateToStep(int toStep) {
        for (int step = this.currentCalculatedStep; step <= toStep; step += this.stepSize) {
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
        this.variables.stream()
            .filter(var -> var instanceof StateOperandSupplier)
            .map(StateOperandSupplier.class::cast)
            .forEach(var -> var.setVal(getDelayedStateCount(step, var.getState(), var.getDelay())));
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
}
