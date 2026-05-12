package com.dataforge.ai;

import com.dataforge.ai.dto.DatasetAiInsightResponse;
import java.security.Principal;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/datasets")
public class DatasetAiInsightController {

    private final DatasetAiInsightService datasetAiInsightService;

    public DatasetAiInsightController(DatasetAiInsightService datasetAiInsightService) {
        this.datasetAiInsightService = datasetAiInsightService;
    }

    @GetMapping("/{datasetId}/insights")
    public ResponseEntity<DatasetAiInsightResponse> getDatasetInsights(
            Principal principal,
            @PathVariable UUID datasetId
    ) {
        return ResponseEntity.ok(datasetAiInsightService.getInsights(principal.getName(), datasetId));
    }
}
