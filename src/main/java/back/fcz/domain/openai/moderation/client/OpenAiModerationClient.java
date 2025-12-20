package back.fcz.domain.openai.moderation.client;

import back.fcz.domain.openai.moderation.client.dto.OpenAiModerationRequest;
import back.fcz.domain.openai.moderation.client.dto.OpenAiModerationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class OpenAiModerationClient {

    private final RestTemplate restTemplate;

    @Value("${openai.api-key:}")
    private String apiKey;

    @Value("${openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    public OpenAiModerationResponse moderate(String model, String input) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("openai.api-key is empty");
        }

        String url = baseUrl + "/moderations";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        OpenAiModerationRequest body = new OpenAiModerationRequest(model, input);
        HttpEntity<OpenAiModerationRequest> entity = new HttpEntity<>(body, headers);

        ResponseEntity<OpenAiModerationResponse> resp =
                restTemplate.exchange(url, HttpMethod.POST, entity, OpenAiModerationResponse.class);

        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new IllegalStateException("OpenAI moderation request failed: " + resp.getStatusCode());
        }

        return resp.getBody();
    }
}
