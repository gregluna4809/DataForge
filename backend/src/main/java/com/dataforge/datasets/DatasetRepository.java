package com.dataforge.datasets;

import com.dataforge.users.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasetRepository extends JpaRepository<Dataset, UUID> {

    List<Dataset> findByUploadedByOrderByUploadTimestampDesc(User uploadedBy);
}
