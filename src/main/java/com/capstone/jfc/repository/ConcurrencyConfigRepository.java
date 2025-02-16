package com.capstone.jfc.repository;

import com.capstone.jfc.model.ConcurrencyConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConcurrencyConfigRepository extends JpaRepository<ConcurrencyConfigEntity, Long> {

    List<ConcurrencyConfigEntity> findByConcurrencyType(String concurrencyType);

    // Optionally, you could define more queries, e.g.:
    // findByEventTypeAndTool(...) if you'd like direct lookups.
}
