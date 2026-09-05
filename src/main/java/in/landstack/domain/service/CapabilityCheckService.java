package in.landstack.domain.service;

import in.landstack.api.exception.CapabilityNotSupportedException;
import in.landstack.domain.entity.AdapterCapabilityFlag;
import in.landstack.domain.enums.AdapterCapability;
import in.landstack.domain.repository.AdapterCapabilityFlagRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CapabilityCheckService {

    private final AdapterCapabilityFlagRepository capabilityRepository;

    public CapabilityCheckService(AdapterCapabilityFlagRepository capabilityRepository) {
        this.capabilityRepository = capabilityRepository;
    }

    public List<AdapterCapabilityFlag> getCapabilitiesForState(String stateCode) {
        return capabilityRepository.findByStateCode(stateCode);
    }

    public boolean supportsCapability(String stateCode, AdapterCapability capability) {
        return capabilityRepository.findByStateCodeAndCapability(stateCode, capability)
                .map(AdapterCapabilityFlag::isEnabled)
                .orElse(false);
    }

    public void requireCapability(String stateCode, AdapterCapability capability) {
        if (!supportsCapability(stateCode, capability)) {
            throw new CapabilityNotSupportedException(
                String.format("State adapter '%s' does not support the capability: %s", stateCode, capability)
            );
        }
    }
}
