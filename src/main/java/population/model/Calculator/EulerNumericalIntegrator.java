package population.model.Calculator;


import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import expression.FunctionExecutorProvider;
import expression.OperatorExecutorProvider;
import population.model.TaskV4;


public class EulerNumericalIntegrator extends NumericalMultiStepIntegrator<Double> {
    @AssistedInject
    public EulerNumericalIntegrator(@Assisted TaskV4 task, FunctionExecutorProvider<Double> fep, OperatorExecutorProvider<Double> oep) {
        super(task, fep, oep);
    }

    @Override
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
