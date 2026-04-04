package dev.nutellim.MyPlaceholders.models.types;

import dev.nutellim.MyPlaceholders.models.Placeholder;
import lombok.Getter;
import lombok.Setter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class MathPlaceholder extends Placeholder {

    private final RoundingMode rounding;
    private final int precision;
    private final MathContext mathContext;
    @Setter @Getter private String processedValue;

    private static final int MAX_EXPRESSION_LENGTH = 64;
    private static final double MAX_EXPONENT = 10;
    private static final double MAX_INPUT_VALUE = 1_000_000;
    private static final int MAX_RESULT_DIGITS = 20;
    private static final int MAX_TOKENS = 32;

    public MathPlaceholder(String id, ConfigurationSection section) {
        super(id, section);

        String roundingStr = section.getString("rounding", "HALF_UP");
        RoundingMode rm;
        try {
            rm = RoundingMode.valueOf(roundingStr != null ? roundingStr.toUpperCase() : "HALF_UP");
        } catch (IllegalArgumentException e) {
            rm = RoundingMode.HALF_UP;
        }
        this.rounding = rm;
        this.precision = section.getInt("precision", 2);
        this.mathContext = new MathContext(Math.max(1, this.precision), this.rounding);
        this.processedValue = this.value != null ? this.value : "";
    }

    @Override
    public String process(Player player) {
        String pv = this.processedValue != null ? this.processedValue : (this.value != null ? this.value : "");
        String resolved = PlaceholderAPI.setPlaceholders(player, pv);
        return applyDecorations(evaluate(resolved));
    }

    public String evaluate(String expression) {
        try {
            expression = expression.replaceAll("\\s", "");

            if (expression.isEmpty())
                return "0";

            if (expression.length() > MAX_EXPRESSION_LENGTH)
                return "Expression too long";

            BigDecimal result = evaluateExpression(expression);
            result = result.setScale(precision, rounding);
            String plain = result.stripTrailingZeros().toPlainString();

            if (plain.replace("-", "").replace(".", "").length() > MAX_RESULT_DIGITS)
                return "Result too large";

            return plain;

        } catch (Exception e) {
            return "Invalid expression";
        }
    }

    private BigDecimal evaluateExpression(String expression) {
        return evaluatePostfix(infixToPostfix(expression));
    }

    private List<String> infixToPostfix(String expression) {
        Stack<Character> operators = new Stack<>();
        List<String> output = new ArrayList<>();
        StringBuilder number = new StringBuilder();

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (Character.isDigit(c) || c == '.') {
                number.append(c);
            } else if (Character.isLetter(c)) {
                number.append(c);
            } else {
                if (number.length() > 0) { output.add(number.toString()); number.setLength(0); }
                if (c == '(') {
                    operators.push(c);
                } else if (c == ')') {
                    while (!operators.isEmpty() && operators.peek() != '(')
                        output.add(String.valueOf(operators.pop()));
                    if (!operators.isEmpty()) operators.pop();
                } else {
                    while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(c))
                        output.add(String.valueOf(operators.pop()));
                    operators.push(c);
                }
            }

            if (output.size() > MAX_TOKENS)
                throw new ArithmeticException("Expression too complex");
        }

        if (number.length() > 0) output.add(number.toString());
        while (!operators.isEmpty()) output.add(String.valueOf(operators.pop()));
        return output;
    }

    private BigDecimal evaluatePostfix(List<String> postfix) {
        Stack<BigDecimal> stack = new Stack<>();

        for (String token : postfix) {
            if (token.matches("-?\\d+(\\.\\d+)?")) {
                BigDecimal val = new BigDecimal(token, mathContext);
                if (val.abs().compareTo(BigDecimal.valueOf(MAX_INPUT_VALUE)) > 0)
                    throw new ArithmeticException("Value too large");
                stack.push(val);
            } else if (isFunction(token)) {
                if (stack.isEmpty()) throw new ArithmeticException("Invalid expression");
                BigDecimal a = stack.pop();
                stack.push(applyFunction(token, a));
            } else {
                if (stack.size() < 2) throw new ArithmeticException("Invalid expression");
                BigDecimal b = stack.pop();
                BigDecimal a = stack.pop();
                stack.push(applyOperator(token, a, b));
            }
        }

        if (stack.isEmpty()) throw new ArithmeticException("Empty result");
        return stack.pop();
    }

    private int precedence(char op) {
        switch (op) {
            case '+': case '-': return 1;
            case '*': case '/': case '%': return 2;
            case '^': return 3;
            default: return 0;
        }
    }

    private boolean isFunction(String token) {
        return Arrays.asList("sqrt", "log", "sin", "cos", "tan", "abs", "round", "ceil", "floor", "exp")
                .contains(token);
    }

    private BigDecimal applyFunction(String function, BigDecimal value) {
        double d = value.doubleValue();
        double result;
        switch (function) {
            case "sqrt":  result = Math.sqrt(d);  break;
            case "log":   result = Math.log(d);   break;
            case "sin":   result = Math.sin(Math.toRadians(d)); break;
            case "cos":   result = Math.cos(Math.toRadians(d)); break;
            case "tan":   result = Math.tan(Math.toRadians(d)); break;
            case "abs":   return value.abs();
            case "round": return BigDecimal.valueOf(Math.round(d));
            case "ceil":  return BigDecimal.valueOf(Math.ceil(d));
            case "floor": return BigDecimal.valueOf(Math.floor(d));
            case "exp":   result = Math.exp(d);   break;
            default: throw new IllegalArgumentException("Unknown function: " + function);
        }
        if (Double.isNaN(result) || Double.isInfinite(result))
            throw new ArithmeticException("Invalid result in " + function);
        return BigDecimal.valueOf(result);
    }

    private BigDecimal applyOperator(String operator, BigDecimal a, BigDecimal b) {
        switch (operator) {
            case "+": return a.add(b);
            case "-": return a.subtract(b);
            case "*": {
                BigDecimal r = a.multiply(b);
                if (r.abs().compareTo(BigDecimal.valueOf(MAX_INPUT_VALUE * MAX_INPUT_VALUE)) > 0)
                    throw new ArithmeticException("Result too large");
                return r;
            }
            case "/":
                if (b.compareTo(BigDecimal.ZERO) == 0)
                    throw new ArithmeticException("Division by zero");
                return a.divide(b, mathContext);
            case "%":
                if (b.compareTo(BigDecimal.ZERO) == 0)
                    throw new ArithmeticException("Division by zero");
                return a.remainder(b, mathContext);
            case "^": {
                double exp = b.doubleValue();
                if (Math.abs(exp) > MAX_EXPONENT)
                    throw new ArithmeticException("Exponent too large (max " + (int)MAX_EXPONENT + ")");
                double result = Math.pow(a.doubleValue(), exp);
                if (Double.isNaN(result) || Double.isInfinite(result))
                    throw new ArithmeticException("Invalid power result");
                return BigDecimal.valueOf(result);
            }
            default: throw new IllegalArgumentException("Unknown operator: " + operator);
        }
    }
}
