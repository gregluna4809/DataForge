package com.dataforge.datasets;

import com.dataforge.users.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "datasets")
public class Dataset {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(length = 1000)
    private String description;

    @Column(name = "upload_timestamp", nullable = false, updatable = false)
    private Instant uploadTimestamp;

    @Column(name = "row_count", nullable = false)
    private long rowCount;

    @Column(name = "column_count", nullable = false)
    private int columnCount;

    @Column(name = "file_size_bytes", nullable = false)
    private long fileSizeBytes;

    @Column(name = "stored_filename", length = 255)
    private String storedFilename;

    @Column(name = "storage_path", length = 1000)
    private String storagePath;

    @Column(name = "uploaded_file_content_type", length = 100)
    private String uploadedFileContentType;

    @Column(name = "file_uploaded_at")
    private Instant fileUploadedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DatasetStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by_user_id", nullable = false)
    private User uploadedBy;

    protected Dataset() {
    }

    public Dataset(
            String name,
            String originalFilename,
            String description,
            long rowCount,
            int columnCount,
            long fileSizeBytes,
            DatasetStatus status,
            User uploadedBy,
            Instant uploadTimestamp
    ) {
        this.name = name;
        this.originalFilename = originalFilename;
        this.description = description;
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.fileSizeBytes = fileSizeBytes;
        this.status = status;
        this.uploadedBy = uploadedBy;
        this.uploadTimestamp = uploadTimestamp;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getDescription() {
        return description;
    }

    public Instant getUploadTimestamp() {
        return uploadTimestamp;
    }

    public long getRowCount() {
        return rowCount;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public DatasetStatus getStatus() {
        return status;
    }

    public User getUploadedBy() {
        return uploadedBy;
    }

    public String getStoredFilename() {
        return storedFilename;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public String getUploadedFileContentType() {
        return uploadedFileContentType;
    }

    public Instant getFileUploadedAt() {
        return fileUploadedAt;
    }

    public void markUploaded(
            String originalFilename,
            String storedFilename,
            String storagePath,
            String contentType,
            long fileSizeBytes,
            Instant uploadedAt
    ) {
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.storagePath = storagePath;
        this.uploadedFileContentType = contentType;
        this.fileSizeBytes = fileSizeBytes;
        this.fileUploadedAt = uploadedAt;
        this.status = DatasetStatus.UPLOADED;
    }
}
