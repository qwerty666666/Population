package population.component.parametricPortrait;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import population.util.Resources.StringResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParametricPortraitProperties extends HBox {
    private int propertiesCount = 2;

    private List<TextField> startValueTextFields = new ArrayList<>();
    private List<TextField> endValueTextFields = new ArrayList<>();
    private List<TextField> stepCountTextFields = new ArrayList<>();
    private List<ComboBox<Object>> instanceComboBoxes = new ArrayList<>();
    private List<ComboBox<String>> propertyComboBoxes = new ArrayList<>();



    public ParametricPortraitProperties() {
        super();
        this.setLayout();
    }


    private void setLayout() {
        this.setAlignment(Pos.CENTER);
        this.setSpacing(25);
        this.setPadding(new Insets(25, 0, 0, 0));

        for (int i = 0; i < this.propertiesCount; i++) {
            GridPane gridPane = this.getGridPane();

            this.getChildren().add(gridPane);

            ComboBox<Object> instanceComboBox = new ComboBox<>();
            this.addGridPaneItem(gridPane, instanceComboBox, StringResource.getString("parametric_portrait_instance_label"), 0);
            this.instanceComboBoxes.add(instanceComboBox);

            ComboBox<String> propertyComboBox = new ComboBox<>();
            this.addGridPaneItem(gridPane, propertyComboBox, StringResource.getString("parametric_portrait_property_label"), 1);
            this.propertyComboBoxes.add(propertyComboBox);

            TextField startValueTextField = new TextField();
            this.addGridPaneItem(gridPane, startValueTextField, StringResource.getString("parametric_portrait_start_value_label"), 2);
            this.startValueTextFields.add(startValueTextField);

            TextField endValueTextField = new TextField();
            this.addGridPaneItem(gridPane, endValueTextField, StringResource.getString("parametric_portrait_end_value_label"), 3);
            this.endValueTextFields.add(endValueTextField);

            TextField stepCountTextField = new TextField();
            this.addGridPaneItem(gridPane, stepCountTextField, StringResource.getString("parametric_portrait_steps_count_label"), 4);
            this.stepCountTextFields.add(stepCountTextField);

            if (i < this.propertiesCount - 1) {
                Separator separator = new Separator(Orientation.VERTICAL);
                separator.setPadding(new Insets(15, 0, 15, 0));
                this.getChildren().add(separator);
            }
        }
    }


    private GridPane getGridPane() {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(20);
        gridPane.setVgap(20);
        HBox.setHgrow(gridPane, Priority.ALWAYS);

        ColumnConstraints column0 = new ColumnConstraints();
        column0.setHgrow(Priority.SOMETIMES);
        column0.setMinWidth(70);

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setHgrow(Priority.ALWAYS);
        column1.setMinWidth(60);

        RowConstraints row = new RowConstraints();
        row.setVgrow(Priority.NEVER);

        gridPane.getColumnConstraints().addAll(column0, column1);
        gridPane.getRowConstraints().addAll(Collections.nCopies(5, row));

        return gridPane;
    }


    private void addGridPaneItem(GridPane gridPane, Region node, String labelText, int row) {
        Label label = new Label(labelText);
        label.setWrapText(true);
        gridPane.add(label, 0, row);

        node.setMaxWidth(Double.MAX_VALUE);
        gridPane.add(node, 1, row);
    }
}
