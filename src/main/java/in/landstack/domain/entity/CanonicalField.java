package in.landstack.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.Data;

@Entity
@Table(name = "canonical_fields")
@Data
public class CanonicalField {
    @Id
    private String fieldId;
    
    @ManyToOne
    @JoinColumn(name = "resource_name")
    private Resource resource;
    
    private String dataType;
    private Integer permlevel;
}
