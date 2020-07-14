package ru.bloodsoft.gibddchecker.models;

public class FsspItem {
    private String name;
    private String exeProduction;
    private String details;
    private String subject;
    private String department;
    private String bailiff;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExeProduction() {
        return exeProduction;
    }

    public void setExeProduction(String exeProduction) {
        this.exeProduction = exeProduction;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getBailiff() {
        return bailiff;
    }

    public void setBailiff(String bailiff) {
        this.bailiff = bailiff;
    }
}