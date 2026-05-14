package com.dataforge.ai;

import com.dataforge.ai.dto.DatasetChatRequest;
import com.dataforge.ai.dto.DatasetChatResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/datasets")
public class DatasetChatController {

    private final DatasetChatService datasetChatService;

    public DatasetChatController(DatasetChatService datasetChatService) {
        this.datasetChatService = datasetChatService;
    }

    @PostMapping("/{datasetId}/chat")
    public ResponseEntity<DatasetChatResponse> chat(
            Principal principal,
            @PathVariable UUID datasetId,
            @Valid @RequestBody DatasetChatRequest request
    ) {
        return ResponseEntity.ok(datasetChatService.chat(principal.getName(), datasetId, request.message()));
    }
}
