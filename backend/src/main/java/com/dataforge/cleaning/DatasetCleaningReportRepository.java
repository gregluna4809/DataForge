package com.dataforge.cleaning;

import com.dataforge.datasets.Dataset;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasetCleaningReportRepository extends JpaRepository<DatasetCleaningReport, UUID> {

    Optional<DatasetCleaningReport> findByDataset(Dataset dataset);

    void deleteByDataset(Dataset dataset);
}
