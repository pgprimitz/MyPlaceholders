package dev.nutellim.MyPlaceholders.models;

public enum PlaceholderType {
    STRING,
    RANDOM,
    COLORED_TEXT,
    MATH,
    REQUIREMENT,
    ANIMATION,
    PROGRESS;

    public static PlaceholderType find(String type) {
        if (type == null) return STRING;
        try {
            return PlaceholderType.valueOf(type.toUpperCase());
        } catch (Exception exception) {
            return STRING;
        }
    }
}
