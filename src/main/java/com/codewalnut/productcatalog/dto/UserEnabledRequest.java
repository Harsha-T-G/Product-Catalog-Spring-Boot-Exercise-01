package com.codewalnut.productcatalog.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserEnabledRequest {

    @NotNull
    private Boolean enabled;
}
