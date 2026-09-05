package in.landstack.domain.repository;

import in.landstack.domain.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
    List<UserRole> findByUser_UserId(UUID userId);
    void deleteByUser_UserIdAndRole_RoleId(UUID userId, String roleId);
}
