package in.landstack.api.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class MappingSuggestionDTO {
    private String sourceField;
    private String canonicalField;
    private double confidenceScore;
}
