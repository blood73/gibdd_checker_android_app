package ru.bloodsoft.gibddchecker.models;

import java.util.HashMap;
import java.util.List;

public class DamagePoints {

    private static final HashMap<String, String> ITEMS_MAP = new HashMap<>(2);

    static {
        addItem(new DamagePoints.DamagePoint("01", "Правая часть переднего бампера"));
        addItem(new DamagePoints.DamagePoint("02", "Правое переднее крыло и/или правая передняя дверь"));
        addItem(new DamagePoints.DamagePoint("03", "Правое заднее крыло и/или правая задняя дверь"));
        addItem(new DamagePoints.DamagePoint("04", "Правая часть заднего бампера"));
        addItem(new DamagePoints.DamagePoint("05", "Левая часть заднего бампера"));
        addItem(new DamagePoints.DamagePoint("06", "Левое заднее крыло и/или левая задняя дверь"));
        addItem(new DamagePoints.DamagePoint("07", "Левое переднее крыло и/или левая передняя дверь"));
        addItem(new DamagePoints.DamagePoint("08", "Левая часть переднего бампера"));
        addItem(new DamagePoints.DamagePoint("09", "Крыша"));
        addItem(new DamagePoints.DamagePoint("10", "Повреждения днища"));
        addItem(new DamagePoints.DamagePoint("11", "Полная деформация кузова"));
        addItem(new DamagePoints.DamagePoint("12", "Смещение двигателя"));
        addItem(new DamagePoints.DamagePoint("13", "Смещение переднего моста"));
        addItem(new DamagePoints.DamagePoint("14", "Смещение заднего моста"));
        addItem(new DamagePoints.DamagePoint("15", "Возгорание"));
    }

    private static void addItem(DamagePoint item) {
        ITEMS_MAP.put(item.id, item.name);
    }

    public static class DamagePoint {
        public final String id;
        public final String name;

        public DamagePoint(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public String getDescriptionByDamagePoints(List damagePoints) {
        String damage_description = "";
        for (int i = 0; i < damagePoints.size(); i++) {
            damage_description = damage_description + ITEMS_MAP.get(damagePoints.get(i).toString());
            if (i < damagePoints.size() - 1) {
                damage_description = damage_description + "; ";
            }
        }
        return damage_description;
    }
}
