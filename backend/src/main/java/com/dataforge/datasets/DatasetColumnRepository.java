package com.dataforge.datasets;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasetColumnRepository extends JpaRepository<DatasetColumn, java.util.UUID> {

    void deleteByDataset(Dataset dataset);

    List<DatasetColumn> findByDatasetOrderByPositionAsc(Dataset dataset);
}
