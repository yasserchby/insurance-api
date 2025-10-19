package com.insurance.api.controller;

import com.insurance.api.dto.ContractCostUpdateDTO;
import com.insurance.api.dto.ContractDTO;
import com.insurance.api.service.ContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @PostMapping
    public ResponseEntity<ContractDTO> createContract(@Valid @RequestBody ContractDTO contractDTO) {
        ContractDTO created = contractService.createContract(contractDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/cost")
    public ResponseEntity<ContractDTO> updateContractCost(
            @PathVariable Long id,
            @Valid @RequestBody ContractCostUpdateDTO updateDTO) {
        ContractDTO updated = contractService.updateContractCost(id, updateDTO);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ContractDTO>> getActiveContractsForClient(
            @PathVariable Long clientId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate updatedAfter) {
        List<ContractDTO> contracts = contractService.getActiveContractsForClient(clientId, updatedAfter);
        return ResponseEntity.ok(contracts);
    }

    @GetMapping("/client/{clientId}/sum")
    public ResponseEntity<Map<String, BigDecimal>> getSumOfActiveContracts(@PathVariable Long clientId) {
        BigDecimal sum = contractService.getSumOfActiveContracts(clientId);
        return ResponseEntity.ok(Map.of("totalCost", sum));
    }
}