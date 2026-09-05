package in.landstack.domain.service;

import in.landstack.api.dto.request.RoleCreateDTO;
import in.landstack.api.dto.request.RolePermissionCreateDTO;
import in.landstack.domain.entity.Resource;
import in.landstack.domain.entity.Role;
import in.landstack.domain.entity.RolePermission;
import in.landstack.domain.repository.ResourceRepository;
import in.landstack.domain.repository.RolePermissionRepository;
import in.landstack.domain.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final ResourceRepository resourceRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public RoleService(RoleRepository roleRepository, ResourceRepository resourceRepository,
                       RolePermissionRepository rolePermissionRepository) {
        this.roleRepository = roleRepository;
        this.resourceRepository = resourceRepository;
        this.rolePermissionRepository = rolePermissionRepository;
    }

    @Transactional
    public Role createRole(RoleCreateDTO dto) {
        if (roleRepository.existsById(dto.getRoleId())) {
            throw new IllegalArgumentException("Role already exists");
        }
        Role role = new Role();
        role.setRoleId(dto.getRoleId());
        role.setDescription(dto.getDescription());
        return roleRepository.save(role);
    }

    @Transactional
    public RolePermission assignPermission(String roleId, RolePermissionCreateDTO dto) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
        Resource resource = resourceRepository.findById(dto.getResourceName())
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));

        RolePermission rp = new RolePermission();
        rp.setId(UUID.randomUUID());
        rp.setRole(role);
        rp.setResource(resource);
        rp.setPermlevel(dto.getPermlevel());
        rp.setCanCreate(dto.getCanCreate());
        rp.setCanRead(dto.getCanRead());
        rp.setCanUpdate(dto.getCanUpdate());
        rp.setCanDelete(dto.getCanDelete());
        
        return rolePermissionRepository.save(rp);
    }

    public List<Resource> getAllResources() {
        return resourceRepository.findAll();
    }
}
