package in.landstack.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.Data;

@Entity
@Table(name = "villages")
@Data
public class Village {
    @Id
    private String villageCode;
    
    @ManyToOne
    @JoinColumn(name = "sub_district_code")
    private SubDistrict subDistrict;
    
    private String name;
}
