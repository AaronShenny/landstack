package in.landstack.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RolePermissionCreateDTO {
    @NotBlank(message = "Resource Name is required")
    private String resourceName;
    
    @NotNull(message = "Permlevel is required")
    private Integer permlevel;
    
    private Boolean canCreate = false;
    private Boolean canRead = false;
    private Boolean canUpdate = false;
    private Boolean canDelete = false;
}
