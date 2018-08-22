package population.util;


import population.App;
import population.component.ChartSeries;
import population.model.Expression.ExpressionManager;
import population.model.StateModel.State;
import population.model.TransitionModel.StateMode;
import population.model.TransitionType;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;


public class Converter {
    /**
     * the same as {@link DoubleStringConverter} but trim string and replace all commas with dots
     */
    public static final StringConverter<Double> DOUBLE_STRING_CONVERTER = new StringConverter<Double>() {
        DoubleStringConverter converter = new DoubleStringConverter();

        @Override
        public String toString(Double object) {
            return object == null ? "" :
                    // remove trailing zeros
                    object.toString().replaceAll("[0]*$", "").replaceAll("\\.$", "");
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
            if (state == null) {
                return App.getString("unselected");
            }
            if (state.isExternal()) {
                return App.getString("state_external");
            }
            String name = state.getName();
            if (Utils.isNullOrEmpty(name)) {
                return App.getString("unnamed");
            }
            return name;
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


    public static final StringConverter<Number> STATE_IN_TRANSITION_MODE_STRING_CONVERTER = new StringConverter<Number>() {
        @Override
        public String toString(Number object) {
            return object == null ? "" : StateMode.getName(object.intValue());
        }

        @Override
        public Number fromString(String string) {
            return null;
        }
    };


    public static final StringConverter<Number> COLOR_STRING_CONVERTER = new StringConverter<Number>() {
        @Override
        public String toString(Number object) {
            return ChartSeries.Color.getName(object.intValue(), App.getResources());
        }

        @Override
        public Number fromString(String string) {
            return null;
        }
    };


    public static final StringConverter<Number> DASH_STRING_CONVERTER = new StringConverter<Number>() {
        @Override
        public String toString(Number object) {
            return ChartSeries.Dash.getName(object.intValue(), App.getResources());
        }

        @Override
        public Number fromString(String string) {
            return null;
        }
    };


    public static final StringConverter<Number> THICKNESS_STRING_CONVERTER = new StringConverter<Number>() {
        @Override
        public String toString(Number object) {
            return ChartSeries.Thickness.getName(object.intValue(), App.getResources());
        }

        @Override
        public Number fromString(String string) {
            return null;
        }
    };
}
