package in.landstack.api.controller;

import in.landstack.domain.entity.AdapterCapabilityFlag;
import in.landstack.domain.service.CapabilityCheckService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/adapters")
public class PublicAdapterController {

    private final CapabilityCheckService capabilityCheckService;

    public PublicAdapterController(CapabilityCheckService capabilityCheckService) {
        this.capabilityCheckService = capabilityCheckService;
    }

    @GetMapping("/{stateCode}/capabilities")
    public ResponseEntity<List<AdapterCapabilityFlag>> getStateCapabilities(@PathVariable String stateCode) {
        return ResponseEntity.ok(capabilityCheckService.getCapabilitiesForState(stateCode));
    }
}
