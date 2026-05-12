package com.dataforge.quality;

import com.dataforge.datasets.Dataset;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasetQualityScoreRepository extends JpaRepository<DatasetQualityScore, UUID> {

    void deleteByDataset(Dataset dataset);

    Optional<DatasetQualityScore> findByDataset(Dataset dataset);
}
