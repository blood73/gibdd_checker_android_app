package ru.bloodsoft.gibddchecker.models;

public class PlateItem {
    private String make;
    private String model;
    private String date;
    private String url;
    private String image;

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        if (make.equals("null")) {
            make = "";
        }

        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        if (model.equals("null")) {
            model = "";
        }

        this.model = model;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}