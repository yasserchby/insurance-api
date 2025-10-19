package com.insurance.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class PersonDTO extends ClientDTO {

    @NotNull(message = "Birthdate is required for Person")
    @Past(message = "Birthdate must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthdate;

    public PersonDTO() {
        setType("PERSON");
    }
}