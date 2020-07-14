package ru.bloodsoft.gibddchecker.models;

import java.util.HashMap;

public class Organs {
    private static final HashMap<String, String> ITEMS_MAP = new HashMap<>();

    static {
        addItem(new Organs.Organ("0", "не предусмотренный код"));
        addItem(new Organs.Organ("1", "Судебные органы"));
        addItem(new Organs.Organ("2", "Судебный пристав"));
        addItem(new Organs.Organ("3", "Таможенные органы"));
        addItem(new Organs.Organ("4", "Органы социальной защиты"));
        addItem(new Organs.Organ("5", "Нотариус"));
        addItem(new Organs.Organ("6", "ОВД или иные правоохр. органы"));
        addItem(new Organs.Organ("7", "ОВД или иные правоохр. органы (прочие)"));
    }

    private static void addItem(Organ item) {
        ITEMS_MAP.put(item.id, item.name);
    }

    public static class Organ {
        public final String id;
        public final String name;

        public Organ(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public String getOrganType(String type) {
        return ITEMS_MAP.get(type);
    }
}