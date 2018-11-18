package population;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import population.model.Calculator.CalculatorFactory;
import population.model.Calculator.EulerCalculator;
import population.model.Calculator.TaskCalculator;
import population.model.ParametricPortrait.ParametricPortraitCalculator;
import population.model.ParametricPortrait.ParametricPortraitCalculatorFactory;
import population.model.ParametricPortrait.SimpleParametricPortraitCalculator;


public class GuiceModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder()
            .implement(TaskCalculator.class, EulerCalculator.class)
            .build(CalculatorFactory.class)
        );

        install(new FactoryModuleBuilder()
            .implement(ParametricPortraitCalculator.class, SimpleParametricPortraitCalculator.class)
            .build(ParametricPortraitCalculatorFactory.class)
        );
    }
}
