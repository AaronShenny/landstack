package in.landstack.domain.entity;

import in.landstack.domain.enums.AdapterCapability;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "adapter_capabilities")
@Data
public class AdapterCapabilityFlag {
    @Id
    private UUID id;

    @Column(name = "state_code")
    private String stateCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "capability")
    private AdapterCapability capability;

    @Column(name = "is_enabled")
    private boolean isEnabled;
}
