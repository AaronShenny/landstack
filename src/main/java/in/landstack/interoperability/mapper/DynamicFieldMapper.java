package in.landstack.interoperability.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import in.landstack.domain.entity.AdapterFieldMapping;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DynamicFieldMapper {

    private final ObjectMapper objectMapper;
    private final Configuration jsonPathConfig;

    public DynamicFieldMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.jsonPathConfig = Configuration.builder()
                .jsonProvider(new JacksonJsonNodeJsonProvider())
                .mappingProvider(new JacksonMappingProvider(objectMapper))
                .options(Option.SUPPRESS_EXCEPTIONS)
                .build();
    }

    public JsonNode mapToCanonical(String rawJsonResponse, List<AdapterFieldMapping> mappings) throws Exception {
        JsonNode sourceNode = objectMapper.readTree(rawJsonResponse);
        
        // If it's a FeatureCollection, we map each feature individually
        if (sourceNode.has("type") && "FeatureCollection".equals(sourceNode.get("type").asText()) && sourceNode.has("features")) {
            ArrayNode features = (ArrayNode) sourceNode.get("features");
            ArrayNode canonicalArray = objectMapper.createArrayNode();
            
            for (JsonNode feature : features) {
                canonicalArray.add(mapSingleNode(feature, mappings));
            }
            return canonicalArray;
        }

        // If it's a generic JSON Array
        if (sourceNode.isArray()) {
            ArrayNode canonicalArray = objectMapper.createArrayNode();
            for (JsonNode element : sourceNode) {
                canonicalArray.add(mapSingleNode(element, mappings));
            }
            return canonicalArray;
        }

        // Otherwise treat as a single object
        return mapSingleNode(sourceNode, mappings);
    }

    private ObjectNode mapSingleNode(JsonNode node, List<AdapterFieldMapping> mappings) {
        ObjectNode canonicalNode = objectMapper.createObjectNode();
        // Read JSON as document using JsonPath against the individual node
        Object document = jsonPathConfig.jsonProvider().parse(node.toString());

        for (AdapterFieldMapping mapping : mappings) {
            String jsonPathExpr = mapping.getSourceField();
            if (!jsonPathExpr.startsWith("$")) {
                jsonPathExpr = "$." + jsonPathExpr;
            }

            try {
                JsonNode valueNode = JsonPath.using(jsonPathConfig).parse(document).read(jsonPathExpr, JsonNode.class);

                if (valueNode != null && !valueNode.isMissingNode() && !valueNode.isNull()) {
                    JsonNode transformedValue = applyTransform(valueNode, mapping.getTransformHint());
                    canonicalNode.set(mapping.getCanonicalField(), transformedValue);
                }
            } catch (Exception e) {
                // Ignore path not found for this specific node
            }
        }
        return canonicalNode;
    }

    private JsonNode applyTransform(JsonNode valueNode, String transformHint) {
        if (transformHint == null || transformHint.isBlank()) {
            return valueNode;
        }

        switch (transformHint.toLowerCase()) {
            case "sqft_to_sqm":
                if (valueNode.isNumber()) {
                    return objectMapper.valueToTree(valueNode.asDouble() * 0.092903);
                }
                break;
            case "acres_to_sqm":
                if (valueNode.isNumber()) {
                    return objectMapper.valueToTree(valueNode.asDouble() * 4046.86);
                }
                break;
            case "date_normalization":
                if (valueNode.isTextual()) {
                    try {
                        String rawDate = valueNode.asText();
                        return objectMapper.valueToTree(rawDate.replace(" ", "T")); 
                    } catch (Exception e) {
                        return valueNode;
                    }
                }
                break;
            default:
                return valueNode;
        }
        return valueNode;
    }
}
