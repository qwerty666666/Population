package population.model.NumericalIntegrator;


import expression.FunctionExecutorProvider;
import expression.OperatorExecutorProvider;
import expression.RPNExpression;
import population.model.Calculator.DifferentialEquationSystem;
import population.model.RPNConverter.StateOperandSupplier;
import population.model.RPNConverter.VariableOperandSupplier;
import population.model.StateModel.State;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.IntStream;


public abstract class NumericalIntegrator {
    protected List<RPNExpression<Double>> ODEs;
    protected double[][] statesCount;
    protected DifferentialEquationSystem<Double> differentialEquationSystem;
    protected FunctionExecutorProvider<Double> fep;
    protected OperatorExecutorProvider<Double> oep;
    /** variables representing expressions in system. Sorted by expressions order in system */
    protected List<StateOperandSupplier> stateVariables;
    protected double stepSize;
    protected int stepCnt;

    protected Integer roundScale = null;
    protected boolean isAllowNegative = false;


    public NumericalIntegrator(
        DifferentialEquationSystem<Double> differentialEquationSystem,
        double stepSize,
        int stepCnt,
        List<StateOperandSupplier> stateVariables,
        FunctionExecutorProvider<Double> fep,
        OperatorExecutorProvider<Double> oep
    ) {
        this.differentialEquationSystem = differentialEquationSystem;
        this.oep = oep;
        this.fep = fep;
        this.stepSize = stepSize;
        this.stepCnt = stepCnt;
        this.ODEs = differentialEquationSystem.getODEs();
        this.stateVariables = stateVariables;

        this.statesCount = new double[this.stepCnt][ODEs.size()];
        for (int i = 0; i < ODEs.size(); i++) {
            this.statesCount[0][i] = differentialEquationSystem.getOdeVariable(ODEs.get(i)).get();
        }
    }


    public void setIsAllowNegative(boolean isAllowNegative) {
        this.isAllowNegative = isAllowNegative;
    }

    public void setRoundScale(Integer roundScale) {
        this.roundScale = roundScale;
    }


    abstract public double[][] calculate();


    /**
     * Set variable values from the stepSize i.e. set Yi = f(Xstep)
     */
    protected void setVariablesFromStep(int step) {
        this.stateVariables.forEach(var -> var.setVal(getDelayedStateCount(step, var.getState(), var.getDelay())));
    }


    /**
     * Returns state count with delay influence
     */
    protected double getDelayedStateCount(int step, State state, int delay) {
        int ind = IntStream.range(0, this.stateVariables.size())
            .filter(i -> this.stateVariables.get(i).getState() == state)
            .findFirst()
            .orElse(-1);
        if (step - delay < 0) {
            return statesCount[0][ind];
        }
        return statesCount[step - delay][ind];
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
}
