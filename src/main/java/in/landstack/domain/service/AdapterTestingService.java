package in.landstack.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.landstack.api.dto.request.AdapterTestRequestDTO;
import in.landstack.api.dto.response.AdapterTestResponseDTO;
import in.landstack.api.dto.response.EndpointTestResultDTO;
import in.landstack.domain.entity.AdapterEndpoint;
import in.landstack.domain.entity.AdapterFieldMapping;
import in.landstack.domain.entity.AdapterTestRun;
import in.landstack.domain.entity.StateAdapter;
import in.landstack.domain.repository.AdapterEndpointRepository;
import in.landstack.domain.repository.AdapterFieldMappingRepository;
import in.landstack.domain.repository.AdapterTestRunRepository;
import in.landstack.interoperability.mapper.DynamicFieldMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AdapterTestingService {

    private final AdapterEndpointRepository endpointRepository;
    private final AdapterFieldMappingRepository mappingRepository;
    private final AdapterTestRunRepository testRunRepository;
    private final StateAdapterService stateAdapterService;
    private final DynamicFieldMapper dynamicFieldMapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AdapterTestingService(AdapterEndpointRepository endpointRepository,
                                 AdapterFieldMappingRepository mappingRepository,
                                 AdapterTestRunRepository testRunRepository,
                                 StateAdapterService stateAdapterService,
                                 DynamicFieldMapper dynamicFieldMapper,
                                 ObjectMapper objectMapper) {
        this.endpointRepository = endpointRepository;
        this.mappingRepository = mappingRepository;
        this.testRunRepository = testRunRepository;
        this.stateAdapterService = stateAdapterService;
        this.dynamicFieldMapper = dynamicFieldMapper;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    public AdapterTestResponseDTO runTests(String stateCode, AdapterTestRequestDTO requestDTO) {
        StateAdapter adapter = stateAdapterService.getAdapter(stateCode);
        List<AdapterEndpoint> endpoints = endpointRepository.findByStateAdapter_StateCode(stateCode);

        if (endpoints.isEmpty()) {
            throw new IllegalArgumentException("No endpoints configured for this adapter to test");
        }

        boolean overallSuccess = true;
        List<EndpointTestResultDTO> results = new ArrayList<>();

        for (AdapterEndpoint endpoint : endpoints) {
            EndpointTestResultDTO resultDTO = new EndpointTestResultDTO();
            resultDTO.setCapability(endpoint.getCapability().name());
            
            String fullUrl = adapter.getBaseUrl() + endpoint.getPath();
            
            // Apply mock params
            Map<String, String> params = requestDTO.getMockParams() != null ? 
                                         requestDTO.getMockParams().get(endpoint.getCapability().name()) : null;
            if (params != null) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    fullUrl = fullUrl.replace("{" + entry.getKey() + "}", entry.getValue());
                }
            }
            
            resultDTO.setEndpointUrl(fullUrl);

            try {
                // Probe request
                String responseBody = restTemplate.getForObject(fullUrl, String.class);
                
                // Fetch mappings
                List<AdapterFieldMapping> mappings = mappingRepository
                    .findByStateAdapter_StateCodeAndCapability(stateCode, endpoint.getCapability());
                
                // Validate JSON and Apply mappings
                JsonNode canonicalOutput = dynamicFieldMapper.mapToCanonical(responseBody, mappings);
                resultDTO.setPreviewCanonicalOutput(canonicalOutput);
                resultDTO.setSuccessful(true);
            } catch (Exception e) {
                resultDTO.setSuccessful(false);
                resultDTO.setErrorMessage(e.getMessage());
                overallSuccess = false;
            }
            
            results.add(resultDTO);
        }

        // Store result
        AdapterTestRun testRun = new AdapterTestRun();
        testRun.setId(UUID.randomUUID());
        testRun.setStateCode(stateCode);
        testRun.setTestTimestamp(LocalDateTime.now());
        testRun.setSuccessful(overallSuccess);
        
        try {
            testRun.setDetailedResults(objectMapper.writeValueAsString(results));
        } catch (Exception e) {
            testRun.setDetailedResults("[]");
        }
        
        testRunRepository.save(testRun);

        AdapterTestResponseDTO response = new AdapterTestResponseDTO();
        response.setOverallSuccess(overallSuccess);
        response.setResults(results);

        return response;
    }
}
