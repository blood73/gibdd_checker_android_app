package ru.bloodsoft.gibddchecker.models;

import java.util.HashMap;

public class Regions {

    private static final HashMap<String, String> ITEMS_MAP = new HashMap<>();

    static {
        addItem(new Regions.Region("Республика Адыгея", "1"));
        addItem(new Regions.Region("Республика Башкортостан", "2"));
        addItem(new Regions.Region("Республика Бурятия", "3"));
        addItem(new Regions.Region("Республика Алтай", "4"));
        addItem(new Regions.Region("Республика Дагестан", "5"));
        addItem(new Regions.Region("Республика Ингушетия", "6"));
        addItem(new Regions.Region("Кабардино-Балкария", "7"));
        addItem(new Regions.Region("Республика Калмыкия", "8"));
        addItem(new Regions.Region("Карачаево-Черкесия", "9"));
        addItem(new Regions.Region("Республика Карелия", "10"));
        addItem(new Regions.Region("Республика Коми", "11"));
        addItem(new Regions.Region("Республика Марий-Эл", "12"));
        addItem(new Regions.Region("Республика Мордовия", "13"));
        addItem(new Regions.Region("Республика Саха (Якутия)", "14"));
        addItem(new Regions.Region("Северная Осетия-Алания", "15"));
        addItem(new Regions.Region("Республика Татарстан", "16"));
        addItem(new Regions.Region("Республика Тыва", "17"));
        addItem(new Regions.Region("Удмуртская Республика", "18"));
        addItem(new Regions.Region("Республика Хакасия", "19"));
        addItem(new Regions.Region("Чеченская Республика", "20"));
        addItem(new Regions.Region("Чувашская Республика", "21"));
        addItem(new Regions.Region("Алтайский край", "22"));
        addItem(new Regions.Region("Краснодарский край", "23"));
        addItem(new Regions.Region("Красноярский край", "24"));
        addItem(new Regions.Region("Приморский край", "25"));
        addItem(new Regions.Region("Ставропольский край", "26"));
        addItem(new Regions.Region("Хабаровский край", "27"));
        addItem(new Regions.Region("Амурская область", "28"));
        addItem(new Regions.Region("Архангельская область", "29"));
        addItem(new Regions.Region("Астраханская область", "30"));
        addItem(new Regions.Region("Белгородская область", "31"));
        addItem(new Regions.Region("Брянская область", "32"));
        addItem(new Regions.Region("Владимирская область", "33"));
        addItem(new Regions.Region("Волгоградская область", "34"));
        addItem(new Regions.Region("Вологодская область", "35"));
        addItem(new Regions.Region("Воронежская область", "36"));
        addItem(new Regions.Region("Ивановская область", "37"));
        addItem(new Regions.Region("Иркутская область", "38"));
        addItem(new Regions.Region("Калининградская область", "39"));
        addItem(new Regions.Region("Калужская область", "40"));
        addItem(new Regions.Region("Камчатский край", "41"));
        addItem(new Regions.Region("Кемеровская область", "42"));
        addItem(new Regions.Region("Кировская область", "43"));
        addItem(new Regions.Region("Костромская область", "44"));
        addItem(new Regions.Region("Курганская область", "45"));
        addItem(new Regions.Region("Курская область", "46"));
        addItem(new Regions.Region("Ленинградская область", "47"));
        addItem(new Regions.Region("Липецкая область", "48"));
        addItem(new Regions.Region("Магаданская область", "49"));
        addItem(new Regions.Region("Московская область", "50"));
        addItem(new Regions.Region("Мурманская область", "51"));
        addItem(new Regions.Region("Нижегородская область", "52"));
        addItem(new Regions.Region("Новгородская область", "53"));
        addItem(new Regions.Region("Новосибирская область", "54"));
        addItem(new Regions.Region("Омская область", "55"));
        addItem(new Regions.Region("Оренбургская область", "56"));
        addItem(new Regions.Region("Орловская область", "57"));
        addItem(new Regions.Region("Пензенская область", "58"));
        addItem(new Regions.Region("Пермский край", "59"));
        addItem(new Regions.Region("Псковская область", "60"));
        addItem(new Regions.Region("Ростовская область", "61"));
        addItem(new Regions.Region("Рязанская область", "62"));
        addItem(new Regions.Region("Самарская область", "63"));
        addItem(new Regions.Region("Саратовская область", "64"));
        addItem(new Regions.Region("Сахалинская область", "65"));
        addItem(new Regions.Region("Свердловская область", "66"));
        addItem(new Regions.Region("Смоленская область", "67"));
        addItem(new Regions.Region("Тамбовская область", "68"));
        addItem(new Regions.Region("Тверская область", "69"));
        addItem(new Regions.Region("Томская область", "70"));
        addItem(new Regions.Region("Тульская область", "71"));
        addItem(new Regions.Region("Тюменская область", "72"));
        addItem(new Regions.Region("Ульяновская область", "73"));
        addItem(new Regions.Region("Челябинская область", "74"));
        addItem(new Regions.Region("Забайкальский край", "75"));
        addItem(new Regions.Region("Ярославская область", "76"));
        addItem(new Regions.Region("Москва", "77"));
        addItem(new Regions.Region("Санкт-Петербург", "78"));
        addItem(new Regions.Region("Еврейская АО", "79"));
        addItem(new Regions.Region("Ненецкий АО", "80"));
        addItem(new Regions.Region("Ханты-Мансийский АО", "81"));
        addItem(new Regions.Region("Чукотский АО", "82"));
        addItem(new Regions.Region("Ямало-Ненецкий АО", "83"));
        addItem(new Regions.Region("Республика Крым", "84"));
        addItem(new Regions.Region("Севастополь", "85"));
    }

    private static void addItem(Region item) {
        ITEMS_MAP.put(item.name, item.code);
    }

    public static class Region {
        public final String name;
        public final String code;

        public Region(String name, String code) {
            this.name = name;
            this.code = code;
        }
    }

    public String getRegionIdByName(String regionName) {
        return ITEMS_MAP.get(regionName);
    }
}