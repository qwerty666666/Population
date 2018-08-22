package population.model.Expression;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpExpression extends BaseExpression {
    protected LinearExpression inner = null;
    protected boolean isValid = true;

    public ExpExpression(String expr) {
        super(expr);
        Matcher matcher = Pattern.compile("^exp\\((.+t)\\)$").matcher(expression);
        if (matcher.find()) {
            this.inner = new LinearExpression(matcher.group(1));
        } else {
            isValid = false;
        }
    }

    @Override
    public boolean isValid() {
        return isValid && inner.isValid();
    }
}
