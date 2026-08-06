package com.chronos.chronos_audit.repository;

import com.chronos.chronos_audit.dto.LeakingSubscriptionDTO;
import com.chronos.chronos_audit.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByLastInteractionTimestampBefore(LocalDateTime threshold);

    Optional<Subscription> findByProviderNameIgnoreCase(String providerName);

    List<Subscription> findByLastInteractionTimestampBeforeAndStatusNot(LocalDateTime threshold, String status);

    @Query("SELECT new com.chronos.chronos_audit.dto.LeakingSubscriptionDTO(" +
            "u.email, u.fullName, s.providerName, s.monthlyAmount, s.lastInteractionTimestamp) " +
            "FROM Subscription s " +
            "JOIN s.user u " +
            "WHERE s.lastInteractionTimestamp < :threshold AND s.status = 'CRITICAL_LEAK'")
    List<LeakingSubscriptionDTO> findLeakingSubscriptionsWithUserDetail(@Param("threshold") LocalDateTime threshold);
}