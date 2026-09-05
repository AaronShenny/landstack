package in.landstack.domain.repository;

import in.landstack.domain.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {
    List<RolePermission> findByRole_RoleIdAndResource_ResourceName(String roleId, String resourceName);
    List<RolePermission> findByRole_RoleIdInAndResource_ResourceName(List<String> roleIds, String resourceName);
}
