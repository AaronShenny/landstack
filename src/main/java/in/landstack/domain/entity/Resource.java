package in.landstack.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "resources")
@Data
public class Resource {
    @Id
    private String resourceName;
    private String description;
}
