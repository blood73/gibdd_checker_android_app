package ru.bloodsoft.gibddchecker.models;

import java.util.HashMap;

public class TypeOperation {
    private static final HashMap<String, String> ITEMS_MAP = new HashMap<>(2);

    static {
        addItem(new TypeOperation.Operation("01", "Регистрация новых, произведенных в России или везенных, а также ввезенных в Россию бывших в эксплуатации, в том числе временно на срок более 6 месяцев, испытательной техники."));
        addItem(new TypeOperation.Operation("02", "Ранее зарегистрированных в регистрирующих органах"));
        addItem(new TypeOperation.Operation("03", "Изменение собственника (владельца) в результате совершения сделки, вступления в наследство, слияние и разделение капитала у юридического лица, переход права по договору лизинга, судебные решения и др."));
        addItem(new TypeOperation.Operation("04", "Изменение данных о собственнике (владельце)"));
        addItem(new TypeOperation.Operation("05", "Изменение данных о транспортном средстве, в том числе изменение технических характеристик и (или) назначения (типа) транспортного средства"));
        addItem(new TypeOperation.Operation("06", "Выдача взамен утраченных или пришедших в негодность государственных регистрационных знаков, регистрационных документов, паспортов транспортных средств."));
        addItem(new TypeOperation.Operation("07", "Прекращение регистрации"));
        addItem(new TypeOperation.Operation("08", "Снятие с учета в связи с убытием за пределы Российской Федерации"));
        addItem(new TypeOperation.Operation("09", "Снятие с учета в связи с утилизацией"));
        addItem(new TypeOperation.Operation("11", "Первичная регистрация"));
        addItem(new TypeOperation.Operation("12", "Регистрация снятых с учета"));
        addItem(new TypeOperation.Operation("13", "Временная регистрация ТС (на срок проведения проверок, на срок временной прописки, регистрация испытательной техники)"));
        addItem(new TypeOperation.Operation("14", "Временный учет (временная регистрация места пребывания ТС без выдачи документов)"));
        addItem(new TypeOperation.Operation("15", "Регистрация ТС, ввезенных из-за пределов Российской Федерации"));
        addItem(new TypeOperation.Operation("16", "Регистрация ТС, прибывших из других регионов Российской Федерации"));
        addItem(new TypeOperation.Operation("17", "Регистрация ТС по новому месту жительства собственника, прибывшего из другого субъекта Российской Федерации"));
        addItem(new TypeOperation.Operation("21", "Постановка на постоянный учет в связи со сверкой"));
        addItem(new TypeOperation.Operation("22", "Постановка на оперативный учет"));
        addItem(new TypeOperation.Operation("23", "Постановка на постоянный учет"));
        addItem(new TypeOperation.Operation("47", "Наложение ограничений"));
        addItem(new TypeOperation.Operation("24", "Постановка в розыск утраченной спецпродукции"));
        addItem(new TypeOperation.Operation("25", "Постановка уничтоженной спецпродукции"));
        addItem(new TypeOperation.Operation("26", "Учет изготовленной и отгруженной спецпродукции (по информации предприятий-изготовителей)"));
        addItem(new TypeOperation.Operation("27", "Учет выданной и распределенной спецпродукции (по информации подразделений ГИБДД)"));
        addItem(new TypeOperation.Operation("28", "Учет закрепленной спецпродукции"));
        addItem(new TypeOperation.Operation("29", "Учет ПТС, выданных заводами-изготовителями"));
        addItem(new TypeOperation.Operation("30", "Учет ПТС, выданных таможенными органами"));
        addItem(new TypeOperation.Operation("31", "Резерв"));
        addItem(new TypeOperation.Operation("32", "Оформление первичного материала по административному правонарушению"));
        addItem(new TypeOperation.Operation("33", "Учет лиц в розыске"));
        addItem(new TypeOperation.Operation("34", "Учет утраченного оружия"));
        addItem(new TypeOperation.Operation("35", "Первичная выдача после обучения"));
        addItem(new TypeOperation.Operation("36", "Первичная выдача после самоподготовки"));
        addItem(new TypeOperation.Operation("37", "Замена в связи с утратой"));
        addItem(new TypeOperation.Operation("38", "Замена в связи с истечением срока действия"));
        addItem(new TypeOperation.Operation("39", "Открытие новой категории"));
        addItem(new TypeOperation.Operation("40", "Выдача международного водительского удостоверения"));
        addItem(new TypeOperation.Operation("41", "Замена государственного регистрационного знака"));
        addItem(new TypeOperation.Operation("42", "Выдача дубликата регистрационного документа"));
        addItem(new TypeOperation.Operation("43", "Выдача (замена) паспорта ТС"));
        addItem(new TypeOperation.Operation("44", "Замена номерного агрегата, цвета, изменение конструкции ТС"));
        addItem(new TypeOperation.Operation("45", "Изменение Ф.И.О. (наименования) владельца"));
        addItem(new TypeOperation.Operation("46", "Изменение места жительства (юридического адреса) владельца в пределах территории обслуживания регистрационным пунктом"));
        addItem(new TypeOperation.Operation("47", "Наличие запретов и ограничений"));
        addItem(new TypeOperation.Operation("48", "Снятие запретов и ограничений"));
        addItem(new TypeOperation.Operation("49", "Регистрация залога ТС"));
        addItem(new TypeOperation.Operation("50", "Прекращение регистрации залога ТС"));
        addItem(new TypeOperation.Operation("51", "Коррекция иных реквизитов"));
        addItem(new TypeOperation.Operation("52", "Выдача акта технического осмотра"));
        addItem(new TypeOperation.Operation("53", "Проведение ГТО"));
        addItem(new TypeOperation.Operation("54", "Постоянная регистрация ТС по окончанию временной"));
        addItem(new TypeOperation.Operation("55", "Коррекция реквизитов по информации налоговых органов"));
        addItem(new TypeOperation.Operation("56", "Коррекция реквизитов при проведении ГТО"));
        addItem(new TypeOperation.Operation("52", "Коррекция ОУ"));
        addItem(new TypeOperation.Operation("53", "Коррекция ПУ"));
        addItem(new TypeOperation.Operation("54", "Перевод с ОУ на ПУ"));
        addItem(new TypeOperation.Operation("55", "Коррекция в связи со сверкой"));
        addItem(new TypeOperation.Operation("52", "Коррекция наложенных ограничений"));
        addItem(new TypeOperation.Operation("56", "Коррекция реквизитов"));
        addItem(new TypeOperation.Operation("57", "Оформление этапа производства по делу об АП"));
        addItem(new TypeOperation.Operation("59", "Коррекция реквизитов"));
        addItem(new TypeOperation.Operation("48", "Снятие ограничений"));
        addItem(new TypeOperation.Operation("61", "В связи с изменением места регистрации"));
        addItem(new TypeOperation.Operation("62", "В связи с прекращением права собственности (отчуждение, конфискация ТС)"));
        addItem(new TypeOperation.Operation("63", "В связи с вывозом ТС за пределы Российской Федерации"));
        addItem(new TypeOperation.Operation("64", "В связи с окончанием срока временной регистрации"));
        addItem(new TypeOperation.Operation("65", "В связи с утилизацией"));
        addItem(new TypeOperation.Operation("66", "В связи с признанием регистрации недействительной"));
        addItem(new TypeOperation.Operation("67", "Снятие с временного учета"));
        addItem(new TypeOperation.Operation("68", "Снятие с учета в связи с кражей или угоном"));
        addItem(new TypeOperation.Operation("69", "Постановка с одновременным снятием с учета"));
        addItem(new TypeOperation.Operation("71", "С ОУ в связи с обнаружением"));
        addItem(new TypeOperation.Operation("72", "С ОУ за давностью лет"));
        addItem(new TypeOperation.Operation("73", "С ОУ в связи с не подтверждением"));
        addItem(new TypeOperation.Operation("74", "С ОУ в связи с переводом на ПУ"));
        addItem(new TypeOperation.Operation("75", "С ПУ в связи с обнаружением"));
        addItem(new TypeOperation.Operation("76", "С ПУ за давностью лет"));
        addItem(new TypeOperation.Operation("77", "Чистка ФКУ ГИАЦ МВД России"));
        addItem(new TypeOperation.Operation("78", "Наложенных ограничений"));
        addItem(new TypeOperation.Operation("81", "Документов в связи с обнаружением"));
        addItem(new TypeOperation.Operation("82", "Удаление ошибочно введенной записи"));
        addItem(new TypeOperation.Operation("83", "Удаление в связи со сверкой"));
        addItem(new TypeOperation.Operation("84", "Перевод в архив в связи с корректировкой"));
        addItem(new TypeOperation.Operation("91", "По наследству с заменой государственных регистрационных знаков"));
        addItem(new TypeOperation.Operation("92", "По наследству с сохранением государственных регистрационных знаков за новым собственником (наследником)"));
        addItem(new TypeOperation.Operation("93", "По сделкам, произведенным в любой форме (купля-продажа, дарение, др.) с заменой государственных регистрационных знаков"));
        addItem(new TypeOperation.Operation("94", "По сделкам, произведенным в любой форме (купля-продажа, дарение, др.) с сохранением государственных регистрационных"));
        addItem(new TypeOperation.Operation("95", "Учет прекращения действия водительского удостоверения"));
        addItem(new TypeOperation.Operation("96", "Учет восстановления действия водительского удостоверения"));
        addItem(new TypeOperation.Operation("97", "Учет приостановления действия права управления ТС по постановлению судебного пристава-исполнителя о временном"));
        addItem(new TypeOperation.Operation("98", "Учет отмены приостановления действия права управления ТС по постановлению судебного пристава-исполнителя о временном ограничении на пользование должников специальным правом"));
        addItem(new TypeOperation.Operation("", "Нет данных"));
    }

    private static void addItem(Operation item) {
        ITEMS_MAP.put(item.id, item.name);
    }

    public static class Operation {
        public final String id;
        public final String name;

        public Operation(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public String getDescriptionByTypeOperation(String id) {
        return ITEMS_MAP.get(id);
    }
}