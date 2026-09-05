package in.landstack.domain.service;

import in.landstack.api.dto.request.AdapterFieldMappingCreateDTO;
import in.landstack.api.dto.request.AdapterFieldMappingUpdateDTO;
import in.landstack.domain.entity.AdapterFieldMapping;
import in.landstack.domain.entity.StateAdapter;
import in.landstack.domain.repository.AdapterFieldMappingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AdapterFieldMappingService {

    private final AdapterFieldMappingRepository mappingRepository;
    private final StateAdapterService stateAdapterService;

    public AdapterFieldMappingService(AdapterFieldMappingRepository mappingRepository, StateAdapterService stateAdapterService) {
        this.mappingRepository = mappingRepository;
        this.stateAdapterService = stateAdapterService;
    }

    public List<AdapterFieldMapping> getMappingsForAdapter(String stateCode) {
        return mappingRepository.findByStateAdapter_StateCode(stateCode);
    }

    @Transactional
    public AdapterFieldMapping createMapping(String stateCode, AdapterFieldMappingCreateDTO dto) {
        StateAdapter adapter = stateAdapterService.getAdapter(stateCode);

        AdapterFieldMapping mapping = new AdapterFieldMapping();
        mapping.setId(UUID.randomUUID());
        mapping.setStateAdapter(adapter);
        mapping.setCapability(dto.getCapability());
        mapping.setSourceField(dto.getSourceField());
        mapping.setCanonicalField(dto.getCanonicalField());
        mapping.setTransformHint(dto.getTransformHint());

        return mappingRepository.save(mapping);
    }

    @Transactional
    public AdapterFieldMapping updateMapping(String stateCode, UUID mappingId, AdapterFieldMappingUpdateDTO dto) {
        AdapterFieldMapping mapping = mappingRepository.findById(mappingId)
                .orElseThrow(() -> new IllegalArgumentException("Mapping not found"));
                
        if (!mapping.getStateAdapter().getStateCode().equals(stateCode)) {
            throw new IllegalArgumentException("Mapping does not belong to the specified state adapter");
        }

        mapping.setSourceField(dto.getSourceField());
        mapping.setCanonicalField(dto.getCanonicalField());
        mapping.setTransformHint(dto.getTransformHint());

        return mappingRepository.save(mapping);
    }

    @Transactional
    public void deleteMapping(String stateCode, UUID mappingId) {
        AdapterFieldMapping mapping = mappingRepository.findById(mappingId)
                .orElseThrow(() -> new IllegalArgumentException("Mapping not found"));
                
        if (!mapping.getStateAdapter().getStateCode().equals(stateCode)) {
            throw new IllegalArgumentException("Mapping does not belong to the specified state adapter");
        }

        mappingRepository.delete(mapping);
    }
}
