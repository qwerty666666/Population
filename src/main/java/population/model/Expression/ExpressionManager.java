package population.model.Expression;

public class ExpressionManager {
    protected String expression;

    public ExpressionManager(String expr) {
        this.expression = expr;
    }

    public boolean isValid() {
        BaseExpression exprImpl = new LinearExpression(expression);
        if (exprImpl.isValid()) {
            return true;
        }

        exprImpl = new ExpExpression(expression);
        if (exprImpl.isValid()) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return this.expression;
    }
}
