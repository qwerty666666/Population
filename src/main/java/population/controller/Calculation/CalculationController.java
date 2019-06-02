package population.controller.Calculation;

import com.google.inject.Inject;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import population.App;
import population.controller.base.AbstractController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.converter.NumberStringConverter;
import population.model.Calculator.CalculatorFactory;
import population.model.Calculator.NumericalIntegratorCalculatorFactory;
import population.model.Calculator.NumericalTaskIntegrator;
import population.model.Calculator.TaskCalculator;


public class CalculationController extends AbstractController {
    @FXML
    private TextField startPointTextField;
    @FXML
    private TextField stepsCountTextField;
    @FXML
    private TextField stepSizeTextField;
    @FXML
    private ProgressBar calculationProgressBar;
    @FXML
    private Button calculationButton;
    @FXML
    private CheckBox allowNegativeNumbers;

    @Inject
    private CalculatorFactory calculatorFactory;

    @Inject
    private NumericalIntegratorCalculatorFactory numericalIntegratorCalculatorFactory;


    @Override
    public void initialize() {
        startPointTextField.textProperty().bindBidirectional(App.getTask().startPointProperty(), new NumberStringConverter());
        stepsCountTextField.textProperty().bindBidirectional(App.getTask().stepsCountProperty(), new NumberStringConverter());
        allowNegativeNumbers.selectedProperty().bindBidirectional(App.getTask().isAllowNegativeProperty());

        stepSizeTextField.setText(Double.toString(0.1));
    }


    /**
     * Calculate task and display the result
     */
    @FXML
    public void calculate() {
        onCalculationStart();

        // create calculator
        TaskCalculator calculator = this.numericalIntegratorCalculatorFactory.create(
            App.getTask().clone(),
            Double.parseDouble(this.stepSizeTextField.getText())
        );
        calculator.progressProperty().addListener(new ChangeListener<Number>() {
            private double prevValue = 0;

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                // update only when progress value changed
                double newVal = newValue.doubleValue();
                if (prevValue != newVal) {
                    prevValue = newVal;
                    Platform.runLater(() -> calculationProgressBar.setProgress(newVal));
                }
            }
        });

        Task task = new Task() {
            @Override
            protected Object call() {
                calculator.calculate();
                return null;
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                this.onCalculationFinished();
                App.getResultTableController().addCalculationResult(calculator.getCalculationResult());
                App.getResultChartController().addChartForCalculationResult(calculator.getCalculationResult());
            });
        });

        new Thread(task).start();
    }


    protected void onCalculationStart() {
        calculationProgressBar.setProgress(0);
        calculationProgressBar.setVisible(true);
        setInterfaceDisabled(true);
    }

    protected void onCalculationFinished() {
        calculationProgressBar.setVisible(false);
        setInterfaceDisabled(false);
    }

    protected void setInterfaceDisabled(boolean isDisabled) {
        startPointTextField.setDisable(isDisabled);
        stepsCountTextField.setDisable(isDisabled);
        stepSizeTextField.setDisable(isDisabled);
        calculationButton.setDisable(isDisabled);
        allowNegativeNumbers.setDisable(isDisabled);
    }
}