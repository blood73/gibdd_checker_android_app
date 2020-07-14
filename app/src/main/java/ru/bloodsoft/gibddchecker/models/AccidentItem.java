package ru.bloodsoft.gibddchecker.models;

import java.util.List;

public class AccidentItem {
    private String vehicleModel;
    private String vehicleMark;
    private String vehicleYear;
    private String vehicleDamageState;
    private String regionName;
    private String accidentTime;
    private String accidentNumber;
    private String accidentType;
    private List damagePoints;

    public String getAccidentTime() {
        return accidentTime;
    }

    public void setAccidentTime(String accidentTime) {
        this.accidentTime = accidentTime;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public String getVehicleDamageState() {
        return vehicleDamageState;
    }

    public void setVehicleDamageState(String vehicleDamageState) {
        this.vehicleDamageState = vehicleDamageState;
    }

    public String getVehicleMark() {
        return vehicleMark;
    }

    public void setVehicleMark(String vehicleMark) {
        this.vehicleMark = vehicleMark;
    }

    public String getVehicleYear() {
        return vehicleYear;
    }

    public void setVehicleYear(String vehicleYear) {
        this.vehicleYear = vehicleYear;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getAccidentNumber() {
        return accidentNumber;
    }

    public void setAccidentNumber(String accidentNumber) {
        this.accidentNumber = accidentNumber;
    }

    public String getAccidentType() {
        return accidentType;
    }

    public void setAccidentType(String accidentType) {
        this.accidentType = accidentType;
    }

    public List getDamagePoints() {
        return damagePoints;
    }

    public void setDamagePoints(List damagePoints) {
        this.damagePoints = damagePoints;
    }
}
