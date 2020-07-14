package ru.bloodsoft.gibddchecker.models;


public class RestrictedItem {
    private String regname;
    private String dateogr;
    private String ogrkod;
    private String tsmodel;
    private String phone;
    private String divtype;
    private String osnOgr;

    public String getRegName() {
        return regname;
    }

    public void setRegName(String regname) {
        this.regname = regname;
    }

    public String getDateOgr() {
        return dateogr;
    }

    public void setDateOgr(String dateogr) {
        this.dateogr = dateogr;
    }

    public String getOgrKod() {
        return ogrkod;
    }

    public void setOgrKod(String ogrkod) {
        this.ogrkod = ogrkod;
    }

    public String getTsmodel() {
        return tsmodel;
    }

    public void setTsmodel(String tsmodel) {
        this.tsmodel = tsmodel;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDivType() {
        return divtype;
    }

    public void setDivType(String divtype) {
        this.divtype = divtype;
    }

    public String getOsnOgr() {
        return osnOgr;
    }

    public void setOsnOgr(String osnOgr) {
        this.osnOgr = osnOgr;
    }
}
