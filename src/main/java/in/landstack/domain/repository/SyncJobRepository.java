package in.landstack.domain.repository;

import in.landstack.domain.entity.SyncJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SyncJobRepository extends JpaRepository<SyncJob, UUID> {
    List<SyncJob> findByStateAdapter_StateCodeOrderByStartedAtDesc(String stateCode);
    List<SyncJob> findByStatusOrderByStartedAtDesc(String status);
    List<SyncJob> findByStateAdapter_StateCodeAndStatusOrderByStartedAtDesc(String stateCode, String status);
}
