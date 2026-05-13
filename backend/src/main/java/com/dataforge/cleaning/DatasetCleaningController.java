package com.dataforge.cleaning;

import com.dataforge.cleaning.DatasetCleaningService.CleanedDatasetDownload;
import com.dataforge.cleaning.dto.DatasetCleaningReportResponse;
import java.security.Principal;
import java.util.UUID;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/datasets")
public class DatasetCleaningController {

    private final DatasetCleaningService datasetCleaningService;

    public DatasetCleaningController(DatasetCleaningService datasetCleaningService) {
        this.datasetCleaningService = datasetCleaningService;
    }

    @PostMapping("/{datasetId}/clean")
    public ResponseEntity<DatasetCleaningReportResponse> cleanDataset(
            Principal principal,
            @PathVariable UUID datasetId
    ) {
        return ResponseEntity.ok(datasetCleaningService.cleanDataset(principal.getName(), datasetId));
    }

    @GetMapping("/{datasetId}/cleaning-report")
    public ResponseEntity<DatasetCleaningReportResponse> getCleaningReport(
            Principal principal,
            @PathVariable UUID datasetId
    ) {
        return ResponseEntity.ok(datasetCleaningService.getCleaningReport(principal.getName(), datasetId));
    }

    @GetMapping("/{datasetId}/download-cleaned")
    public ResponseEntity<?> downloadCleaned(
            Principal principal,
            @PathVariable UUID datasetId
    ) {
        CleanedDatasetDownload download = datasetCleaningService.cleanedDatasetDownload(principal.getName(), datasetId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(download.fileSizeBytes())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(download.filename()).build().toString()
                )
                .body(download.resource());
    }
}
