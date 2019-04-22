package population.model.Calculator;

public interface NumericalIntegrator<T> {
    double[][] integrate(DifferentialEquationSystem<T> differentialEquationSystem);
}
