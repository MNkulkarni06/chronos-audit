package com.chronos.chronos_audit.repository;

import com.chronos.chronos_audit.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, String> {

    // Custom finder lookup targeted for Phase 2 background audits
    List<Subscription> findByLastInteractionTimestampBefore(LocalDateTime thresholdDate);
}