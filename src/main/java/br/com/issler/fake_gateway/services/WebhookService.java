package br.com.issler.fake_gateway.services;

import br.com.issler.fake_gateway.database.models.PaymentEntity;
import br.com.issler.fake_gateway.dtos.WebhookBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.LocalDateTime;

@Service
public class WebhookService {
    private final RestClient restClient;
    private final String apiKey;

    public WebhookService(
            @Value("${spring.application.your-api-key}")
            String apiKey,
            @Value("${spring.application.your-api-url}")
            String apiUrl
    ){
        this.apiKey = apiKey;

        HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(client);
        this.restClient = RestClient.builder()
                .baseUrl(apiUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public void send(PaymentEntity payment){
        WebhookBody body = WebhookBody.builder()
                .gatewayPaymentId(payment.getId())
                .paidAt(payment.getPaidAt())
                .status(payment.getStatus())
                .build();

        // simulate sending a webhook to the API
        restClient.post()
                .uri("/v1/webhooks")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-API-KEY", apiKey)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}
