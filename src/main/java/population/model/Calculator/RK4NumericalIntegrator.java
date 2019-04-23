package population.model.Calculator;


import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import expression.FunctionExecutorProvider;
import expression.OperatorExecutorProvider;
import population.model.RPNConverter.StateOperandSupplier;
import population.model.RPNConverter.VariableOperandSupplier;
import population.model.TaskV4;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Fourth-order Runge-Kutta method implementation
 */
public class RK4NumericalIntegrator extends NumericalMultiStepIntegrator<Double> {
    private double K[][];
    private double[] offsets;


    @AssistedInject
    public RK4NumericalIntegrator(@Assisted TaskV4 task, FunctionExecutorProvider<Double> fep, OperatorExecutorProvider<Double> oep) {
        super(task, fep, oep);
        K = new double[4][task.getStates().size()];
        offsets = new double[task.getStates().size()];
        stepSize = 0.1;
    }


    @Override
    protected void applyStep(int step) {
        // set variables to last calculated values i.e. Yi
        this.setVariablesFromStep(step - 1);

        // calculate K0 = f(y)
        for (int i = 0; i < ODEs.size(); i++) {
            K[0][i] = ODEs.get(i).calc(oep, fep) * this.stepSize;
        }

        // calculate K1 = f(y + K0/2)
        for (int i = 0; i < offsets.length; i++) {
            offsets[i] = K[0][i] / 2;
        }
        this.setVariablesFromStepWithOffset(step - 1, offsets);
        for (int i = 0; i < ODEs.size(); i++) {
            K[1][i] = ODEs.get(i).calc(oep, fep) * this.stepSize;
        }

        // calculate K2 = f(y + K1/2)
        for (int i = 0; i < offsets.length; i++) {
            offsets[i] = K[1][i] / 2;
        }
        this.setVariablesFromStepWithOffset(step - 1, offsets);
        for (int i = 0; i < ODEs.size(); i++) {
            K[2][i] = ODEs.get(i).calc(oep, fep) * this.stepSize;
        }

        // calculate K3 = f(y + K2/2)
        this.setVariablesFromStepWithOffset(step - 1, K[3]);
        for (int i = 0; i < ODEs.size(); i++) {
            K[3][i] = ODEs.get(i).calc(oep, fep) * this.stepSize;
        }

        // add deltas to prev values i.e. Yi = f(Xi) + 1/6 * (K0 + 2*K1 + 2*K2 + K3)
        for (int i = 0; i < statesCount[step].length; i++) {
            statesCount[step][i] = statesCount[step - 1][i] + (K[0][i] + 2 * K[1][i] + 2 * K[2][i] + K[3][i]) / 6.;
        }
    }


    private void setVariablesFromStepWithOffset(int step, double[] offsets) {
        for (int i = 0; i < this.stateVariables.size(); i++) {
            StateOperandSupplier var = stateVariables.get(i);
            var.setVal(getDelayedStateCount(step, var.getState(), var.getDelay()) + offsets[i]);
        }
    }
}
