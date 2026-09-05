package in.landstack.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class StateAdapterCreateDTO {
    @NotBlank(message = "State Code is required")
    @Pattern(regexp = "^(AP|AR|AS|BR|CG|GA|GJ|HR|HP|JH|KA|KL|MP|MH|MN|ML|MZ|NL|OD|PB|RJ|SK|TN|TG|TR|UP|UK|WB|AN|CH|DN|DD|DL|JK|LA|LD|PY)$", message = "Must be a valid ISO 3166-2:IN state code (e.g. KL)")
    private String stateCode;

    @NotBlank(message = "State Name is required")
    private String stateName;
    
    @NotBlank(message = "Base URL is required")
    private String baseUrl;
}
