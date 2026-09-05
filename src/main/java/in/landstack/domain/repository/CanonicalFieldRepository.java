package in.landstack.domain.repository;

import in.landstack.domain.entity.CanonicalField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CanonicalFieldRepository extends JpaRepository<CanonicalField, String> {
    List<CanonicalField> findByResource_ResourceName(String resourceName);
}
