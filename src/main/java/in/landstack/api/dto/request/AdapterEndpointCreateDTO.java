package in.landstack.api.dto.request;

import in.landstack.domain.enums.AdapterCapability;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AdapterEndpointCreateDTO {
    @NotNull(message = "Capability is required")
    private AdapterCapability capability;

    @NotBlank(message = "Path is required")
    @Pattern(regexp = "^/.*", message = "Path must start with /")
    private String path;

    @NotBlank(message = "Method is required")
    @Pattern(regexp = "^(GET|POST|PUT|DELETE|PATCH)$", message = "Method must be a valid HTTP method")
    private String method;
}
