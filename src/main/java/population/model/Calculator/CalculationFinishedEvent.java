package population.model.Calculator;

import population.util.Event.Event;

public class CalculationFinishedEvent extends Event {
    protected Calculator calculator = null;

    public Calculator getCalculator() {
        return calculator;
    }

    public void setCalculator(Calculator calculator) {
        this.calculator = calculator;
    }
}
