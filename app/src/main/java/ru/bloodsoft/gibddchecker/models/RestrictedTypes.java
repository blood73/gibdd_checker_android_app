package ru.bloodsoft.gibddchecker.models;

import java.util.HashMap;

public class RestrictedTypes {
    private static final HashMap<String, String> ITEMS_MAP = new HashMap<>(2);

    static {
        addItem(new RestrictedTypes.RestrictedType("0", ""));
        addItem(new RestrictedTypes.RestrictedType("1", "Запрет на регистрационные действия"));
        addItem(new RestrictedTypes.RestrictedType("2", "Запрет на снятие с учета"));
        addItem(new RestrictedTypes.RestrictedType("3", "Запрет на регистрационные действия и прохождение ГТО"));
        addItem(new RestrictedTypes.RestrictedType("4", "Утилизация (для транспорта не старше 5 лет)"));
        addItem(new RestrictedTypes.RestrictedType("5", "Аннулирование"));
    }

    private static void addItem(RestrictedType item) {
        ITEMS_MAP.put(item.id, item.name);
    }

    public static class RestrictedType {
        public final String id;
        public final String name;

        public RestrictedType(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public String getRestrictedType(String type) {
        return ITEMS_MAP.get(type);
    }
}