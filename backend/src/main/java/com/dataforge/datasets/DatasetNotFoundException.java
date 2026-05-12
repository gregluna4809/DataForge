package com.dataforge.datasets;

import java.util.UUID;

public class DatasetNotFoundException extends RuntimeException {

    public DatasetNotFoundException(UUID datasetId) {
        super("Dataset was not found: " + datasetId);
    }
}
