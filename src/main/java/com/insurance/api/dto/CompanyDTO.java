package com.insurance.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CompanyDTO extends ClientDTO {

    @NotBlank(message = "Company identifier is required")
    @Pattern(regexp = "^[A-Za-z0-9]+-[A-Za-z0-9.]+$",
            message = "Company identifier must match pattern: xxx-xxx")
    private String companyIdentifier;

    public CompanyDTO() {
        setType("COMPANY");
    }
}