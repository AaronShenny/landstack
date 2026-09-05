package in.landstack.domain.repository;

import in.landstack.domain.entity.DiscoveryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DiscoveryLogRepository extends JpaRepository<DiscoveryLog, UUID> {
}
