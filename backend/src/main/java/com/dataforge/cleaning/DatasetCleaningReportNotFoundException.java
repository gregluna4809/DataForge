package com.dataforge.cleaning;

import java.util.UUID;

public class DatasetCleaningReportNotFoundException extends RuntimeException {

    public DatasetCleaningReportNotFoundException(UUID datasetId) {
        super("Cleaning report was not found for dataset: " + datasetId);
    }
}
