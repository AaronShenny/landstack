package in.landstack.domain.repository;

import in.landstack.domain.entity.AdapterFieldMapping;
import in.landstack.domain.enums.AdapterCapability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AdapterFieldMappingRepository extends JpaRepository<AdapterFieldMapping, UUID> {
    List<AdapterFieldMapping> findByStateAdapter_StateCodeAndCapability(String stateCode, AdapterCapability capability);
    List<AdapterFieldMapping> findByStateAdapter_StateCode(String stateCode);
}

