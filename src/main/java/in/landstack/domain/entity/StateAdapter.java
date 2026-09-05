package in.landstack.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import lombok.Data;

@Entity
@Table(name = "state_adapters")
@Filter(name = "stateFilter", condition = "state_code = :stateCode")
@Data
public class StateAdapter {
    @Id
    private String stateCode;
    
    @OneToOne
    @JoinColumn(name = "state_code")
    private State state;
    
    private String baseUrl;
    private String authType;
    private String authCredentials;
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private in.landstack.domain.enums.AdapterStatus status;
    private String syncCronExpression;
}

