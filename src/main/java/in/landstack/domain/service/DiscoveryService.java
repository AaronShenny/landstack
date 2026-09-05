package in.landstack.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.landstack.api.dto.request.DiscoveryRequestDTO;
import in.landstack.api.dto.response.DiscoveryResponseDTO;
import in.landstack.api.dto.response.MappingSuggestionDTO;
import in.landstack.domain.entity.AdapterEndpoint;
import in.landstack.domain.entity.CanonicalField;
import in.landstack.domain.entity.DiscoveryLog;
import in.landstack.domain.entity.StateAdapter;
import in.landstack.domain.repository.AdapterEndpointRepository;
import in.landstack.domain.repository.CanonicalFieldRepository;
import in.landstack.domain.repository.DiscoveryLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DiscoveryService {

    private final AdapterEndpointRepository endpointRepository;
    private final CanonicalFieldRepository canonicalFieldRepository;
    private final DiscoveryLogRepository discoveryLogRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public DiscoveryService(AdapterEndpointRepository endpointRepository,
                            CanonicalFieldRepository canonicalFieldRepository,
                            DiscoveryLogRepository discoveryLogRepository,
                            ObjectMapper objectMapper) {
        this.endpointRepository = endpointRepository;
        this.canonicalFieldRepository = canonicalFieldRepository;
        this.discoveryLogRepository = discoveryLogRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    public DiscoveryResponseDTO discoverFields(String stateCode, DiscoveryRequestDTO dto) {
        AdapterEndpoint endpoint = endpointRepository.findById(dto.getEndpointId())
                .orElseThrow(() -> new IllegalArgumentException("Endpoint not found"));

        if (!endpoint.getStateAdapter().getStateCode().equals(stateCode)) {
            throw new IllegalArgumentException("Endpoint does not belong to the state");
        }

        StateAdapter adapter = endpoint.getStateAdapter();
        String fullUrl = adapter.getBaseUrl() + endpoint.getPath();

        // Replace URI variables
        if (dto.getUriVariables() != null) {
            for (Map.Entry<String, String> entry : dto.getUriVariables().entrySet()) {
                fullUrl = fullUrl.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }

        // Make HTTP request
        String responseBody;
        try {
            responseBody = restTemplate.getForObject(fullUrl, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to call endpoint: " + e.getMessage());
        }

        // Parse JSON and extract fields
        List<String> discoveredFields = new ArrayList<>();
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            extractPaths("", rootNode, discoveredFields);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON response");
        }

        // Get canonical fields for the capability
        List<CanonicalField> canonicalFields = canonicalFieldRepository.findByResource_ResourceName(endpoint.getCapability().name());
        List<String> canonicalFieldNames = canonicalFields.stream().map(CanonicalField::getFieldId).toList();

        // Generate suggestions
        List<MappingSuggestionDTO> suggestions = generateSuggestions(discoveredFields, canonicalFieldNames);

        // Log discovery
        DiscoveryLog log = new DiscoveryLog();
        log.setId(UUID.randomUUID());
        log.setStateCode(stateCode);
        log.setEndpointUrl(fullUrl);
        log.setSampleResponseHash(hash(responseBody));
        log.setDiscoveredFields(String.join(",", discoveredFields));
        log.setAttemptTimestamp(LocalDateTime.now());
        discoveryLogRepository.save(log);

        DiscoveryResponseDTO response = new DiscoveryResponseDTO();
        response.setDiscoveredFields(discoveredFields);
        response.setCanonicalFields(canonicalFieldNames);
        response.setSuggestions(suggestions);
        return response;
    }

    private void throwException(String msg) {
        throw new IllegalArgumentException(msg);
    }

    private void extractPaths(String currentPath, JsonNode node, List<String> paths) {
        if (node.isObject()) {
            node.fieldNames().forEachRemaining(fieldName -> {
                String newPath = currentPath.isEmpty() ? fieldName : currentPath + "." + fieldName;
                extractPaths(newPath, node.get(fieldName), paths);
            });
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                String newPath = currentPath + "[" + i + "]";
                extractPaths(newPath, node.get(i), paths);
            }
        } else {
            paths.add("$." + currentPath);
        }
    }

    private String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    private List<MappingSuggestionDTO> generateSuggestions(List<String> discovered, List<String> canonical) {
        List<MappingSuggestionDTO> suggestions = new ArrayList<>();
        
        for (String cField : canonical) {
            String bestMatch = null;
            double bestScore = 0.0;
            
            for (String dField : discovered) {
                // Extract last part for matching (e.g. $.owner.name -> name)
                String dFieldNormalized = dField.replaceAll("^.*\\.", "").replaceAll("\\[[0-9]+\\]", "").toLowerCase();
                String cFieldNormalized = cField.toLowerCase();
                
                double score = getSimilarity(cFieldNormalized, dFieldNormalized);
                
                // Add some hardcoded smart matches
                if ((cFieldNormalized.equals("localparcelid") && dFieldNormalized.contains("survey")) ||
                    (cFieldNormalized.equals("area") && dFieldNormalized.contains("hect"))) {
                    score = Math.max(score, 0.85);
                }

                if (score > bestScore) {
                    bestScore = score;
                    bestMatch = dField;
                }
            }
            
            if (bestMatch != null && bestScore > 0.4) {
                MappingSuggestionDTO dto = new MappingSuggestionDTO();
                dto.setCanonicalField(cField);
                dto.setSourceField(bestMatch);
                dto.setConfidenceScore(Math.round(bestScore * 100.0) / 100.0);
                suggestions.add(dto);
            }
        }
        
        suggestions.sort((a, b) -> Double.compare(b.getConfidenceScore(), a.getConfidenceScore()));
        return suggestions;
    }

    // Very basic Levenshtein distance based similarity
    private double getSimilarity(String s1, String s2) {
        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) return 1.0;
        int distance = levenshtein(s1, s2);
        return 1.0 - ((double) distance / maxLength);
    }

    private int levenshtein(String s1, String s2) {
        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    costs[j] = j;
                } else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1)) {
                            newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                        }
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0) costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }
}

