package in.landstack.domain.repository;

import in.landstack.domain.entity.AdapterEndpoint;
import in.landstack.domain.enums.AdapterCapability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdapterEndpointRepository extends JpaRepository<AdapterEndpoint, UUID> {
    Optional<AdapterEndpoint> findByStateAdapter_StateCodeAndCapability(String stateCode, AdapterCapability capability);
    List<AdapterEndpoint> findByStateAdapter_StateCode(String stateCode);
}
