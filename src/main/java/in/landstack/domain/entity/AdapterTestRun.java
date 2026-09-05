package in.landstack.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "adapter_test_runs")
@Data
public class AdapterTestRun {
    @Id
    private UUID id;

    @Column(name = "state_code")
    private String stateCode;

    @Column(name = "test_timestamp")
    private LocalDateTime testTimestamp;

    @Column(name = "is_successful")
    private boolean isSuccessful;

    @Column(name = "detailed_results")
    private String detailedResults; // Store JSON string of results
}
