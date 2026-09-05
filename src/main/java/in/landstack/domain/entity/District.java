package in.landstack.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.Data;

@Entity
@Table(name = "districts")
@Data
public class District {
    @Id
    private String districtCode;
    
    @ManyToOne
    @JoinColumn(name = "state_code")
    private State state;
    
    private String name;
}
