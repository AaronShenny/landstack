package in.landstack.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdapterFieldMappingUpdateDTO {
    @NotBlank(message = "Source field is required")
    private String sourceField;

    @NotBlank(message = "Canonical field is required")
    private String canonicalField;

    private String transformHint;
}
