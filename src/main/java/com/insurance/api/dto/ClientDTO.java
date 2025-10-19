package com.insurance.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PersonDTO.class, name = "PERSON"),
        @JsonSubTypes.Type(value = CompanyDTO.class, name = "COMPANY")
})
public abstract class ClientDTO {

    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @Pattern(regexp = "^\\+[0-9]{10,15}$", message = "Phone must start with + and contain 10-15 digits")
    @NotBlank(message = "Phone is required")
    private String phone;

    private String type;
}