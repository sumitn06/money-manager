package moneymanager.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EmailService {

    private final RestClient restClient = RestClient.create();

    @Value("${brevo.api-key}")
    private String apiKey;

    @Value("${brevo.sender-email}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String body) {

        Map<String, Object> payload = Map.of(
                "sender", Map.of(
                        "name", "Money Manager",
                        "email", fromEmail
                ),
                "to", List.of(
                        Map.of("email", to)
                ),
                "subject", subject,
                "htmlContent", body
        );

        try {
            restClient.post()
                    .uri("https://api.brevo.com/v3/smtp/email")
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Email accepted by Brevo for: {}", to);

        } catch (RestClientResponseException e) {
            log.error(
                    "Brevo API error. Status: {}, Response: {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString()
            );

            throw new RuntimeException(
                    "Brevo rejected the email request", e
            );

        } catch (RestClientException e) {
            log.error("Could not connect to Brevo API", e);

            throw new RuntimeException(
                    "Could not connect to Brevo email service", e
            );
        }
    }
}
