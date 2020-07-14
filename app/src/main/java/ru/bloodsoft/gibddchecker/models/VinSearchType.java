package ru.bloodsoft.gibddchecker.models;


import java.util.HashMap;

public class VinSearchType {
    private static final HashMap<String, String> ITEMS_MAP = new HashMap<>(2);
    private static final HashMap<String, Integer> POSITION_MAP = new HashMap<>(2);

    static {
        addItem(new VinSearchType.SearchType("history", "История регистрации"));
        addItem(new VinSearchType.SearchType("aiusdtp", "ДТП"));
        addItem(new VinSearchType.SearchType("wanted", "Розыск автомобиля"));
        addItem(new VinSearchType.SearchType("restricted", "Запрет регистрации"));
        addItem(new VinSearchType.SearchType("reestr", "Реестр залогов"));
        addItem(new VinSearchType.SearchType("decoder", "Расшифровка VIN"));
        addItem(new VinSearchType.SearchType("mileage", "Пробег авто"));
    }

    static {
        addItemPosition(new VinSearchType.SearchPosition("history", 1));
        addItemPosition(new VinSearchType.SearchPosition("aiusdtp", 2));
        addItemPosition(new VinSearchType.SearchPosition("wanted", 3));
        addItemPosition(new VinSearchType.SearchPosition("restricted", 4));
        addItemPosition(new VinSearchType.SearchPosition("reestr", 5));
        addItemPosition(new VinSearchType.SearchPosition("decoder", 6));
        addItemPosition(new VinSearchType.SearchPosition("mileage", 7));
    }

    private static void addItem(SearchType item) {
        ITEMS_MAP.put(item.id, item.name);
    }

    private static void addItemPosition(SearchPosition item) {
        POSITION_MAP.put(item.id, item.position);
    }

    public static class SearchType {
        public final String id;
        public final String name;

        public SearchType(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public static class SearchPosition {
        public final String id;
        public final Integer position;

        public SearchPosition(String id, Integer position) {
            this.id = id;
            this.position = position;
        }
    }

    public String getSearchType(String type) {
        return ITEMS_MAP.get(type);
    }

    public Integer getSearchPosition(String type) {
        return POSITION_MAP.get(type);
    }
}
