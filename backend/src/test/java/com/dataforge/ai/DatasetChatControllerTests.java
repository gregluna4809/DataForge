package com.dataforge.ai;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dataforge.ai.dto.DatasetChatResponse;
import com.dataforge.security.JwtService;
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

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DatasetChatService datasetChatService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void authenticatedChatReturnsAnswer() throws Exception {
        UUID datasetId = UUID.fromString("3a28a4a5-3137-4a67-a7d4-379cc1efbd55");
        String question = "Summarize this dataset";
        String answer = "This dataset contains customer records with 8 columns and an overall quality score of 85.";
        when(datasetChatService.chat(USER_EMAIL, datasetId, question)).thenReturn(new DatasetChatResponse(answer));

        mockMvc.perform(post("/api/datasets/{datasetId}/chat", datasetId)
                        .with(user(USER_EMAIL))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Summarize this dataset\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value(answer));

        verify(datasetChatService).chat(USER_EMAIL, datasetId, question);
    }

    @Test
    void unauthenticatedChatReturns401() throws Exception {
        UUID datasetId = UUID.fromString("3a28a4a5-3137-4a67-a7d4-379cc1efbd55");

        mockMvc.perform(post("/api/datasets/{datasetId}/chat", datasetId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Summarize this dataset\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void blankMessageReturns400() throws Exception {
        UUID datasetId = UUID.fromString("3a28a4a5-3137-4a67-a7d4-379cc1efbd55");

        mockMvc.perform(post("/api/datasets/{datasetId}/chat", datasetId)
                        .with(user(USER_EMAIL))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
