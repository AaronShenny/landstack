package in.landstack.api.dto.request;

import in.landstack.domain.enums.AdapterCapability;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdapterFieldMappingCreateDTO {
    @NotNull(message = "Capability is required")
    private AdapterCapability capability;

    @NotBlank(message = "Source field is required")
    private String sourceField;

    @NotBlank(message = "Canonical field is required")
    private String canonicalField;

    private String transformHint;
}
