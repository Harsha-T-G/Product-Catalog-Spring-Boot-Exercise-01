package com.codewalnut.productcatalog.controller;

import com.codewalnut.productcatalog.dto.InfoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class InfoController {

    private final String applicationName;
    private final String applicationVersion;

    public InfoController(
            @Value("${spring.application.name}") String applicationName,
            @Value("${info.app.version}") String applicationVersion) {
        this.applicationName = applicationName;
        this.applicationVersion = applicationVersion;
    }

    @GetMapping("/info")
    public InfoResponse getInfo() {
        return new InfoResponse(applicationName, applicationVersion, "UP");
    }
}
