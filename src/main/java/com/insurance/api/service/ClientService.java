package com.insurance.api.service;

import com.insurance.api.dto.*;
import com.insurance.api.entity.Client;
import com.insurance.api.entity.Company;
import com.insurance.api.entity.Contract;
import com.insurance.api.entity.Person;
import com.insurance.api.exception.ResourceNotFoundException;
import com.insurance.api.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    @Transactional
    public ClientDTO createClient(ClientDTO dto) {
        Client client;

        if (dto instanceof PersonDTO personDTO) {
            Person person = new Person();
            person.setName(personDTO.getName());
            person.setEmail(personDTO.getEmail());
            person.setPhone(personDTO.getPhone());
            person.setBirthdate(personDTO.getBirthdate());
            client = person;
        } else if (dto instanceof CompanyDTO companyDTO) {
            Company company = new Company();
            company.setName(companyDTO.getName());
            company.setEmail(companyDTO.getEmail());
            company.setPhone(companyDTO.getPhone());
            company.setCompanyIdentifier(companyDTO.getCompanyIdentifier());
            client = company;
        } else {
            throw new IllegalArgumentException("Invalid client type");
        }

        client = clientRepository.save(client);
        return mapToDTO(client);
    }

    @Transactional(readOnly = true)
    public ClientDTO getClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
        return mapToDTO(client);
    }

    @Transactional
    public ClientDTO updateClient(Long id, ClientUpdateDTO updateDTO) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));

        if (updateDTO.getName() != null) {
            client.setName(updateDTO.getName());
        }
        if (updateDTO.getEmail() != null) {
            client.setEmail(updateDTO.getEmail());
        }
        if (updateDTO.getPhone() != null) {
            client.setPhone(updateDTO.getPhone());
        }

        client = clientRepository.save(client);
        return mapToDTO(client);
    }

    @Transactional
    public void deleteClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));

        LocalDate today = LocalDate.now();
        for (Contract contract : client.getContracts()) {
            if (contract.getEndDate() == null || contract.getEndDate().isAfter(today)) {
                contract.setEndDate(today);
            }
        }

        clientRepository.delete(client);
    }

    private ClientDTO mapToDTO(Client client) {
        if (client instanceof Person person) {
            PersonDTO dto = new PersonDTO();
            dto.setId(person.getId());
            dto.setName(person.getName());
            dto.setEmail(person.getEmail());
            dto.setPhone(person.getPhone());
            dto.setBirthdate(person.getBirthdate());
            return dto;
        } else if (client instanceof Company company) {
            CompanyDTO dto = new CompanyDTO();
            dto.setId(company.getId());
            dto.setName(company.getName());
            dto.setEmail(company.getEmail());
            dto.setPhone(company.getPhone());
            dto.setCompanyIdentifier(company.getCompanyIdentifier());
            return dto;
        }
        throw new IllegalArgumentException("Unknown client type");
    }
}