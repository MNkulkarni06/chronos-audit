package com.chronos.chronos_audit.repository;

import com.chronos.chronos_audit.dto.LeakingSubscriptionDTO;
import com.chronos.chronos_audit.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByProviderNameIgnoreCase(String providerName);

    List<Subscription> findByLastInteractionTimestampBeforeAndStatusNot(LocalDateTime timestamp, String status);

    @Query("SELECT new com.chronos.chronos_audit.dto.LeakingSubscriptionDTO(" +
            "s.user.email, s.user.fullName, s.providerName, s.monthlyAmount, s.lastInteractionTimestamp) " +
            "FROM Subscription s " +
            "WHERE s.status = 'CRITICAL_LEAK'")
    List<LeakingSubscriptionDTO> findAllCriticalLeaksSummary();

    @Query("SELECT s FROM Subscription s JOIN FETCH s.user WHERE s.status = 'ACTIVE' AND s.nextBillingDate BETWEEN :startDate AND :endDate")
    List<Subscription> findUpcomingRenewals(
            @org.springframework.data.repository.query.Param("startDate") LocalDateTime startDate,
            @org.springframework.data.repository.query.Param("endDate") LocalDateTime endDate
    );
}