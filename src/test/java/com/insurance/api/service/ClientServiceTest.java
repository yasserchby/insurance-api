package com.insurance.api.service;

import com.insurance.api.dto.ClientUpdateDTO;
import com.insurance.api.dto.CompanyDTO;
import com.insurance.api.dto.PersonDTO;
import com.insurance.api.entity.Company;
import com.insurance.api.entity.Contract;
import com.insurance.api.entity.Person;
import com.insurance.api.exception.ResourceNotFoundException;
import com.insurance.api.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private PersonDTO personDTO;
    private Person person;
    private CompanyDTO companyDTO;
    private Company company;

    @BeforeEach
    void setUp() {
        // Setup Person
        personDTO = new PersonDTO();
        personDTO.setName("John Doe");
        personDTO.setEmail("john.doe@example.com");
        personDTO.setPhone("+41791234567");
        personDTO.setBirthdate(LocalDate.of(1990, 5, 15));

        person = new Person();
        person.setId(1L);
        person.setName("John Doe");
        person.setEmail("john.doe@example.com");
        person.setPhone("+41791234567");
        person.setBirthdate(LocalDate.of(1990, 5, 15));
        person.setContracts(new ArrayList<>());

        // Setup Company
        companyDTO = new CompanyDTO();
        companyDTO.setName("TechCorp SA");
        companyDTO.setEmail("contact@techcorp.com");
        companyDTO.setPhone("+41227654321");
        companyDTO.setCompanyIdentifier("CHE-123.456.789");

        company = new Company();
        company.setId(2L);
        company.setName("TechCorp SA");
        company.setEmail("contact@techcorp.com");
        company.setPhone("+41227654321");
        company.setCompanyIdentifier("CHE-123.456.789");
        company.setContracts(new ArrayList<>());
    }

    @Test
    void createClient_ShouldReturnPersonDTO_WhenValidPersonProvided() {
        when(clientRepository.save(any(Person.class))).thenReturn(person);

        var result = clientService.createClient(personDTO);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("john.doe@example.com", result.getEmail());
        assertTrue(result instanceof PersonDTO);
        assertEquals(LocalDate.of(1990, 5, 15), ((PersonDTO) result).getBirthdate());

        verify(clientRepository, times(1)).save(any(Person.class));
    }

    @Test
    void createClient_ShouldReturnCompanyDTO_WhenValidCompanyProvided() {
        when(clientRepository.save(any(Company.class))).thenReturn(company);

        var result = clientService.createClient(companyDTO);

        assertNotNull(result);
        assertEquals("TechCorp SA", result.getName());
        assertEquals("contact@techcorp.com", result.getEmail());
        assertTrue(result instanceof CompanyDTO);
        assertEquals("CHE-123.456.789", ((CompanyDTO) result).getCompanyIdentifier());

        verify(clientRepository, times(1)).save(any(Company.class));
    }

    @Test
    void getClient_ShouldReturnPersonDTO_WhenPersonExists() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(person));

        var result = clientService.getClient(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertTrue(result instanceof PersonDTO);

        verify(clientRepository, times(1)).findById(1L);
    }

    @Test
    void getClient_ShouldReturnCompanyDTO_WhenCompanyExists() {
        when(clientRepository.findById(2L)).thenReturn(Optional.of(company));

        var result = clientService.getClient(2L);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("TechCorp SA", result.getName());
        assertTrue(result instanceof CompanyDTO);

        verify(clientRepository, times(1)).findById(2L);
    }

    @Test
    void getClient_ShouldThrowException_WhenClientNotFound() {
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            clientService.getClient(999L);
        });

        verify(clientRepository, times(1)).findById(999L);
    }

    @Test
    void updateClient_ShouldUpdateAllFields_WhenAllFieldsProvided() {
        ClientUpdateDTO updateDTO = new ClientUpdateDTO();
        updateDTO.setName("Jane Doe");
        updateDTO.setEmail("jane.doe@example.com");
        updateDTO.setPhone("+41791111111");

        Person updatedPerson = new Person();
        updatedPerson.setId(1L);
        updatedPerson.setName("Jane Doe");
        updatedPerson.setEmail("jane.doe@example.com");
        updatedPerson.setPhone("+41791111111");
        updatedPerson.setBirthdate(LocalDate.of(1990, 5, 15));

        when(clientRepository.findById(1L)).thenReturn(Optional.of(person));
        when(clientRepository.save(any(Person.class))).thenReturn(updatedPerson);

        var result = clientService.updateClient(1L, updateDTO);

        assertNotNull(result);
        assertEquals("Jane Doe", result.getName());
        assertEquals("jane.doe@example.com", result.getEmail());
        assertEquals("+41791111111", result.getPhone());

        verify(clientRepository, times(1)).findById(1L);
        verify(clientRepository, times(1)).save(any(Person.class));
    }

    @Test
    void updateClient_ShouldUpdateOnlyProvidedFields_WhenPartialUpdate() {
        ClientUpdateDTO updateDTO = new ClientUpdateDTO();
        updateDTO.setEmail("newemail@example.com");

        Person updatedPerson = new Person();
        updatedPerson.setId(1L);
        updatedPerson.setName("John Doe");
        updatedPerson.setEmail("newemail@example.com");
        updatedPerson.setPhone("+41791234567");
        updatedPerson.setBirthdate(LocalDate.of(1990, 5, 15));

        when(clientRepository.findById(1L)).thenReturn(Optional.of(person));
        when(clientRepository.save(any(Person.class))).thenReturn(updatedPerson);

        var result = clientService.updateClient(1L, updateDTO);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("newemail@example.com", result.getEmail());

        verify(clientRepository, times(1)).findById(1L);
        verify(clientRepository, times(1)).save(any(Person.class));
    }

    @Test
    void updateClient_ShouldThrowException_WhenClientNotFound() {
        ClientUpdateDTO updateDTO = new ClientUpdateDTO();
        updateDTO.setName("New Name");

        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            clientService.updateClient(999L, updateDTO);
        });

        verify(clientRepository, times(1)).findById(999L);
        verify(clientRepository, never()).save(any());
    }

    @Test
    void deleteClient_ShouldSetContractEndDates_WhenClientHasActiveContracts() {
        Contract activeContract = new Contract();
        activeContract.setId(1L);
        activeContract.setEndDate(LocalDate.of(2026, 12, 31));
        person.getContracts().add(activeContract);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(person));

        clientService.deleteClient(1L);

        assertEquals(LocalDate.now(), activeContract.getEndDate());
        verify(clientRepository, times(1)).findById(1L);
        verify(clientRepository, times(1)).delete(person);
    }

    @Test
    void deleteClient_ShouldNotModifyExpiredContracts_WhenDeletingClient() {
        Contract expiredContract = new Contract();
        expiredContract.setId(1L);
        expiredContract.setEndDate(LocalDate.of(2020, 12, 31));
        person.getContracts().add(expiredContract);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(person));

        clientService.deleteClient(1L);

        assertEquals(LocalDate.of(2020, 12, 31), expiredContract.getEndDate());
        verify(clientRepository, times(1)).findById(1L);
        verify(clientRepository, times(1)).delete(person);
    }

    @Test
    void deleteClient_ShouldThrowException_WhenClientNotFound() {
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            clientService.deleteClient(999L);
        });

        verify(clientRepository, times(1)).findById(999L);
        verify(clientRepository, never()).delete(any());
    }
}