package com.dataforge.ai;

import com.dataforge.datasets.Dataset;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasetAiInsightRepository extends JpaRepository<DatasetAiInsight, UUID> {

    void deleteByDataset(Dataset dataset);

    Optional<DatasetAiInsight> findByDataset(Dataset dataset);
}
