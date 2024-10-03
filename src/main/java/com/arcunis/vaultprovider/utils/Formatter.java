package com.arcunis.vaultprovider.utils;

import java.util.Map;

public class Formatter {

    public static String formatString(String template, Map<String, ?> data) {
        // Iterate over the map and replace all placeholders
        for (Map.Entry<String, ?> entry : data.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}"; // Placeholder pattern without regex escaping
            Object value = entry.getValue();

            // Convert value to string, properly formatting numeric values to remove ".0" if unnecessary
            String valueString;
            if (value instanceof Double || value instanceof Float) {
                valueString = String.format("%.2f", value); // Format to two decimal places
                if (valueString.endsWith(".00")) {
                    valueString = valueString.substring(0, valueString.length() - 3); // Remove ".00" if unnecessary
                }
            } else {
                valueString = value.toString();
            }

            template = template.replace(placeholder, valueString);
        }
        return template;
    }

    public static String formatNumber(double number, int decimalPlaces) {
        if (number >= 1_000_000_000) {
            return formatWithSuffix(number, 1_000_000_000, "B", decimalPlaces);
        } else if (number >= 1_000_000) {
            return formatWithSuffix(number, 1_000_000, "M", decimalPlaces);
        } else if (number >= 1_000) {
            return formatWithSuffix(number, 1_000, "K", decimalPlaces);
        } else {
            return String.valueOf((int) number);
        }
    }

    private static String formatWithSuffix(double number, int divisor, String suffix, int decimalPlaces) {
        double result = number / divisor;
        String format = "%." + decimalPlaces + "f";
        return String.format(format, result) + suffix;
    }



}
