package com.dataforge.cleaning;

public class DatasetCleaningException extends RuntimeException {

    public DatasetCleaningException(String message) {
        super(message);
    }

    public DatasetCleaningException(String message, Throwable cause) {
        super(message, cause);
    }
}
