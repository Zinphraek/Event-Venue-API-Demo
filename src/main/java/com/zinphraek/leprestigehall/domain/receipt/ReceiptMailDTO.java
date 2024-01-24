package com.zinphraek.leprestigehall.domain.receipt;

import jakarta.validation.constraints.NotBlank;

public record ReceiptMailDTO(
    @NotBlank String address,
    @NotBlank String subject,
    @NotBlank String body
) {
}
