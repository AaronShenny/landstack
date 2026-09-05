package in.landstack.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleAssignDTO {
    @NotBlank(message = "Role ID is required")
    private String roleId;
}
