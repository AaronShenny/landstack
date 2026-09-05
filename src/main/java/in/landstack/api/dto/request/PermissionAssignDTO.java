package in.landstack.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PermissionAssignDTO {
    @NotBlank(message = "Allow Type is required")
    private String allowType;
    
    @NotBlank(message = "For Value is required")
    private String forValue;
}
