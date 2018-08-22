package population.model.Expression;



public class BaseExpression implements ExpressionInterface {
    protected String expression;

    public BaseExpression(String expr) {
        this.expression = expr.replaceAll("\\s", "");
    }

    @Override
    public boolean isValid() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public double resolve() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
