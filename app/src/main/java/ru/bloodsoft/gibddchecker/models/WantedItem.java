package ru.bloodsoft.gibddchecker.models;

public class WantedItem {
    private String reg_inic;
    private String w_data_pu;
    private String w_god_vyp;
    private String w_model;

    public String getRegInic() {
        return reg_inic;
    }

    public void setRegInic(String reg_inic) {
        this.reg_inic = reg_inic;
    }

    public String getDataPu() {
        return w_data_pu;
    }

    public void setDataPu(String w_data_pu) {
        this.w_data_pu = w_data_pu;
    }

    public String getGodVyp() {
        return w_god_vyp;
    }

    public void setGodVyp(String w_god_vyp) {
        this.w_god_vyp = w_god_vyp;
    }

    public String getModel() {
        return w_model;
    }

    public void setModel(String w_model) {
        this.w_model = w_model;
    }
}
