package ru.bloodsoft.gibddchecker.util;

public class RegexUtil {
    // Assumptions: inputStr is a non-null String
    public static String extractFirstNumber(String inputStr) {
        String[] parts = inputStr.split("\\,"); // String array, each element is text between dots

        return parts[0];
    }
}