package population.model.Calculator;

import population.util.Event.Event;

public class CalculationProgressEvent extends Event {
    protected double progress = 0;

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }
}
