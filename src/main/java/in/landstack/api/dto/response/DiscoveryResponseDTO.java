package in.landstack.api.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class DiscoveryResponseDTO {
    private List<String> discoveredFields;
    private List<String> canonicalFields;
    private List<MappingSuggestionDTO> suggestions;
}
