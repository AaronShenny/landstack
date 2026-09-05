package in.landstack.api.dto.response;

import lombok.Data;
import com.fasterxml.jackson.databind.JsonNode;

@Data
public class EndpointTestResultDTO {
    private String capability;
    private String endpointUrl;
    private boolean successful;
    private String errorMessage;
    private JsonNode previewCanonicalOutput;
}
