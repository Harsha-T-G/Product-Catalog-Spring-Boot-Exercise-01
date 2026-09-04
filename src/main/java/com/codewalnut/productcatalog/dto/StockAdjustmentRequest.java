package com.codewalnut.productcatalog.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockAdjustmentRequest {

    @NotNull(message = "Adjustment is required")
    private Integer adjustment;

    private Long version;
}
