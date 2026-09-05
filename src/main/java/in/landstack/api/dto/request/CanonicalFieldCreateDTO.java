package in.landstack.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CanonicalFieldCreateDTO {
    @NotBlank(message = "Field ID is required")
    private String fieldId;
    
    @NotBlank(message = "Resource Name is required")
    private String resourceName;
    
    @NotBlank(message = "Data Type is required")
    private String dataType;
    
    @NotNull(message = "Permlevel is required")
    private Integer permlevel;
}
