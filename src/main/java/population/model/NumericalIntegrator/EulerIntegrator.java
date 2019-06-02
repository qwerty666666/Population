package population.model.NumericalIntegrator;


import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import expression.FunctionExecutorProvider;
import expression.OperatorExecutorProvider;
import expression.RPNExpression;
import population.model.Calculator.DifferentialEquationSystem;
import population.model.RPNConverter.RPNConverter;
import population.model.RPNConverter.StateOperandSupplier;
import population.model.RPNConverter.VariableOperandSupplier;
import population.model.StateModel.State;
import population.model.TaskV4;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class EulerIntegrator extends NumericalIntegrator {
    /** calculated delta on one stepSize */
    private double[] deltas;


    @AssistedInject
    public EulerIntegrator(
        DifferentialEquationSystem<Double> differentialEquationSystem,
        double stepSize,
        int stepCnt,
        List<StateOperandSupplier> stateVariables,
        FunctionExecutorProvider<Double> fep,
        OperatorExecutorProvider<Double> oep
    ) {
        super(differentialEquationSystem, stepSize, stepCnt, stateVariables, fep, oep);
        this.deltas = new double[this.ODEs.size()];
    }


    public double[][] calculate() {
        for (int step = 0; step < this.stepCnt; step += 1) {
            applyStep(step);

            if (this.roundScale != null) {
                this.roundStates(roundScale, this.statesCount[step]);
            }

            if (!this.isAllowNegative) {
                this.restrictNegativeness(this.statesCount[step]);
            }
        }
        return this.statesCount;
    }


    protected void applyStep(int step) {
        // set variables to last calculated values i.e. Yi
        this.setVariablesFromStep(step - 1);

        // calculated deltas i.e. f(Xi)
        for (int i = 0; i < ODEs.size(); i++) {
            deltas[i] = ODEs.get(i).calc(oep, fep);
        }

        // add deltas to prev values i.e. Yi = f(Xi) + step * delta(Xi)
        for (int i = 0; i < statesCount[step].length; i++) {
            statesCount[step][i] = statesCount[step - 1][i] + deltas[i] * this.stepSize;
        }
    }
}
