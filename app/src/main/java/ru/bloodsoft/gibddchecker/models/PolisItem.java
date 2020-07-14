package ru.bloodsoft.gibddchecker.models;

public class PolisItem {
    private String isRestrict;
    private String insCompanyName;
    private String policyBsoSerial;
    private String policyBsoNumber;

    public String getIsRestrict() {
        return isRestrict;
    }

    public void setIsRestrict(String isRestrict) {
        this.isRestrict = isRestrict;
    }

    public String getInsCompanyName() {
        return insCompanyName;
    }

    public void setInsCompanyName(String insCompanyName) {
        this.insCompanyName = insCompanyName;
    }

    public String getPolicyBsoSerial() {
        return policyBsoSerial;
    }

    public void setPolicyBsoSerial(String policyBsoSerial) {
        this.policyBsoSerial = policyBsoSerial;
    }

    public String getPolicyBsoNumber() {
        return policyBsoNumber;
    }

    public void setPolicyBsoNumber(String policyBsoNumber) {
        this.policyBsoNumber = policyBsoNumber;
    }
}