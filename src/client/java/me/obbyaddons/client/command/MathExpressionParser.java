package me.obbyaddons.client.command;

public final class MathExpressionParser {

    private final String input;
    private int position;

    private MathExpressionParser(String input) {
        this.input = input.replaceAll("\\s+", "");
    }

    public static double evaluate(String input) {

        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Empty expression");
        }

        MathExpressionParser parser =
                new MathExpressionParser(input);

        double result =
                parser.parseExpression();

        if (parser.position != parser.input.length()) {
            throw new IllegalArgumentException("Unexpected input");
        }

        return result;
    }

    private double parseExpression() {

        double value =
                parseTerm();

        while (true) {

            if (match('+')) {
                value += parseTerm();

            } else if (match('-')) {
                value -= parseTerm();

            } else {
                return value;
            }
        }
    }

    private double parseTerm() {

        double value =
                parseUnary();

        while (true) {

            if (match('*')) {
                value *= parseUnary();

            } else if (match('/')) {

                double divisor =
                        parseUnary();

                if (divisor == 0) {
                    throw new IllegalArgumentException(
                            "Division by zero"
                    );
                }

                value /= divisor;

            } else {
                return value;
            }
        }
    }

    private double parseUnary() {

        if (match('+')) {
            return parseUnary();
        }

        if (match('-')) {
            return -parseUnary();
        }

        return parsePrimary();
    }

    private double parsePrimary() {

        if (match('(')) {

            double value =
                    parseExpression();

            if (!match(')')) {
                throw new IllegalArgumentException(
                        "Missing closing parenthesis"
                );
            }

            return value;
        }

        return parseNumber();
    }

    private double parseNumber() {

        int start =
                position;

        boolean decimalSeen =
                false;

        while (position < input.length()) {

            char current =
                    input.charAt(position);

            if (Character.isDigit(current)) {
                position++;
                continue;
            }

            if (current == '.' && !decimalSeen) {
                decimalSeen = true;
                position++;
                continue;
            }

            break;
        }

        if (start == position) {
            throw new IllegalArgumentException(
                    "Expected number"
            );
        }

        return Double.parseDouble(
                input.substring(
                        start,
                        position
                )
        );
    }

    private boolean match(char expected) {

        if (position >= input.length()) {
            return false;
        }

        if (input.charAt(position) != expected) {
            return false;
        }

        position++;
        return true;
    }
}