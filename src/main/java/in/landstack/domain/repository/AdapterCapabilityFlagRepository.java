package in.landstack.domain.repository;

import in.landstack.domain.entity.AdapterCapabilityFlag;
import in.landstack.domain.enums.AdapterCapability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdapterCapabilityFlagRepository extends JpaRepository<AdapterCapabilityFlag, UUID> {
    Optional<AdapterCapabilityFlag> findByStateCodeAndCapability(String stateCode, AdapterCapability capability);
    List<AdapterCapabilityFlag> findByStateCode(String stateCode);
}
