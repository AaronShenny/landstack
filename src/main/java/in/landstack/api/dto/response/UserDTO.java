package in.landstack.api.dto.response;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class UserDTO {
    private UUID userId;
    private String username;
    private String email;
    private Boolean isSuperadmin;
    private List<String> roles;
}
