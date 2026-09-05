package in.landstack.domain.repository;

import in.landstack.domain.entity.AdapterTestRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AdapterTestRunRepository extends JpaRepository<AdapterTestRun, UUID> {
    List<AdapterTestRun> findByStateCodeOrderByTestTimestampDesc(String stateCode);
}
