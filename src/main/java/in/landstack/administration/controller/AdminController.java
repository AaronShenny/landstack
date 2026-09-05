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

    @org.springframework.beans.factory.annotation.Autowired
    private in.landstack.interoperability.scheduler.DynamicSyncScheduler dynamicSyncScheduler;

    @org.springframework.beans.factory.annotation.Autowired
    private in.landstack.domain.repository.SyncJobRepository syncJobRepository;

    @PostMapping("/adapters/{stateCode}/sync/trigger")
    public ResponseEntity<?> triggerManualSync(@PathVariable String stateCode) {
        in.landstack.domain.entity.StateAdapter adapter = stateAdapterRepository.findById(stateCode).orElse(null);
        if (adapter == null) {
            return ResponseEntity.notFound().build();
        }
        in.landstack.domain.entity.SyncJob job = dynamicSyncScheduler.executeSyncJob(adapter);
        return ResponseEntity.ok(job);
    }

    @GetMapping("/sync-jobs")
    public ResponseEntity<java.util.List<in.landstack.domain.entity.SyncJob>> getSyncJobs(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String stateCode,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String status) {
            
        if (stateCode != null && status != null) {
            return ResponseEntity.ok(syncJobRepository.findByStateAdapter_StateCodeAndStatusOrderByStartedAtDesc(stateCode, status));
        } else if (stateCode != null) {
            return ResponseEntity.ok(syncJobRepository.findByStateAdapter_StateCodeOrderByStartedAtDesc(stateCode));
        } else if (status != null) {
            return ResponseEntity.ok(syncJobRepository.findByStatusOrderByStartedAtDesc(status));
        }
        return ResponseEntity.ok(syncJobRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "startedAt")));
    }
}

