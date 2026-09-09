package com.codewalnut.productcatalog.dto;

import lombok.Value;

@Value
public class InfoResponse {

    String application;
    String version;
    String status;
}
