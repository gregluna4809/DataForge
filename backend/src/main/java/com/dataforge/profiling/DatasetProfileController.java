package com.dataforge.profiling;

import com.dataforge.profiling.dto.DatasetProfileResponse;
import java.security.Principal;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/datasets")
public class DatasetProfileController {

    private final DatasetProfileService datasetProfileService;

    public DatasetProfileController(DatasetProfileService datasetProfileService) {
        this.datasetProfileService = datasetProfileService;
    }

    @GetMapping("/{datasetId}/profile")
    public ResponseEntity<DatasetProfileResponse> getDatasetProfile(
            Principal principal,
            @PathVariable UUID datasetId
    ) {
        return ResponseEntity.ok(datasetProfileService.getProfile(principal.getName(), datasetId));
    }
}
