package com.dataforge.datasets;

import com.dataforge.datasets.dto.CreateDatasetRequest;
import com.dataforge.datasets.dto.DatasetResponse;
import com.dataforge.datasets.dto.DatasetUploadResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/datasets")
public class DatasetController {

    private final DatasetService datasetService;
    private final DatasetUploadService datasetUploadService;

    public DatasetController(DatasetService datasetService, DatasetUploadService datasetUploadService) {
        this.datasetService = datasetService;
        this.datasetUploadService = datasetUploadService;
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

    @PostMapping(path = "/{datasetId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DatasetUploadResponse> uploadDatasetCsv(
            Principal principal,
            @PathVariable UUID datasetId,
            @RequestParam("file") MultipartFile file
    ) {
        DatasetUploadResponse response = datasetUploadService.uploadCsv(principal.getName(), datasetId, file);
        return ResponseEntity.ok(response);
    }
}
