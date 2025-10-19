package com.insurance.api.controller;

import com.insurance.api.dto.ContractCostUpdateDTO;
import com.insurance.api.dto.ContractDTO;
import com.insurance.api.exception.ResourceNotFoundException;
import com.insurance.api.service.ContractService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContractController.class)
class ContractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ContractService contractService;

    private ContractDTO contractDTO;

    @BeforeEach
    void setUp() {
        contractDTO = new ContractDTO();
        contractDTO.setId(1L);
        contractDTO.setClientId(1L);
        contractDTO.setCostAmount(BigDecimal.valueOf(1500.50));
        contractDTO.setStartDate(LocalDate.of(2025, 1, 1));
        contractDTO.setEndDate(LocalDate.of(2026, 1, 1));
    }

    @Test
    void createContract_ShouldReturn201_WhenValidDataProvided() throws Exception {
        when(contractService.createContract(any(ContractDTO.class))).thenReturn(contractDTO);

        mockMvc.perform(post("/api/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contractDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.clientId").value(1))
                .andExpect(jsonPath("$.costAmount").value(1500.50));

        verify(contractService, times(1)).createContract(any(ContractDTO.class));
    }

    @Test
    void createContract_ShouldReturn400_WhenNegativeCostAmount() throws Exception {
        contractDTO.setCostAmount(BigDecimal.valueOf(-100));

        mockMvc.perform(post("/api/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contractDTO)))
                .andExpect(status().isBadRequest());

        verify(contractService, never()).createContract(any());
    }

    @Test
    void createContract_ShouldReturn404_WhenClientNotFound() throws Exception {
        when(contractService.createContract(any(ContractDTO.class)))
                .thenThrow(new ResourceNotFoundException("Client not found with id: 999"));

        contractDTO.setClientId(999L);

        mockMvc.perform(post("/api/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contractDTO)))
                .andExpect(status().isNotFound());

        verify(contractService, times(1)).createContract(any(ContractDTO.class));
    }

    @Test
    void updateContractCost_ShouldReturn200_WhenValidDataProvided() throws Exception {
        ContractCostUpdateDTO updateDTO = new ContractCostUpdateDTO();
        updateDTO.setCostAmount(BigDecimal.valueOf(2000.00));

        ContractDTO updatedContract = new ContractDTO();
        updatedContract.setId(1L);
        updatedContract.setClientId(1L);
        updatedContract.setCostAmount(BigDecimal.valueOf(2000.00));
        updatedContract.setStartDate(LocalDate.of(2025, 1, 1));
        updatedContract.setEndDate(LocalDate.of(2026, 1, 1));

        when(contractService.updateContractCost(eq(1L), any(ContractCostUpdateDTO.class)))
                .thenReturn(updatedContract);

        mockMvc.perform(patch("/api/contracts/1/cost")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.costAmount").value(2000.00));

        verify(contractService, times(1)).updateContractCost(eq(1L), any(ContractCostUpdateDTO.class));
    }

    @Test
    void updateContractCost_ShouldReturn400_WhenNegativeCostAmount() throws Exception {
        ContractCostUpdateDTO updateDTO = new ContractCostUpdateDTO();
        updateDTO.setCostAmount(BigDecimal.valueOf(-500));

        mockMvc.perform(patch("/api/contracts/1/cost")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest());

        verify(contractService, never()).updateContractCost(any(), any());
    }

    @Test
    void updateContractCost_ShouldReturn404_WhenContractNotFound() throws Exception {
        ContractCostUpdateDTO updateDTO = new ContractCostUpdateDTO();
        updateDTO.setCostAmount(BigDecimal.valueOf(2000.00));

        when(contractService.updateContractCost(eq(999L), any(ContractCostUpdateDTO.class)))
                .thenThrow(new ResourceNotFoundException("Contract not found with id: 999"));

        mockMvc.perform(patch("/api/contracts/999/cost")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());

        verify(contractService, times(1)).updateContractCost(eq(999L), any(ContractCostUpdateDTO.class));
    }

    @Test
    void getActiveContractsForClient_ShouldReturn200_WhenContractsExist() throws Exception {
        ContractDTO contract2 = new ContractDTO();
        contract2.setId(2L);
        contract2.setClientId(1L);
        contract2.setCostAmount(BigDecimal.valueOf(2500.00));
        contract2.setStartDate(LocalDate.of(2025, 6, 1));

        List<ContractDTO> contracts = Arrays.asList(contractDTO, contract2);

        when(contractService.getActiveContractsForClient(eq(1L), any()))
                .thenReturn(contracts);

        mockMvc.perform(get("/api/contracts/client/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(contractService, times(1)).getActiveContractsForClient(eq(1L), any());
    }

    @Test
    void getActiveContractsForClient_ShouldReturn200_WithFilter_WhenUpdatedAfterProvided() throws Exception {
        List<ContractDTO> contracts = Arrays.asList(contractDTO);
        LocalDate filterDate = LocalDate.of(2025, 10, 1);

        when(contractService.getActiveContractsForClient(eq(1L), eq(filterDate)))
                .thenReturn(contracts);

        mockMvc.perform(get("/api/contracts/client/1")
                        .param("updatedAfter", "2025-10-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(contractService, times(1)).getActiveContractsForClient(eq(1L), eq(filterDate));
    }

    @Test
    void getActiveContractsForClient_ShouldReturn404_WhenClientNotFound() throws Exception {
        when(contractService.getActiveContractsForClient(eq(999L), any()))
                .thenThrow(new ResourceNotFoundException("Client not found with id: 999"));

        mockMvc.perform(get("/api/contracts/client/999"))
                .andExpect(status().isNotFound());

        verify(contractService, times(1)).getActiveContractsForClient(eq(999L), any());
    }

    @Test
    void getSumOfActiveContracts_ShouldReturn200_WhenContractsExist() throws Exception {
        BigDecimal sum = BigDecimal.valueOf(4000.50);

        when(contractService.getSumOfActiveContracts(1L)).thenReturn(sum);

        mockMvc.perform(get("/api/contracts/client/1/sum"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCost").value(4000.50));

        verify(contractService, times(1)).getSumOfActiveContracts(1L);
    }

    @Test
    void getSumOfActiveContracts_ShouldReturn200WithZero_WhenNoActiveContracts() throws Exception {
        when(contractService.getSumOfActiveContracts(1L)).thenReturn(BigDecimal.ZERO);

        mockMvc.perform(get("/api/contracts/client/1/sum"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCost").value(0));

        verify(contractService, times(1)).getSumOfActiveContracts(1L);
    }

    @Test
    void getSumOfActiveContracts_ShouldReturn404_WhenClientNotFound() throws Exception {
        when(contractService.getSumOfActiveContracts(999L))
                .thenThrow(new ResourceNotFoundException("Client not found with id: 999"));

        mockMvc.perform(get("/api/contracts/client/999/sum"))
                .andExpect(status().isNotFound());

        verify(contractService, times(1)).getSumOfActiveContracts(999L);
    }
}