package population.model.Expression;

import population.util.Converter;

public class LinearExpression extends BaseExpression {
    public LinearExpression(String expr) {
        super(expr);
        if (expression.equals("t") || expression.isEmpty()) {
            this.expression = "1";
        } else if (expression.endsWith("t")) {
            expression = expression.substring(0, expression.length() - 2);
        }
    }

    @Override
    public boolean isValid() {
        try {
            Converter.DOUBLE_STRING_CONVERTER.fromString(expression);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
