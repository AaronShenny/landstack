package in.landstack.administration.controller;

import in.landstack.domain.entity.StateAdapter;
import in.landstack.domain.entity.User;
import in.landstack.domain.repository.StateAdapterRepository;
import in.landstack.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
public class AdminController {

    @Autowired
    private StateAdapterRepository stateAdapterRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/adapters")
    public ResponseEntity<List<StateAdapter>> getAllAdapters() {
        return ResponseEntity.ok(stateAdapterRepository.findAll());
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PostMapping("/users")
    public ResponseEntity<Object> createUser(@RequestBody Object userDTO) {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{userId}/permissions")
    public ResponseEntity<Object> assignUserPermission(@PathVariable UUID userId, @RequestBody Object permissionDTO) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Object>> getAllRoles() {
        return ResponseEntity.ok(List.of());
    }
}
