package population.component.ui;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

import java.util.ResourceBundle;

/**
 * Helper for JavaFX Table
 */
public class TableUtils {
    /**
     * Set text header text from bundle with tooltip <br>
     * Key for tooltip will made by appending 'Tooltip' to the text key.
     * If string for key doesn't exist, tooltip won't be added
     *
     * @param column table column
     * @param bundle resource bundle for text
     * @param key    key for bundle
     */
    public static void setColumnHeaderTextWithTooltip(TableColumn column, ResourceBundle bundle, String key) {
        // add header text
        Label label = new Label(bundle.getString(key));
        column.setGraphic(label);

        // set text to fill al available space
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);

        // add tooltip to label
        try {
            label.setTooltip(new Tooltip(bundle.getString(key + "Tooltip")));
        } catch (Exception ignored) {
            // if tooltip key wasn't found, ignore it
        }

    }


    /**
     * hide tooltip in table cell when row item associated with cell is null
     * @param cell table cell with assigned Tooltip
     */
    public static void hideCellTooltipInEmptyRow(TableCell cell) {
        ObjectProperty<Tooltip> defaultTooltip = new SimpleObjectProperty<>(cell.getTooltip());
        BooleanProperty affectedByThat = new SimpleBooleanProperty(false);

        cell.tooltipProperty().addListener((observable, oldValue, newValue) -> {
            if (!affectedByThat.get()) {
                defaultTooltip.set(newValue);
            }
        });

        // remove tooltip from cell when associated table row item is null
        ObjectProperty rowItem = cell.getTableRow().itemProperty();
        rowItem.addListener((observable, oldValue, newValue) -> {

            affectedByThat.set(true);
            if (newValue == null) {
                cell.setTooltip(null);
            } else {
                cell.setTooltip(defaultTooltip.get());
            }
            affectedByThat.set(false);
        });

        if (rowItem.get() == null) {
            affectedByThat.set(true);
            cell.setTooltip(null);
            affectedByThat.set(false);
        }
    }
}
