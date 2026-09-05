package in.landstack.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "adapter_field_mappings")
@Data
public class AdapterFieldMapping {
    @Id
    private UUID id;
    
    @ManyToOne
    @JoinColumn(name = "state_code")
    private StateAdapter stateAdapter;
    
    private String capability;
    private String sourceField;
    private String canonicalField;
}
