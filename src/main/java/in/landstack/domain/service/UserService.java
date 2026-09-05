package in.landstack.domain.service;

import in.landstack.api.dto.request.UserCreateDTO;
import in.landstack.api.dto.response.UserDTO;
import in.landstack.domain.entity.Role;
import in.landstack.domain.entity.User;
import in.landstack.domain.entity.UserPermission;
import in.landstack.domain.entity.UserRole;
import in.landstack.domain.repository.RoleRepository;
import in.landstack.domain.repository.UserPermissionRepository;
import in.landstack.domain.repository.UserRepository;
import in.landstack.domain.repository.UserRoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       UserRoleRepository userRoleRepository, UserPermissionRepository userPermissionRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.userPermissionRepository = userPermissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserDTO createUser(UserCreateDTO dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setIsSuperadmin(dto.getIsSuperadmin());

        User savedUser = userRepository.save(user);
        return mapToDTO(savedUser);
    }

    @Transactional
    public void assignRole(UUID userId, String roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        UserRole userRole = new UserRole();
        userRole.setId(UUID.randomUUID());
        userRole.setUser(user);
        userRole.setRole(role);
        userRoleRepository.save(userRole);
    }

    @Transactional
    public void removeRole(UUID userId, String roleId) {
        userRoleRepository.deleteByUser_UserIdAndRole_RoleId(userId, roleId);
    }

    @Transactional
    public void assignPermission(UUID userId, String allowType, String forValue) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserPermission permission = new UserPermission();
        permission.setId(UUID.randomUUID());
        permission.setUser(user);
        permission.setAllowType(allowType);
        permission.setForValue(forValue);
        userPermissionRepository.save(permission);
    }

    @Transactional
    public void deletePermission(UUID permissionId) {
        userPermissionRepository.deleteById(permissionId);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        userRepository.deleteById(userId);
    }

    public UserDTO getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return mapToDTO(user);
    }

    public UserDTO mapToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setIsSuperadmin(user.getIsSuperadmin());

        List<String> roles = userRoleRepository.findByUser_UserId(user.getUserId())
                .stream()
                .map(ur -> ur.getRole().getRoleId())
                .collect(Collectors.toList());
        dto.setRoles(roles);

        return dto;
    }
}
