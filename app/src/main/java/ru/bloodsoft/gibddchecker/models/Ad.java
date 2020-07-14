package ru.bloodsoft.gibddchecker.models;

public class Ad {
    private Integer adNumber;
    private String adText;
    private String adUrl;

    public Integer getAdNumber() {
        return adNumber;
    }

    public void setAdNumber(Integer adNumber) {
        this.adNumber = adNumber;
    }

    public String getAdText() {
        return adText;
    }

    public void setAdText(String AdText) {
        this.adText = AdText;
    }

    public String getAdUrl() {
        return adUrl;
    }

    public void setAdUrl(String AdUrl) {
        this.adUrl = AdUrl;
    }
}