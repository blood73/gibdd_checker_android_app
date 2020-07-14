package ru.bloodsoft.gibddchecker.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.bloodsoft.gibddchecker.R;

/**
 * Gibdd content
 *
 */
public class GibddContent {

    /**
     * An array of sample items.
     */
    public static List<DummyItem> ITEMS = new ArrayList<>();

    /**
     * A map of sample items. Key: sample ID; Value: Item.
     */
    public static Map<String, DummyItem> ITEM_MAP = new HashMap<>(10);

    static {
        addItem(new DummyItem("1", R.drawable.p1, R.drawable.p1icon, "История регистрации", "Получение основных сведений о транспортном средстве и периодах его регистрации в Госавтоинспекции за различными собственниками. Внимание! VIN код / (номер рамы) может содержать только цифры и английский буквы."));
        addItem(new DummyItem("2", R.drawable.p2, R.drawable.p2icon, "Участие в ДТП", "Получение сведений о дорожно-транспортных происшествиях с участием транспортного средства с указанным идентификационным номером (VIN). Внимание! VIN код / (номер рамы) может содержать только цифры и английский буквы."));
        addItem(new DummyItem("3", R.drawable.p3, R.drawable.p3icon, "Нахождение в розыске", "Получение сведений о федеральном розыске транспортного средства правоохранительными органами. Внимание! VIN код / (номер рамы) может содержать только цифры и английский буквы."));
        addItem(new DummyItem("4", R.drawable.p4, R.drawable.p4icon, "Наличие ограничений", "Получение сведений о наличии ограничений на регистрационные действия в Госавтоинспекции с транспортным средством. Внимание! VIN код / (номер рамы) может содержать только цифры и английский буквы."));
        addItem(new DummyItem("0", R.drawable.p1, R.drawable.pdf_icon, "Полный PDF-отчет по автомобилю", ""));
        addItem(new DummyItem("5", R.drawable.tehosmotr, R.drawable.tehosmotr, "Пройти ТЕХОСМОТР", ""));
        addItem(new DummyItem("6", R.drawable.sravni, R.drawable.sravni, "КАСКО со скидкой", ""));
        addItem(new DummyItem("12", R.drawable.autospot, R.drawable.autospot, "Купить новое авто", ""));
        addItem(new DummyItem("13", R.drawable.turbopolis, R.drawable.turbopolis, "Оформить ОСАГО", ""));
        addItem(new DummyItem("11", R.drawable.shtrafi, R.drawable.shtrafi, "Оплатить штрафы", ""));
        addItem(new DummyItem("7", R.drawable.perekup1, R.drawable.perekup1, "Срочный выкуп авто", ""));
        addItem(new DummyItem("8", R.drawable.perekup2, R.drawable.perekup2, "Помощь в подборе авто", ""));
        addItem(new DummyItem("9", R.drawable.perekup3, R.drawable.perekup3, "При поддержке Перекуп-Клуб", ""));
    }

    private static void addItem(DummyItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    public static class DummyItem {
        public final String id;
        public final int photoId;
        public final int iconId;
        public final String title;
        public final String content;

        public DummyItem(String id, int photoId, int iconId, String title,
                         String content) {
            this.id = id;
            this.photoId = photoId;
            this.iconId = iconId;
            this.title = title;
            this.content = content;
        }
    }
}
