package in.landstack.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "states")
@Data
public class State {
    @Id
    private String stateCode;
    private String name;
}
