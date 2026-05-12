package com.dataforge.datasets;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasetPreviewRowRepository extends JpaRepository<DatasetPreviewRow, UUID> {

    void deleteByDataset(Dataset dataset);

    List<DatasetPreviewRow> findByDatasetOrderByPositionAsc(Dataset dataset);
}
