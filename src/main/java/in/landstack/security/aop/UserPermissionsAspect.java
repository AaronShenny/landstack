package in.landstack.security.aop;

import in.landstack.domain.entity.UserPermission;
import in.landstack.domain.repository.UserPermissionRepository;
import in.landstack.domain.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Aspect
@Component
public class UserPermissionsAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserPermissionRepository userPermissionRepository;

    @Before("execution(* in.landstack.domain.repository.ParcelRepository.*(..)) || execution(* in.landstack.domain.repository.SyncJobRepository.*(..)) || execution(* in.landstack.domain.repository.StateAdapterRepository.*(..)) || execution(* in.landstack.domain.repository.AdapterEndpointRepository.*(..)) || execution(* in.landstack.domain.repository.AdapterFieldMappingRepository.*(..))")
    public void enableUserPermissionsFilter() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            String username = authentication.getName();
            userRepository.findByUsername(username).ifPresent(user -> {
                if (user.getIsSuperadmin() != null && user.getIsSuperadmin()) {
                    return; // Superadmin sees everything
                }

                List<UserPermission> permissions = userPermissionRepository.findByUser_UserId(user.getUserId());
                for (UserPermission perm : permissions) {
                    if ("state_code".equals(perm.getAllowType())) {
                        Session session = entityManager.unwrap(Session.class);
                        session.enableFilter("stateFilter").setParameter("stateCode", perm.getForValue());
                    }
                }
            });
        }
    }
}


