package ru.bloodsoft.gibddchecker.models;

public class VehicleMake {
    private String imageUrl;
    private String data;
    private String rawResponse;
    private Boolean isNeedRequest;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String url) {
        this.imageUrl = url;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }

    public Boolean getIsNeedRequest() {
        return isNeedRequest;
    }

    public void setIsNeedRequest(Boolean isNeedRequest) {
        this.isNeedRequest = isNeedRequest;
    }
}