package population.controller.Calculation;

import population.App;
import population.controller.base.AbstractController;
import population.model.Calculator.CalculationProgressEvent;
import population.model.Calculator.Calculator;
import population.model.TaskV4;
import population.util.Event.EventManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.util.converter.BooleanStringConverter;
import javafx.util.converter.NumberStringConverter;

import java.io.IOException;


public class CalculationController extends AbstractController {
    @FXML
    private TextField startPointTextField;
    @FXML
    private TextField stepsCountTextField;
    @FXML
    private ProgressBar calculationProgressBar;
    @FXML
    private Button calculationButton;
    @FXML
    private TabPane tabPane;
    @FXML
    private CheckBox allowNegativeNumbers;


    /**************************************************
     *
     *                initialization
     *
     *************************************************/

    @Override
    public void initialize() {
        startPointTextField.textProperty().bindBidirectional(App.getTask().startPointProperty(), new NumberStringConverter());
        stepsCountTextField.textProperty().bindBidirectional(App.getTask().stepsCountProperty(), new NumberStringConverter());
        allowNegativeNumbers.selectedProperty().bindBidirectional(App.getTask().isAllowNegativeProperty());

        EventManager.addEventHandler(Calculator.PROGRESS_EVENT, event -> {
            Platform.runLater(() -> calculationProgressBar.setProgress(((CalculationProgressEvent)event).getProgress()));
            return true;
        });

        EventManager.addEventHandler(Calculator.FINISHED_EVENT, event -> {
            Platform.runLater(this::onCalculationFinished);
            return true;
        });
    }

    /*************************************************
     *
     *              FXML Bindings
     *
     *************************************************/

    @FXML
    public void calculate() {
        TaskV4 task = App.getTask();

        onCalculationStart();
        Calculator calculator = new Calculator(task);
        calculator.calculateAsync();

    }

    /*************************************************
     *
     *                  Stuffs
     *
     *************************************************/

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
        calculationButton.setDisable(isDisabled);
    }
}