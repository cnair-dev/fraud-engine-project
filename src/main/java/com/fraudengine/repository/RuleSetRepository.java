package com.fraudengine.repository;

import com.fraudengine.model.entity.RuleSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface RuleSetRepository extends JpaRepository<RuleSet, UUID> {
    Optional<RuleSet> findTopByAccountTypeOrderByEffectiveFromDesc(String accountType);
}
