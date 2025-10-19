package com.insurance.api.repository;

import com.insurance.api.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    @Query("SELECT c FROM Contract c WHERE c.client.id = :clientId " +
            "AND (c.endDate IS NULL OR c.endDate > :currentDate)")
    List<Contract> findActiveContractsByClientId(
            @Param("clientId") Long clientId,
            @Param("currentDate") LocalDate currentDate
    );

    @Query("SELECT c FROM Contract c WHERE c.client.id = :clientId " +
            "AND (c.endDate IS NULL OR c.endDate > :currentDate) " +
            "AND c.updateDate >= :updatedAfter")
    List<Contract> findActiveContractsByClientIdAndUpdatedAfter(
            @Param("clientId") Long clientId,
            @Param("currentDate") LocalDate currentDate,
            @Param("updatedAfter") LocalDateTime updatedAfter
    );

    @Query(value = "SELECT COALESCE(SUM(c.cost_amount), 0) FROM contracts c " +
            "WHERE c.client_id = :clientId " +
            "AND (c.end_date IS NULL OR c.end_date > :currentDate)",
            nativeQuery = true)
    BigDecimal sumActiveContractsCostByClientId(
            @Param("clientId") Long clientId,
            @Param("currentDate") LocalDate currentDate
    );
}