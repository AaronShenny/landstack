package in.landstack.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AdapterEndpointUpdateDTO {
    @NotBlank(message = "Path is required")
    @Pattern(regexp = "^/.*", message = "Path must start with /")
    private String path;

    @NotBlank(message = "Method is required")
    @Pattern(regexp = "^(GET|POST|PUT|DELETE|PATCH)$", message = "Method must be a valid HTTP method")
    private String method;
}
