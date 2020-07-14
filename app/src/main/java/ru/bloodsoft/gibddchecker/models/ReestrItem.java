package ru.bloodsoft.gibddchecker.models;

public class ReestrItem {
    private String RegDate;
    private String RegInfoText;
    private String Pledgor;
    private String Mortgagees;

    public String getRegDate() {
        return RegDate;
    }

    public void setRegDate(String regDate) {
        this.RegDate = regDate;
    }

    public String getRegInfoText() {
        return RegInfoText;
    }

    public void setRegInfoText(String regInfoText) {
        this.RegInfoText = regInfoText;
    }

    public String getPledgor() {
        return Pledgor;
    }

    public void setPledgor(String pledgor) {
        this.Pledgor = pledgor;
    }

    public String getMortgagees() {
        return Mortgagees;
    }

    public void setMortgagees(String mortgagees) {
        this.Mortgagees = mortgagees;
    }
}