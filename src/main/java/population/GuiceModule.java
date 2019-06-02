package population;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import expression.FunctionExecutorProvider;
import expression.OperatorExecutorProvider;
import population.model.Calculator.*;
import population.model.ParametricPortrait.ParametricPortraitCalculator;
import population.model.ParametricPortrait.ParametricPortraitCalculatorFactory;
import population.model.ParametricPortrait.SimpleParametricPortraitCalculator;
import population.model.RPNConverter.DoubleFunctionExecutorProvider;
import population.model.RPNConverter.DoubleOperatorExecutorProvider;


public class GuiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(new TypeLiteral<OperatorExecutorProvider<Double>>(){}).to(DoubleOperatorExecutorProvider.class);
        bind(new TypeLiteral<FunctionExecutorProvider<Double>>(){}).to(DoubleFunctionExecutorProvider.class);

        install(new FactoryModuleBuilder()
            .implement(TaskCalculator.class, EulerCalculator.class)
//            .implement(TaskCalculator.class, EulerNumericalIntegrator.class)
//            .implement(TaskCalculator.class, RK4NumericalIntegrator.class)
            .build(CalculatorFactory.class)
        );

        install(new FactoryModuleBuilder()
//                .implement(TaskCalculator.class, EulerCalculator.class)
//            .implement(TaskCalculator.class, EulerNumericalIntegrator.class)
            .implement(TaskCalculator.class, RK4NumericalIntegrator.class)
            .build(NumericalIntegratorCalculatorFactory.class)
        );

        install(new FactoryModuleBuilder()
            .implement(ParametricPortraitCalculator.class, SimpleParametricPortraitCalculator.class)
            .build(ParametricPortraitCalculatorFactory.class)
        );
    }
}
