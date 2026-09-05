package in.landstack.domain.repository;

import in.landstack.domain.entity.AdapterEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdapterEndpointRepository extends JpaRepository<AdapterEndpoint, UUID> {
    Optional<AdapterEndpoint> findByStateAdapter_StateCodeAndCapability(String stateCode, String capability);
}
