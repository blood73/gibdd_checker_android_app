package ru.bloodsoft.gibddchecker.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Divisions {
    public static List<Division> ITEMS = new ArrayList<>();
    public static Map<String, Division> ITEM_MAP = new HashMap<>(3);

    public static class Division {
        public final String divId;
        public final String fulladdr;
        public final String coords;

        public static void addItem(Division item) {
            ITEMS.add(item);
            ITEM_MAP.put(item.divId, item);
        }

        public Division(String id, String fulladdr, String coords) {
            this.divId = id;
            this.fulladdr = fulladdr;
            this.coords = coords;
        }
    }

    public Division getDivisionByDivId(String divId) {
        return ITEM_MAP.get(divId);
    }
}