package in.landstack.api.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class AdapterTestResponseDTO {
    private boolean overallSuccess;
    private List<EndpointTestResultDTO> results;
}
