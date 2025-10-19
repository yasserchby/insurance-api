package com.insurance.api.service;

import com.insurance.api.dto.ContractCostUpdateDTO;
import com.insurance.api.dto.ContractDTO;
import com.insurance.api.entity.Client;
import com.insurance.api.entity.Contract;
import com.insurance.api.entity.Person;
import com.insurance.api.exception.ResourceNotFoundException;
import com.insurance.api.repository.ClientRepository;
import com.insurance.api.repository.ContractRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ContractService contractService;

    private Client client;
    private Contract contract;
    private ContractDTO contractDTO;

    @BeforeEach
    void setUp() {
        client = new Person();
        client.setId(1L);
        client.setName("John Doe");
        client.setEmail("john@example.com");
        client.setPhone("+41791234567");

        contract = new Contract();
        contract.setId(1L);
        contract.setClient(client);
        contract.setCostAmount(BigDecimal.valueOf(1500.50));
        contract.setStartDate(LocalDate.of(2025, 1, 1));
        contract.setEndDate(LocalDate.of(2026, 1, 1));
        contract.setUpdateDate(LocalDateTime.now());

        contractDTO = new ContractDTO();
        contractDTO.setClientId(1L);
        contractDTO.setCostAmount(BigDecimal.valueOf(1500.50));
        contractDTO.setStartDate(LocalDate.of(2025, 1, 1));
        contractDTO.setEndDate(LocalDate.of(2026, 1, 1));
    }

    @Test
    void createContract_ShouldReturnContractDTO_WhenValidDataProvided() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(contractRepository.save(any(Contract.class))).thenReturn(contract);

        var result = contractService.createContract(contractDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getClientId());
        assertEquals(BigDecimal.valueOf(1500.50), result.getCostAmount());
        assertEquals(LocalDate.of(2025, 1, 1), result.getStartDate());

        verify(clientRepository, times(1)).findById(1L);
        verify(contractRepository, times(1)).save(any(Contract.class));
    }

    @Test
    void createContract_ShouldDefaultToCurrentDate_WhenStartDateNotProvided() {
        contractDTO.setStartDate(null);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> {
            Contract savedContract = invocation.getArgument(0);
            savedContract.setId(1L);
            return savedContract;
        });

        var result = contractService.createContract(contractDTO);

        assertNotNull(result);
        assertNotNull(result.getStartDate());

        verify(clientRepository, times(1)).findById(1L);
        verify(contractRepository, times(1)).save(any(Contract.class));
    }

    @Test
    void createContract_ShouldThrowException_WhenClientNotFound() {
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());
        contractDTO.setClientId(999L);

        assertThrows(ResourceNotFoundException.class, () -> {
            contractService.createContract(contractDTO);
        });

        verify(clientRepository, times(1)).findById(999L);
        verify(contractRepository, never()).save(any());
    }

    @Test
    void updateContractCost_ShouldUpdateCostAmount_WhenContractExists() {
        ContractCostUpdateDTO updateDTO = new ContractCostUpdateDTO();
        updateDTO.setCostAmount(BigDecimal.valueOf(2000.00));

        Contract updatedContract = new Contract();
        updatedContract.setId(1L);
        updatedContract.setClient(client);
        updatedContract.setCostAmount(BigDecimal.valueOf(2000.00));
        updatedContract.setStartDate(LocalDate.of(2025, 1, 1));
        updatedContract.setEndDate(LocalDate.of(2026, 1, 1));
        updatedContract.setUpdateDate(LocalDateTime.now());

        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(contractRepository.save(any(Contract.class))).thenReturn(updatedContract);

        var result = contractService.updateContractCost(1L, updateDTO);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(2000.00), result.getCostAmount());

        verify(contractRepository, times(1)).findById(1L);
        verify(contractRepository, times(1)).save(any(Contract.class));
    }

    @Test
    void updateContractCost_ShouldThrowException_WhenContractNotFound() {
        ContractCostUpdateDTO updateDTO = new ContractCostUpdateDTO();
        updateDTO.setCostAmount(BigDecimal.valueOf(2000.00));

        when(contractRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            contractService.updateContractCost(999L, updateDTO);
        });

        verify(contractRepository, times(1)).findById(999L);
        verify(contractRepository, never()).save(any());
    }

    @Test
    void getActiveContractsForClient_ShouldReturnActiveContracts_WhenNoFilterProvided() {
        Contract contract2 = new Contract();
        contract2.setId(2L);
        contract2.setClient(client);
        contract2.setCostAmount(BigDecimal.valueOf(2500.00));
        contract2.setStartDate(LocalDate.of(2025, 6, 1));
        contract2.setEndDate(null);
        contract2.setUpdateDate(LocalDateTime.now());

        List<Contract> contracts = Arrays.asList(contract, contract2);

        when(clientRepository.existsById(1L)).thenReturn(true);
        when(contractRepository.findActiveContractsByClientId(eq(1L), any(LocalDate.class)))
                .thenReturn(contracts);

        var result = contractService.getActiveContractsForClient(1L, null);

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(clientRepository, times(1)).existsById(1L);
        verify(contractRepository, times(1)).findActiveContractsByClientId(eq(1L), any(LocalDate.class));
    }

    @Test
    void getActiveContractsForClient_ShouldReturnFilteredContracts_WhenUpdatedAfterProvided() {
        LocalDate filterDate = LocalDate.of(2025, 10, 1);
        List<Contract> contracts = Arrays.asList(contract);

        when(clientRepository.existsById(1L)).thenReturn(true);
        when(contractRepository.findActiveContractsByClientIdAndUpdatedAfter(
                eq(1L), any(LocalDate.class), any(LocalDateTime.class)))
                .thenReturn(contracts);

        var result = contractService.getActiveContractsForClient(1L, filterDate);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(clientRepository, times(1)).existsById(1L);
        verify(contractRepository, times(1)).findActiveContractsByClientIdAndUpdatedAfter(
                eq(1L), any(LocalDate.class), any(LocalDateTime.class));
    }

    @Test
    void getActiveContractsForClient_ShouldThrowException_WhenClientNotFound() {
        when(clientRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            contractService.getActiveContractsForClient(999L, null);
        });

        verify(clientRepository, times(1)).existsById(999L);
        verify(contractRepository, never()).findActiveContractsByClientId(any(), any());
    }

    @Test
    void getSumOfActiveContracts_ShouldReturnCorrectSum_WhenContractsExist() {
        BigDecimal expectedSum = BigDecimal.valueOf(4000.50);

        when(clientRepository.existsById(1L)).thenReturn(true);
        when(contractRepository.sumActiveContractsCostByClientId(eq(1L), any(LocalDate.class)))
                .thenReturn(expectedSum);

        var result = contractService.getSumOfActiveContracts(1L);

        assertNotNull(result);
        assertEquals(expectedSum, result);

        verify(clientRepository, times(1)).existsById(1L);
        verify(contractRepository, times(1)).sumActiveContractsCostByClientId(eq(1L), any(LocalDate.class));
    }

    @Test
    void getSumOfActiveContracts_ShouldReturnZero_WhenNoActiveContracts() {
        when(clientRepository.existsById(1L)).thenReturn(true);
        when(contractRepository.sumActiveContractsCostByClientId(eq(1L), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);

        var result = contractService.getSumOfActiveContracts(1L);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result);

        verify(clientRepository, times(1)).existsById(1L);
        verify(contractRepository, times(1)).sumActiveContractsCostByClientId(eq(1L), any(LocalDate.class));
    }

    @Test
    void getSumOfActiveContracts_ShouldThrowException_WhenClientNotFound() {
        when(clientRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            contractService.getSumOfActiveContracts(999L);
        });

        verify(clientRepository, times(1)).existsById(999L);
        verify(contractRepository, never()).sumActiveContractsCostByClientId(any(), any());
    }
}