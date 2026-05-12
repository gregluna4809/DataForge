package com.dataforge.quality;

import com.dataforge.datasets.AuthenticatedUserNotFoundException;
import com.dataforge.datasets.Dataset;
import com.dataforge.datasets.DatasetNotFoundException;
import com.dataforge.datasets.DatasetRepository;
import com.dataforge.datasets.dto.DatasetResponse;
import com.dataforge.profiling.DatasetColumnProfile;
import com.dataforge.profiling.DatasetProfileService;
import com.dataforge.profiling.DatasetProfileStorageService;
import com.dataforge.quality.dto.ColumnQualityResponse;
import com.dataforge.quality.dto.DatasetQualityResponse;
import com.dataforge.users.User;
import com.dataforge.users.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatasetQualityService {

    private final DatasetRepository datasetRepository;
    private final DatasetProfileService datasetProfileService;
    private final DatasetProfileStorageService datasetProfileStorageService;
    private final DatasetQualityScorer datasetQualityScorer;
    private final DatasetQualityStorageService datasetQualityStorageService;
    private final UserRepository userRepository;

    public DatasetQualityService(
            DatasetRepository datasetRepository,
            DatasetProfileService datasetProfileService,
            DatasetProfileStorageService datasetProfileStorageService,
            DatasetQualityScorer datasetQualityScorer,
            DatasetQualityStorageService datasetQualityStorageService,
            UserRepository userRepository
    ) {
        this.datasetRepository = datasetRepository;
        this.datasetProfileService = datasetProfileService;
        this.datasetProfileStorageService = datasetProfileStorageService;
        this.datasetQualityScorer = datasetQualityScorer;
        this.datasetQualityStorageService = datasetQualityStorageService;
        this.userRepository = userRepository;
    }

    @Transactional
    public void scoreAndStore(Dataset dataset) {
        List<DatasetColumnProfile> profiles = datasetProfileStorageService.profiles(dataset);
        scoreAndStore(dataset, profiles);
    }

    @Transactional
    public void scoreAndStore(Dataset dataset, List<DatasetColumnProfile> profiles) {
        DatasetQualityResult result = datasetQualityScorer.score(profiles);
        datasetQualityStorageService.replaceQuality(dataset, result);
    }

    @Transactional
    public DatasetQualityResponse getQuality(String email, UUID datasetId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticatedUserNotFoundException(email));
        Dataset dataset = datasetRepository.findByIdAndUploadedBy(datasetId, user)
                .orElseThrow(() -> new DatasetNotFoundException(datasetId));

        DatasetQualityScore score = datasetQualityStorageService.qualityScore(dataset)
                .orElseGet(() -> buildAndStoreQuality(dataset));

        return toResponse(dataset, score);
    }

    private DatasetQualityScore buildAndStoreQuality(Dataset dataset) {
        List<DatasetColumnProfile> profiles = datasetProfileStorageService.profiles(dataset);
        if (profiles.isEmpty()) {
            datasetProfileService.profileAndStore(dataset);
            profiles = datasetProfileStorageService.profiles(dataset);
        }

        DatasetQualityResult result = datasetQualityScorer.score(profiles);
        return datasetQualityStorageService.replaceQuality(dataset, result);
    }

    private DatasetQualityResponse toResponse(Dataset dataset, DatasetQualityScore score) {
        return new DatasetQualityResponse(
                DatasetResponse.from(dataset),
                score.getOverallScore(),
                datasetQualityStorageService.readIssues(score.getIssueSummariesJson()),
                score.getScoredAt(),
                datasetQualityStorageService.orderedColumnScores(score)
                        .stream()
                        .map(this::toColumnResponse)
                        .toList()
        );
    }

    private ColumnQualityResponse toColumnResponse(DatasetColumnQualityScore score) {
        return new ColumnQualityResponse(
                score.getColumnName(),
                score.getColumnPosition(),
                score.getQualityScore(),
                score.getNullPercentage(),
                score.getUniquenessPercentage(),
                score.getEmptyPercentage(),
                score.getTypeConsistencyScore(),
                datasetQualityStorageService.readIssues(score.getIssueSummariesJson())
        );
    }
}
