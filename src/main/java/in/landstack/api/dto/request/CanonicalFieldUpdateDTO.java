package in.landstack.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CanonicalFieldUpdateDTO {
    @NotNull(message = "Permlevel is required")
    private Integer permlevel;
}
