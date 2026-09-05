package in.landstack.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.Data;
import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "sync_jobs")
@Data
public class SyncJob {
    @Id
    private UUID jobId;
    
    @ManyToOne
    @JoinColumn(name = "state_code")
    private StateAdapter stateAdapter;
    
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String status;
    private Integer recordsUpdated;
    private String errorLog;
}
