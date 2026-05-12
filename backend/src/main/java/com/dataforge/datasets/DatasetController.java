package com.dataforge.datasets;

import com.dataforge.datasets.dto.CreateDatasetRequest;
import com.dataforge.datasets.dto.DatasetResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/datasets")
public class DatasetController {

    private final DatasetService datasetService;

    public DatasetController(DatasetService datasetService) {
        this.datasetService = datasetService;
    }

    @GetMapping
    public ResponseEntity<List<DatasetResponse>> listDatasets(Principal principal) {
        return ResponseEntity.ok(datasetService.listDatasetsForUser(principal.getName()));
    }

    @PostMapping
    public ResponseEntity<DatasetResponse> createDataset(
            Principal principal,
            @Valid @RequestBody CreateDatasetRequest request
    ) {
        DatasetResponse response = datasetService.createDataset(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
