package in.landstack.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "discovery_logs")
@Data
public class DiscoveryLog {
    @Id
    private UUID id;

    @Column(name = "state_code")
    private String stateCode;

    @Column(name = "endpoint_url")
    private String endpointUrl;

    @Column(name = "sample_response_hash")
    private String sampleResponseHash;

    @Column(name = "discovered_fields")
    private String discoveredFields;

    @Column(name = "attempt_timestamp")
    private LocalDateTime attemptTimestamp;
}
