package in.landstack.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Map;
import java.util.UUID;

@Data
public class DiscoveryRequestDTO {
    @NotNull(message = "Endpoint ID is required")
    private UUID endpointId;

    private Map<String, String> uriVariables;
}
