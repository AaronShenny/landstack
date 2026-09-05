package in.landstack.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    private UUID userId;
    private String username;
    private String email;
    private String passwordHash;
    private Boolean isSuperadmin;
}
