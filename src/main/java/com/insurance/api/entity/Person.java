package com.insurance.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@DiscriminatorValue("PERSON")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Person extends Client {

    @NotNull(message = "Birthdate is required for Person")
    @Past(message = "Birthdate must be in the past")
    @Column(updatable = false)
    private LocalDate birthdate;

    @Override
    public String getClientType() {
        return "PERSON";
    }
}