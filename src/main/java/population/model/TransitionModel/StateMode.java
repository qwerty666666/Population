package population.model.TransitionModel;

import population.App;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ResourceBundle;

public class StateMode {
    public static final int SIMPLE = 0;
    public static final int INHIBITOR = 1;
    public static final int RESIDUAL = 2;

    public static final ObservableList<Number> MODES =
            FXCollections.observableArrayList(new Number[]{INHIBITOR, RESIDUAL});

    public static String getName(int type) {
        ResourceBundle resources = App.getResources();
        switch (type) {
            case SIMPLE: {
                return resources.getString("mode_simple");
            }
            case INHIBITOR: {
                return resources.getString("mode_inhibitor");
            }
            case RESIDUAL: {
                return resources.getString("mode_residual");
            }
            default: {
                return "";
            }
        }
    }
}
