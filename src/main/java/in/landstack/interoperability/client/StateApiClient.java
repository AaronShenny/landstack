package in.landstack.interoperability.client;

import in.landstack.domain.entity.AdapterEndpoint;
import in.landstack.domain.entity.StateAdapter;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
public class StateApiClient {

    private final RestTemplate restTemplate;

    public StateApiClient(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public String executeRequest(StateAdapter adapter, AdapterEndpoint endpoint, Map<String, String> queryParams) {
        String url = adapter.getBaseUrl() + endpoint.getPath();
        
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
        if (queryParams != null) {
            queryParams.forEach(uriBuilder::queryParam);
        }

        HttpHeaders headers = new HttpHeaders();
        if ("API_KEY".equalsIgnoreCase(adapter.getAuthType())) {
            headers.set("Authorization", adapter.getAuthCredentials());
        }

        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        HttpMethod method = HttpMethod.valueOf(endpoint.getMethod().toUpperCase());

        ResponseEntity<String> response = restTemplate.exchange(
                uriBuilder.toUriString(),
                method,
                entity,
                String.class
        );

        return response.getBody();
    }
}
