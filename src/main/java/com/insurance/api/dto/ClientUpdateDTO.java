package com.insurance.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ClientUpdateDTO {

    private String name;

    @Email(message = "Email must be valid")
    private String email;

    @Pattern(regexp = "^\\+[0-9]{10,15}$", message = "Phone must start with + and contain 10-15 digits")
    private String phone;
}