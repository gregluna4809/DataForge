package com.dataforge.quality;

import com.dataforge.quality.dto.DatasetQualityResponse;
import java.security.Principal;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/datasets")
public class DatasetQualityController {

    private final DatasetQualityService datasetQualityService;

    public DatasetQualityController(DatasetQualityService datasetQualityService) {
        this.datasetQualityService = datasetQualityService;
    }

    @GetMapping("/{datasetId}/quality")
    public ResponseEntity<DatasetQualityResponse> getDatasetQuality(
            Principal principal,
            @PathVariable UUID datasetId
    ) {
        return ResponseEntity.ok(datasetQualityService.getQuality(principal.getName(), datasetId));
    }
}
