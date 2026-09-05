package in.landstack.api.dto.request;

import lombok.Data;
import java.util.Map;

@Data
public class AdapterTestRequestDTO {
    // Map of Capability -> Map of URI variables (e.g. RECORD_OF_RIGHTS -> {"ulpin": "KL-1234"})
    private Map<String, Map<String, String>> mockParams;
}
