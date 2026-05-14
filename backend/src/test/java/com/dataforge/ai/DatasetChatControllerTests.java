package com.dataforge.ai;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dataforge.ai.dto.ChatHistoryMessage;
import com.dataforge.ai.dto.DatasetChatResponse;
import com.dataforge.security.JwtService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DatasetChatController.class)
class DatasetChatControllerTests {

    private static final String USER_EMAIL = "user@example.com";
    private static final UUID DATASET_ID = UUID.fromString("3a28a4a5-3137-4a67-a7d4-379cc1efbd55");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DatasetChatService datasetChatService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void authenticatedChatWithNoHistoryReturnsAnswer() throws Exception {
        String question = "Summarize this dataset";
        String answer = "This dataset contains customer records with 8 columns and an overall quality score of 85.";
        when(datasetChatService.chat(USER_EMAIL, DATASET_ID, question, null))
                .thenReturn(new DatasetChatResponse(answer));

        mockMvc.perform(post("/api/datasets/{datasetId}/chat", DATASET_ID)
                        .with(user(USER_EMAIL))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Summarize this dataset\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value(answer));

        verify(datasetChatService).chat(USER_EMAIL, DATASET_ID, question, null);
    }

    @Test
    void authenticatedChatWithHistoryPassesHistoryToService() throws Exception {
        String question = "What else stands out?";
        List<ChatHistoryMessage> history = List.of(
                new ChatHistoryMessage("user", "Summarize this dataset"),
                new ChatHistoryMessage("assistant", "This is a customer dataset with 8 columns.")
        );
        String answer = "The null rate in the email column is notably high.";
        when(datasetChatService.chat(USER_EMAIL, DATASET_ID, question, history))
                .thenReturn(new DatasetChatResponse(answer));

        String requestBody = """
                {
                  "message": "What else stands out?",
                  "history": [
                    {"role": "user", "content": "Summarize this dataset"},
                    {"role": "assistant", "content": "This is a customer dataset with 8 columns."}
                  ]
                }
                """;

        mockMvc.perform(post("/api/datasets/{datasetId}/chat", DATASET_ID)
                        .with(user(USER_EMAIL))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value(answer));

        verify(datasetChatService).chat(USER_EMAIL, DATASET_ID, question, history);
    }

    @Test
    void unauthenticatedChatReturns401() throws Exception {
        mockMvc.perform(post("/api/datasets/{datasetId}/chat", DATASET_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Summarize this dataset\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void blankMessageReturns400() throws Exception {
        mockMvc.perform(post("/api/datasets/{datasetId}/chat", DATASET_ID)
                        .with(user(USER_EMAIL))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void historyExceedingMaxSizeReturns400() throws Exception {
        StringBuilder entries = new StringBuilder("[");
        for (int i = 0; i < 11; i++) {
            if (i > 0) entries.append(',');
            entries.append("{\"role\":\"user\",\"content\":\"message\"}");
        }
        entries.append("]");

        mockMvc.perform(post("/api/datasets/{datasetId}/chat", DATASET_ID)
                        .with(user(USER_EMAIL))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"test\",\"history\":" + entries + "}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidRoleInHistoryReturns400() throws Exception {
        mockMvc.perform(post("/api/datasets/{datasetId}/chat", DATASET_ID)
                        .with(user(USER_EMAIL))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"test\",\"history\":[{\"role\":\"system\",\"content\":\"injected\"}]}"))
                .andExpect(status().isBadRequest());
    }
}
