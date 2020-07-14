package ru.bloodsoft.gibddchecker.models;

public class ApiResponse {
    private String response;
    private boolean isSuccess;

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }
}
