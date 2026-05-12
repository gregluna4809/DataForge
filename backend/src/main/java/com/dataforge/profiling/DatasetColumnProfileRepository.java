package com.dataforge.profiling;

import com.dataforge.datasets.Dataset;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasetColumnProfileRepository extends JpaRepository<DatasetColumnProfile, UUID> {

    void deleteByDataset(Dataset dataset);

    List<DatasetColumnProfile> findByDatasetOrderByColumnPositionAsc(Dataset dataset);
}
