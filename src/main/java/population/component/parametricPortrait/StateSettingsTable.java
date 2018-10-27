package population.component.parametricPortrait;


import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;
import population.component.UIComponents.ColorTableCell;
import population.model.ColorGenerator.ColorGenerator;
import population.model.ParametricPortrait.StateSetting;
import population.model.StateModel.State;
import population.model.TaskV4;
import population.util.Resources.StringResource;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents
 */
public class StateSettingsTable extends TableView<StateSetting> {
    /**
     * Task which states should be observed for table model.
     * It usually should be global task instance
     */
    private final TaskV4 task;

    /** table model */
    private ObservableList<StateSetting> stateSettings;

    private ColorGenerator colorGenerator;


    public StateSettingsTable(TaskV4 task, ObservableList<StateSetting> stateSettings) {
        this.task = task;
        this.setLayout();
        this.setEditable(true);

        this.stateSettings = stateSettings;
        this.initStateSettingList();
    }


    private void setLayout() {
        this.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<StateSetting, String> nameColumn = new TableColumn<>(StringResource.getString("parametric_portrait_state_name_header"));
        nameColumn.setEditable(false);
        nameColumn.setSortable(false);
        nameColumn.setPrefWidth(100);
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setCellValueFactory(param -> param.getValue().getState().nameProperty());
        this.getColumns().add(nameColumn);

        TableColumn<StateSetting, Boolean> visibilityColumn = new TableColumn<>(StringResource.getString("parametric_portrait_visibility_header"));
        visibilityColumn.setSortable(false);
        visibilityColumn.setPrefWidth(100);
        visibilityColumn.setCellFactory(CheckBoxTableCell.forTableColumn(visibilityColumn));
        visibilityColumn.setCellValueFactory(param -> param.getValue().showProperty());
        this.getColumns().add(visibilityColumn);

        TableColumn<StateSetting, Color> colorColumn = new TableColumn<>(StringResource.getString("parametric_portrait_color_header"));
        colorColumn.setSortable(false);
        colorColumn.setPrefWidth(150);
        // TODO set editable
        colorColumn.setEditable(false);
        colorColumn.setCellFactory(param -> new ColorTableCell<>(colorColumn));
        colorColumn.setCellValueFactory(param -> param.getValue().colorProperty());
        colorColumn.setOnEditCommit(t -> {
            StateSetting stateSetting = t.getTableView().getItems().get(t.getTablePosition().getRow());
            stateSetting.setColor(t.getNewValue());
        });
        this.getColumns().add(colorColumn);
    }


    private void initStateSettingList() {
        this.setItems(stateSettings);

        this.task.getStates().addListener((ListChangeListener<? super State>) c -> {
            while (c.next()) {
                if (c.wasUpdated()) {
                    for (int i = c.getFrom(); i < c.getTo(); i++) {
                    }

                } else if (c.wasPermutated()) {
                    int from = c.getFrom(), to = c.getTo();
                    List<StateSetting> copy = new ArrayList<>(stateSettings.subList(from, to));
                    for (int oldIndex = from; oldIndex < to; oldIndex++) {
                        int newIndex = c.getPermutation(oldIndex);
                        stateSettings.set(newIndex, copy.get(oldIndex - from));
                    }

                } else if (c.wasAdded()) {
                    int i = 0;
                    for (State o: c.getAddedSubList()) {
                        stateSettings.add(c.getFrom() + i, new StateSetting(o, this.colorGenerator));
                        i++;
                    }

                } else if (c.wasRemoved()) {
                    stateSettings.remove(c.getFrom(), c.getFrom() + c.getRemovedSize());
                }
            }
        });
    }


    public void setColorGenerator(ColorGenerator colorGenerator) {
        this.colorGenerator = colorGenerator;
    }
}
