package in.landstack.domain.entity;

import jakarta.persistence.Entity;
import org.hibernate.annotations.Filter;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "adapter_endpoints")
@Filter(name = "stateFilter", condition = "state_code = :stateCode")
@Data
public class AdapterEndpoint {
    @Id
    private UUID id;
    
    @ManyToOne
    @JoinColumn(name = "state_code")
    private StateAdapter stateAdapter;
    
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private in.landstack.domain.enums.AdapterCapability capability;
    private String path;
    private String method;
}




