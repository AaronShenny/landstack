package in.landstack.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleCreateDTO {
    @NotBlank(message = "Role ID is required")
    private String roleId;
    private String description;
}
