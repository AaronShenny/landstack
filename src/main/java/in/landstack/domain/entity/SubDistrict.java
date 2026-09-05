package in.landstack.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.Data;

@Entity
@Table(name = "sub_districts")
@Data
public class SubDistrict {
    @Id
    private String subDistrictCode;
    
    @ManyToOne
    @JoinColumn(name = "district_code")
    private District district;
    
    private String name;
}
