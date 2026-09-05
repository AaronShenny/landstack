package in.landstack.domain.service;

import in.landstack.domain.entity.CanonicalField;
import in.landstack.domain.entity.RolePermission;
import in.landstack.domain.repository.CanonicalFieldRepository;
import in.landstack.domain.repository.RolePermissionRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PermlevelService {

    private final RolePermissionRepository rolePermissionRepository;
    private final CanonicalFieldRepository canonicalFieldRepository;

    public PermlevelService(RolePermissionRepository rolePermissionRepository, CanonicalFieldRepository canonicalFieldRepository) {
        this.rolePermissionRepository = rolePermissionRepository;
        this.canonicalFieldRepository = canonicalFieldRepository;
    }

    public int getUserMaxPermlevel(String resourceName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return 0;
        }

        boolean isSuperadmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
        if (isSuperadmin) {
            return Integer.MAX_VALUE;
        }

        List<String> roleIds = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .collect(Collectors.toList());

        if (roleIds.isEmpty()) return 0;

        List<RolePermission> permissions = rolePermissionRepository.findByRole_RoleIdInAndResource_ResourceName(roleIds, resourceName);
        return permissions.stream()
                .mapToInt(RolePermission::getPermlevel)
                .max()
                .orElse(0);
    }

    @Cacheable("canonicalFields")
    public Map<String, Integer> getFieldPermlevels(String resourceName) {
        return canonicalFieldRepository.findByResource_ResourceName(resourceName)
                .stream()
                .collect(Collectors.toMap(CanonicalField::getFieldId, CanonicalField::getPermlevel));
    }
}
