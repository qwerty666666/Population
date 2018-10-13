package population.util;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ListUtils {
    /**
     * @return cloned list derived by calling "clone" method on each item
     */
    public static <T extends Cloneable<T>> ObservableList<T> cloneObservableList(ObservableList<T> list) {
        ObservableList<T> clone = FXCollections.observableArrayList();
        for (T object: list) {
            clone.add(object.clone());
        }
        return clone;
    }


    /**
     * generate list of items provided by supplier
     *
     * @param supplier items supplier
     * @param limit items count
     */
    public static <T> List<T> generateListValues(Supplier<T> supplier, int limit) {
        return Stream.generate(supplier)
            .limit(limit)
            .collect(Collectors.toList());
    }
}
