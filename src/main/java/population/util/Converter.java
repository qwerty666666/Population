package population.util;


import population.component.ChartSeries;
import population.model.Expression.ExpressionManager;
import population.model.StateModel.State;
import population.model.TransitionModel.StateMode;
import population.model.TransitionType;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;
import population.util.Resources.StringResource;

import java.util.Comparator;


public class Converter {
    /**
     * setup decorator for converter.
     * If default value equals to converted value then return empty string
     */
    public static class HideDefaultValueDecoratorConverter<T> extends StringConverter<T> {
        StringConverter<T> converter;
        T defaultValue;
        Comparator<T> comparator;

        /**
         *
         * @param converter default used converter
         * @param defaultValue default value
         * @param comparator comparator for comparing values
         */
        public HideDefaultValueDecoratorConverter(StringConverter<T> converter, T defaultValue, Comparator<T> comparator) {
            this.converter = converter;
            this.defaultValue = defaultValue;
            this.comparator = comparator;
        }

        @Override
        public String toString(T object) {
            // workaround default Number comparators
            if (object == null && this.defaultValue == null) {
                return "";
            }
            if (object != null && this.defaultValue != null && comparator.compare(this.defaultValue, object) == 0) {
                return "";
            }
            return converter.toString(object);
        }

        @Override
        public T fromString(String string) {
            return converter.fromString(string);
        }
    }

    /**
     * the same as {@link DoubleStringConverter} but trim string and replace all commas with dots
     */
    public static final StringConverter<Double> DOUBLE_STRING_CONVERTER = new StringConverter<Double>() {
        DoubleStringConverter converter = new DoubleStringConverter();

        @Override
        public String toString(Double object) {
            return object == null ? "" :
                    object.toString()
                            // as we have format like 0.0, remove trailing zeros
                            .replaceAll("[0]*$", "")
                            // and remove trailing dot
                            .replaceAll("\\.$", "");
        }

        @Override
        public Double fromString(String string) {
            string = string.trim().replaceAll(",",".");
            return converter.fromString(string);
        }
    };


    public static final StringConverter<ExpressionManager> EXPRESSION_STRING_CONVERTER = new StringConverter<ExpressionManager>() {
        @Override
        public String toString(ExpressionManager object) {
            return object == null ? "" : object.toString();
        }

        @Override
        public ExpressionManager fromString(String string) {
            ExpressionManager em = new ExpressionManager(string);
            if (em.isValid()) {
                return em;
            } else {
                throw new IllegalArgumentException("Invalid expression format");
            }
        }
    };


    public static final StringConverter<State> STATE_STRING_CONVERTER = new StringConverter<State>() {
        @Override
        public String toString(State state) {
            if (state == null || state.isEmptyState()) {
                return "";
            }

            String alias = state.getAlias().trim();
            if (alias.length() > 0) {
                return alias;
            }

            String name = state.getName().trim();
            return name.length() > 0 ? name : StringResource.getString("App.UnnamedStub");
        }

        @Override
        public State fromString(String string) {
            return null;
        }
    };


    public static final StringConverter<Number> TRANSITION_TYPE_STRING_CONVERTER = new StringConverter<Number>() {
        @Override
        public String toString(Number object) {
            return object == null ? "" : TransitionType.getName(object.intValue());
        }

        @Override
        public Number fromString(String string) {
            return null;
        }
    };


    public static final StringConverter<Integer> STATE_IN_TRANSITION_MODE_STRING_CONVERTER = new StringConverter<Integer>() {
        @Override
        public String toString(Integer object) {
            return object == null ? "" : StateMode.getName(object);
        }

        @Override
        public Integer fromString(String string) {
            return null;
        }
    };


    public static final StringConverter<Number> COLOR_STRING_CONVERTER = new StringConverter<Number>() {
        @Override
        public String toString(Number object) {
            return ChartSeries.Color.getName(object.intValue());
        }

        @Override
        public Number fromString(String string) {
            return null;
        }
    };


    public static final StringConverter<Number> DASH_STRING_CONVERTER = new StringConverter<Number>() {
        @Override
        public String toString(Number object) {
            return ChartSeries.Dash.getName(object.intValue());
        }

        @Override
        public Number fromString(String string) {
            return null;
        }
    };


    public static final StringConverter<Number> THICKNESS_STRING_CONVERTER = new StringConverter<Number>() {
        @Override
        public String toString(Number object) {
            return ChartSeries.Thickness.getName(object.intValue());
        }

        @Override
        public Number fromString(String string) {
            return null;
        }
    };
}
