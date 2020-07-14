package ru.bloodsoft.gibddchecker.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VehicleTypes {

    private static final HashMap<String, String> ITEMS_MAP = new HashMap<>(2);

    static {
        addItem(new VehicleTypes.VehicleType("01", "Грузовые автомобили бортовые"));
        addItem(new VehicleTypes.VehicleType("02", "Грузовые автомобили шасси"));
        addItem(new VehicleTypes.VehicleType("03", "Грузовые автомобили фургоны"));
        addItem(new VehicleTypes.VehicleType("04", "Грузовые автомобили тягачи седельные"));
        addItem(new VehicleTypes.VehicleType("05", "Грузовые автомобили самосвалы"));
        addItem(new VehicleTypes.VehicleType("06", "Грузовые автомобили рефрижераторы"));
        addItem(new VehicleTypes.VehicleType("07", "Грузовые автомобили цистерны"));
        addItem(new VehicleTypes.VehicleType("08", "Грузовые автомобили с гидроманипулятором"));
        addItem(new VehicleTypes.VehicleType("09", "Грузовые автомобили прочие"));
        addItem(new VehicleTypes.VehicleType("21", "Легковые автомобили универсал"));
        addItem(new VehicleTypes.VehicleType("22", "Легковые автомобили комби (хэтчбек)"));
        addItem(new VehicleTypes.VehicleType("23", "Легковые автомобили седан"));
        addItem(new VehicleTypes.VehicleType("24", "Легковые автомобили лимузин"));
        addItem(new VehicleTypes.VehicleType("25", "Легковые автомобили купе"));
        addItem(new VehicleTypes.VehicleType("26", "Легковые автомобили кабриолет"));
        addItem(new VehicleTypes.VehicleType("27", "Легковые автомобили фаэтон"));
        addItem(new VehicleTypes.VehicleType("28", "Легковые автомобили пикап"));
        addItem(new VehicleTypes.VehicleType("29", "Легковые автомобили прочие"));
        addItem(new VehicleTypes.VehicleType("41", "Автобусы длиной не более 5 м"));
        addItem(new VehicleTypes.VehicleType("42", "Автобусы длиной более 5 м, но не более 8 м"));
        addItem(new VehicleTypes.VehicleType("43", "Автобусы длиной более 8 м, но не более 12 м"));
        addItem(new VehicleTypes.VehicleType("44", "Автобусы сочлененные длиной более 12 м"));
        addItem(new VehicleTypes.VehicleType("49", "Автобусы прочие"));
        addItem(new VehicleTypes.VehicleType("51", "Специализированные автомобили автоцистерны"));
        addItem(new VehicleTypes.VehicleType("52", "Специализированные автомобили санитарные"));
        addItem(new VehicleTypes.VehicleType("53", "Специализированные автомобили автокраны"));
        addItem(new VehicleTypes.VehicleType("54", "Специализированные автомобили заправщики"));
        addItem(new VehicleTypes.VehicleType("55", "Специализированные автомобили мастерские"));
        addItem(new VehicleTypes.VehicleType("56", "Специализированные автомобили автопогрузчики"));
        addItem(new VehicleTypes.VehicleType("57", "Специализированные автомобили эвакуаторы"));
        addItem(new VehicleTypes.VehicleType("58", "Специализированные пассажирские транспортные средства"));
        addItem(new VehicleTypes.VehicleType("59", "Специализированные автомобили прочие"));
        addItem(new VehicleTypes.VehicleType("71", "Мотоциклы"));
        addItem(new VehicleTypes.VehicleType("72", "Мотороллеры и мотоколяски"));
        addItem(new VehicleTypes.VehicleType("73", "Мотовелосипеды и мопеды"));
        addItem(new VehicleTypes.VehicleType("74", "Мотонарты"));
        addItem(new VehicleTypes.VehicleType("80", "Прицепы самосвалы"));
        addItem(new VehicleTypes.VehicleType("81", "Прицепы к легковым автомобилям"));
        addItem(new VehicleTypes.VehicleType("82", "Прицепы общего назначения к грузовым автомобилям"));
        addItem(new VehicleTypes.VehicleType("83", "Прицепы цистерны"));
        addItem(new VehicleTypes.VehicleType("84", "Прицепы тракторные"));
        addItem(new VehicleTypes.VehicleType("85", "Прицепы вагоны-дома передвижные"));
        addItem(new VehicleTypes.VehicleType("86", "Прицепы со специализированными кузовами"));
        addItem(new VehicleTypes.VehicleType("87", "Прицепы трейлеры"));
        addItem(new VehicleTypes.VehicleType("88", "Прицепы автобуса"));
        addItem(new VehicleTypes.VehicleType("89", "Прицепы прочие"));
        addItem(new VehicleTypes.VehicleType("91", "Полуприцепы с бортовой платформой"));
        addItem(new VehicleTypes.VehicleType("92", "Полуприцепы самосвалы"));
        addItem(new VehicleTypes.VehicleType("93", "Полуприцепы фургоны"));
        addItem(new VehicleTypes.VehicleType("95", "Полуприцепы цистерны"));
        addItem(new VehicleTypes.VehicleType("99", "Полуприцепы прочие"));
        addItem(new VehicleTypes.VehicleType("31", "Трактора"));
        addItem(new VehicleTypes.VehicleType("32", "Самоходные машины и механизмы"));
        addItem(new VehicleTypes.VehicleType("33", "Трамваи"));
        addItem(new VehicleTypes.VehicleType("34", "Троллейбусы"));
        addItem(new VehicleTypes.VehicleType("35", "Велосипеды"));
        addItem(new VehicleTypes.VehicleType("36", "Гужевой транспорт"));
        addItem(new VehicleTypes.VehicleType("38", "Подвижной состав железных дорог"));
        addItem(new VehicleTypes.VehicleType("39", "Иной"));
    }

    private static void addItem(VehicleType item) {
        ITEMS_MAP.put(item.id, item.name);
    }

    public static class VehicleType {
        public final String id;
        public final String name;

        public VehicleType(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public String getVehicleType(String type) {
        return ITEMS_MAP.get(type);
    }
}
