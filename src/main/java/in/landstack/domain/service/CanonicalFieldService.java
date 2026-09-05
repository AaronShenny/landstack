package in.landstack.domain.service;

import in.landstack.api.dto.request.CanonicalFieldCreateDTO;
import in.landstack.api.dto.request.CanonicalFieldUpdateDTO;
import in.landstack.domain.entity.CanonicalField;
import in.landstack.domain.entity.Resource;
import in.landstack.domain.repository.CanonicalFieldRepository;
import in.landstack.domain.repository.ResourceRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CanonicalFieldService {

    private final CanonicalFieldRepository canonicalFieldRepository;
    private final ResourceRepository resourceRepository;

    public CanonicalFieldService(CanonicalFieldRepository canonicalFieldRepository, ResourceRepository resourceRepository) {
        this.canonicalFieldRepository = canonicalFieldRepository;
        this.resourceRepository = resourceRepository;
    }

    public List<CanonicalField> getAllFields() {
        return canonicalFieldRepository.findAll();
    }

    public List<CanonicalField> getFieldsByResource(String resourceName) {
        return canonicalFieldRepository.findByResource_ResourceName(resourceName);
    }

    @Transactional
    @CacheEvict(value = "canonicalFields", key = "#dto.resourceName")
    public CanonicalField createField(CanonicalFieldCreateDTO dto) {
        if (canonicalFieldRepository.existsById(dto.getFieldId())) {
            throw new IllegalArgumentException("Field ID already exists");
        }
        Resource resource = resourceRepository.findById(dto.getResourceName())
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));

        CanonicalField field = new CanonicalField();
        field.setFieldId(dto.getFieldId());
        field.setResource(resource);
        field.setDataType(dto.getDataType());
        field.setPermlevel(dto.getPermlevel());
        
        return canonicalFieldRepository.save(field);
    }

    @Transactional
    @CacheEvict(value = "canonicalFields", allEntries = true)
    public CanonicalField updatePermlevel(String fieldId, CanonicalFieldUpdateDTO dto) {
        CanonicalField field = canonicalFieldRepository.findById(fieldId)
                .orElseThrow(() -> new IllegalArgumentException("Field not found"));
        
        field.setPermlevel(dto.getPermlevel());
        return canonicalFieldRepository.save(field);
    }
}
