package in.landstack.interoperability.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.landstack.domain.entity.AdapterFieldMapping;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DynamicFieldMapper {

    private final ObjectMapper objectMapper;

    public DynamicFieldMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JsonNode mapToCanonical(String rawJsonResponse, List<AdapterFieldMapping> mappings) throws Exception {
        JsonNode sourceNode = objectMapper.readTree(rawJsonResponse);
        ObjectNode canonicalNode = objectMapper.createObjectNode();

        // Very basic mapping for demonstration
        for (AdapterFieldMapping mapping : mappings) {
            JsonNode value = sourceNode.at("/" + mapping.getSourceField().replace(".", "/"));
            if (!value.isMissingNode()) {
                canonicalNode.set(mapping.getCanonicalField(), value);
            }
        }

        return canonicalNode;
    }
}
