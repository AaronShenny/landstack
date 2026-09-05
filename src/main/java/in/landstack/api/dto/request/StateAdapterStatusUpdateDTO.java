package in.landstack.api.dto.request;

import in.landstack.domain.enums.AdapterStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StateAdapterStatusUpdateDTO {
    @NotNull(message = "Target status is required")
    private AdapterStatus status;
}
