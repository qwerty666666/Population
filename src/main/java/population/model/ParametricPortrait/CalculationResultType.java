package population.model.ParametricPortrait;


public enum CalculationResultType {
    /**
     * The calculation result is converged
     */
    STABLE,
    /**
     * The result is given by defining trends
     */
    TRENDS,
    /**
     * The result is given for integral sum
     */
    INTEGRAL_SUM
}
