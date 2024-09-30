package com.arcunis.vaultprovider.utils;

import java.util.Map;

public class Formatter {

    public static String format(String template, Map<String, ?> data) {
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


}
