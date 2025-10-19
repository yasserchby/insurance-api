package com.insurance.api.controller;

import com.insurance.api.dto.ClientUpdateDTO;
import com.insurance.api.dto.CompanyDTO;
import com.insurance.api.dto.PersonDTO;
import com.insurance.api.exception.ResourceNotFoundException;
import com.insurance.api.service.ClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClientService clientService;

    private PersonDTO personDTO;
    private CompanyDTO companyDTO;

    @BeforeEach
    void setUp() {
        personDTO = new PersonDTO();
        personDTO.setId(1L);
        personDTO.setName("John Doe");
        personDTO.setEmail("john@example.com");
        personDTO.setPhone("+41791234567");
        personDTO.setBirthdate(LocalDate.of(1990, 5, 15));

        companyDTO = new CompanyDTO();
        companyDTO.setId(2L);
        companyDTO.setName("TechCorp SA");
        companyDTO.setEmail("contact@techcorp.com");
        companyDTO.setPhone("+41227654321");
        companyDTO.setCompanyIdentifier("CHE-123.456.789");
    }

    @Test
    void createClient_ShouldReturn201_WhenValidPersonProvided() throws Exception {
        when(clientService.createClient(any(PersonDTO.class))).thenReturn(personDTO);

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(personDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.type").value("PERSON"));

        verify(clientService, times(1)).createClient(any(PersonDTO.class));
    }

    @Test
    void createClient_ShouldReturn201_WhenValidCompanyProvided() throws Exception {
        when(clientService.createClient(any(CompanyDTO.class))).thenReturn(companyDTO);

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(companyDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("TechCorp SA"))
                .andExpect(jsonPath("$.companyIdentifier").value("CHE-123.456.789"))
                .andExpect(jsonPath("$.type").value("COMPANY"));

        verify(clientService, times(1)).createClient(any(CompanyDTO.class));
    }

    @Test
    void createClient_ShouldReturn400_WhenInvalidEmailProvided() throws Exception {
        personDTO.setEmail("invalid-email");

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(personDTO)))
                .andExpect(status().isBadRequest());

        verify(clientService, never()).createClient(any());
    }

    @Test
    void createClient_ShouldReturn400_WhenInvalidPhoneProvided() throws Exception {
        personDTO.setPhone("123456");

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(personDTO)))
                .andExpect(status().isBadRequest());

        verify(clientService, never()).createClient(any());
    }

    @Test
    void getClient_ShouldReturn200_WhenClientExists() throws Exception {
        when(clientService.getClient(1L)).thenReturn(personDTO);

        mockMvc.perform(get("/api/clients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(clientService, times(1)).getClient(1L);
    }

    @Test
    void getClient_ShouldReturn404_WhenClientNotFound() throws Exception {
        when(clientService.getClient(999L))
                .thenThrow(new ResourceNotFoundException("Client not found with id: 999"));

        mockMvc.perform(get("/api/clients/999"))
                .andExpect(status().isNotFound());

        verify(clientService, times(1)).getClient(999L);
    }

    @Test
    void updateClient_ShouldReturn200_WhenValidDataProvided() throws Exception {
        ClientUpdateDTO updateDTO = new ClientUpdateDTO();
        updateDTO.setName("Jane Doe");
        updateDTO.setEmail("jane@example.com");
        updateDTO.setPhone("+41791111111");

        PersonDTO updatedPerson = new PersonDTO();
        updatedPerson.setId(1L);
        updatedPerson.setName("Jane Doe");
        updatedPerson.setEmail("jane@example.com");
        updatedPerson.setPhone("+41791111111");
        updatedPerson.setBirthdate(LocalDate.of(1990, 5, 15));

        when(clientService.updateClient(eq(1L), any(ClientUpdateDTO.class)))
                .thenReturn(updatedPerson);

        mockMvc.perform(put("/api/clients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.email").value("jane@example.com"));

        verify(clientService, times(1)).updateClient(eq(1L), any(ClientUpdateDTO.class));
    }

    @Test
    void updateClient_ShouldReturn400_WhenInvalidEmailProvided() throws Exception {
        ClientUpdateDTO updateDTO = new ClientUpdateDTO();
        updateDTO.setEmail("invalid-email");

        mockMvc.perform(put("/api/clients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest());

        verify(clientService, never()).updateClient(any(), any());
    }

    @Test
    void updateClient_ShouldReturn404_WhenClientNotFound() throws Exception {
        ClientUpdateDTO updateDTO = new ClientUpdateDTO();
        updateDTO.setName("New Name");

        when(clientService.updateClient(eq(999L), any(ClientUpdateDTO.class)))
                .thenThrow(new ResourceNotFoundException("Client not found with id: 999"));

        mockMvc.perform(put("/api/clients/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());

        verify(clientService, times(1)).updateClient(eq(999L), any(ClientUpdateDTO.class));
    }

    @Test
    void deleteClient_ShouldReturn204_WhenClientExists() throws Exception {
        doNothing().when(clientService).deleteClient(1L);

        mockMvc.perform(delete("/api/clients/1"))
                .andExpect(status().isNoContent());

        verify(clientService, times(1)).deleteClient(1L);
    }

    @Test
    void deleteClient_ShouldReturn404_WhenClientNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Client not found with id: 999"))
                .when(clientService).deleteClient(999L);

        mockMvc.perform(delete("/api/clients/999"))
                .andExpect(status().isNotFound());

        verify(clientService, times(1)).deleteClient(999L);
    }
}