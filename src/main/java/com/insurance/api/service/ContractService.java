package com.insurance.api.service;

import com.insurance.api.dto.ContractCostUpdateDTO;
import com.insurance.api.dto.ContractDTO;
import com.insurance.api.entity.Client;
import com.insurance.api.entity.Contract;
import com.insurance.api.exception.ResourceNotFoundException;
import com.insurance.api.repository.ClientRepository;
import com.insurance.api.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final ClientRepository clientRepository;

    @Transactional
    public ContractDTO createContract(ContractDTO dto) {
        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + dto.getClientId()));

        Contract contract = new Contract();
        contract.setClient(client);
        contract.setCostAmount(dto.getCostAmount());
        contract.setStartDate(dto.getStartDate() != null ? dto.getStartDate() : LocalDate.now());
        contract.setEndDate(dto.getEndDate());

        contract = contractRepository.save(contract);
        return mapToDTO(contract);
    }

    @Transactional
    public ContractDTO updateContractCost(Long id, ContractCostUpdateDTO updateDTO) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + id));

        contract.setCostAmount(updateDTO.getCostAmount());
        contract = contractRepository.save(contract);

        return mapToDTO(contract);
    }

    @Transactional(readOnly = true)
    public List<ContractDTO> getActiveContractsForClient(Long clientId, LocalDate updatedAfter) {
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client not found with id: " + clientId);
        }

        List<Contract> contracts;
        LocalDate today = LocalDate.now();

        if (updatedAfter != null) {
            LocalDateTime updatedAfterDateTime = updatedAfter.atStartOfDay();
            contracts = contractRepository.findActiveContractsByClientIdAndUpdatedAfter(
                    clientId, today, updatedAfterDateTime);
        } else {
            contracts = contractRepository.findActiveContractsByClientId(clientId, today);
        }

        return contracts.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BigDecimal getSumOfActiveContracts(Long clientId) {
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client not found with id: " + clientId);
        }

        LocalDate today = LocalDate.now();
        return contractRepository.sumActiveContractsCostByClientId(clientId, today);
    }

    private ContractDTO mapToDTO(Contract contract) {
        ContractDTO dto = new ContractDTO();
        dto.setId(contract.getId());
        dto.setClientId(contract.getClient().getId());
        dto.setCostAmount(contract.getCostAmount());
        dto.setStartDate(contract.getStartDate());
        dto.setEndDate(contract.getEndDate());
        return dto;
    }
}