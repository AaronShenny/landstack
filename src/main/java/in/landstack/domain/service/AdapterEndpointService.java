package in.landstack.domain.service;

import in.landstack.api.dto.request.AdapterEndpointCreateDTO;
import in.landstack.api.dto.request.AdapterEndpointUpdateDTO;
import in.landstack.domain.entity.AdapterEndpoint;
import in.landstack.domain.entity.StateAdapter;
import in.landstack.domain.repository.AdapterEndpointRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.util.List;
import java.util.UUID;

@Service
public class AdapterEndpointService {

    private final AdapterEndpointRepository endpointRepository;
    private final StateAdapterService stateAdapterService;

    // A simple pattern to detect {variableName}
    private static final Pattern URI_VAR_PATTERN = Pattern.compile("\\{([^/]+)\\}");

    public AdapterEndpointService(AdapterEndpointRepository endpointRepository, StateAdapterService stateAdapterService) {
        this.endpointRepository = endpointRepository;
        this.stateAdapterService = stateAdapterService;
    }

    public List<AdapterEndpoint> getEndpointsForAdapter(String stateCode) {
        return endpointRepository.findByStateAdapter_StateCode(stateCode);
    }

    private void validatePath(String path) {
        // Ensure that path templates use consistent URI-variable notation
        // For instance, we expect paths that need a parcel id to use {ulpin} or {localParcelId}
        Matcher matcher = URI_VAR_PATTERN.matcher(path);
        while (matcher.find()) {
            String varName = matcher.group(1);
            if (!varName.matches("^[a-zA-Z0-9_]+$")) {
                throw new IllegalArgumentException("Invalid URI variable notation in path: {" + varName + "}. Variables must be alphanumeric.");
            }
        }
    }

    @Transactional
    public AdapterEndpoint createEndpoint(String stateCode, AdapterEndpointCreateDTO dto) {
        StateAdapter adapter = stateAdapterService.getAdapter(stateCode);
        
        validatePath(dto.getPath());

        AdapterEndpoint endpoint = new AdapterEndpoint();
        endpoint.setId(UUID.randomUUID());
        endpoint.setStateAdapter(adapter);
        endpoint.setCapability(dto.getCapability());
        endpoint.setPath(dto.getPath());
        endpoint.setMethod(dto.getMethod());

        return endpointRepository.save(endpoint);
    }

    @Transactional
    public AdapterEndpoint updateEndpoint(String stateCode, UUID endpointId, AdapterEndpointUpdateDTO dto) {
        AdapterEndpoint endpoint = endpointRepository.findById(endpointId)
                .orElseThrow(() -> new IllegalArgumentException("Endpoint not found"));
                
        if (!endpoint.getStateAdapter().getStateCode().equals(stateCode)) {
            throw new IllegalArgumentException("Endpoint does not belong to the specified state adapter");
        }

        validatePath(dto.getPath());

        endpoint.setPath(dto.getPath());
        endpoint.setMethod(dto.getMethod());

        return endpointRepository.save(endpoint);
    }

    @Transactional
    public void deleteEndpoint(String stateCode, UUID endpointId) {
        AdapterEndpoint endpoint = endpointRepository.findById(endpointId)
                .orElseThrow(() -> new IllegalArgumentException("Endpoint not found"));
                
        if (!endpoint.getStateAdapter().getStateCode().equals(stateCode)) {
            throw new IllegalArgumentException("Endpoint does not belong to the specified state adapter");
        }

        endpointRepository.delete(endpoint);
    }
}
