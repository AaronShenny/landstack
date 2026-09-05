package in.landstack.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StateAdapterUpdateDTO {
    @NotBlank(message = "Base URL is required")
    private String baseUrl;
    
    private String authType;
    
    private String authCredentials;
    
    @NotBlank(message = "Sync Cron Expression is required")
    private String syncCronExpression;
}
