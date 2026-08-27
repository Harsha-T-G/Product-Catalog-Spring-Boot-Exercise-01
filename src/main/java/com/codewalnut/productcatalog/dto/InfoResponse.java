package com.codewalnut.productcatalog.dto;

public class InfoResponse {

    private final String application;
    private final String version;
    private final String status;

    public InfoResponse(String application, String version, String status) {
        this.application = application;
        this.version = version;
        this.status = status;
    }

    public String getApplication() {
        return application;
    }

    public String getVersion() {
        return version;
    }

    public String getStatus() {
        return status;
    }
}
