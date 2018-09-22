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
        private StringConverter<T> converter;
        private T defaultValue;
        private Comparator<T> comparator;

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
        private DoubleStringConverter converter = new DoubleStringConverter();

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


    /**
     * Convert State to State name <br>
     * If useAlias is true, will use type State alias instead of State name
     */
    public static class StateStringConverter extends StringConverter<State> {
        private boolean useAlias = false;

        public StateStringConverter() {}
        public StateStringConverter(boolean useAlias) {
            this.useAlias = useAlias;
        }

        @Override
        public String toString(State state) {
            if (state == null || state.isEmptyState()) {
                return "";
            }

            if (this.useAlias) {
                String alias = state.getAlias().trim();
                if (alias.length() > 0) {
                    return alias;
                }
            }

            String name = state.getName().trim();
            return name.length() > 0 ? name : StringResource.getString("App.UnnamedStub");
        }

        @Override
        public State fromString(String string) {
            return null;
        }
    };


    /**
     * Convert transition type to String <br>
     * If useAbbreviation is true, will use type abbreviation instead of real name
     */
    public static class TransitionTypeStringConverter extends StringConverter<Number> {
        private boolean useAbbreviation = false;

        public TransitionTypeStringConverter() {}

        public TransitionTypeStringConverter(boolean useAbbreviation) {
            this.useAbbreviation = useAbbreviation;
        }

        @Override
        public String toString(Number object) {
            if (object == null) {
                return "";
            }
            return TransitionType.getName(object.intValue(), useAbbreviation);
        }

        @Override
        public Number fromString(String string) {
            return null;
        }
    }


    /**
     * Convert State mode to String <br>
     * If useAbbreviation is true, will use mode abbreviation instead of real name
     */
    public static class StateInTransitionModeStringConverter extends StringConverter<Integer> {
        private boolean useAbbreviation = false;

        public StateInTransitionModeStringConverter() {}
        public StateInTransitionModeStringConverter(boolean useAbbreviation) {
            this.useAbbreviation = useAbbreviation;
        }

        @Override
        public String toString(Integer object) {
            if (object == null) {
                return "";
            }
            return StateMode.getName(object, useAbbreviation);
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
