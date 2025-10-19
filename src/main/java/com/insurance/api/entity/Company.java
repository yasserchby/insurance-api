package com.insurance.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("COMPANY")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Company extends Client {

    @NotBlank(message = "Company identifier is required")
    @Pattern(regexp = "^[A-Za-z0-9]+-[A-Za-z0-9.]+$",
            message = "Company identifier must match pattern: xxx-xxx")
    @Column(unique = true, updatable = false)
    private String companyIdentifier;

    @Override
    public String getClientType() {
        return "COMPANY";
    }
}