package in.landstack.security.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.landstack.domain.service.PermlevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

@Component
public class PermlevelRedactionFilter extends OncePerRequestFilter {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PermlevelService permlevelService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(request, responseWrapper);

        String contentType = responseWrapper.getContentType();
        if (contentType != null && contentType.contains("application/json")) {
            
            // Determine resource name from URI
            String uri = request.getRequestURI();
            String resourceName = null;
            if (uri.endsWith("/ror")) {
                resourceName = "RECORD_OF_RIGHTS";
            } else if (uri.contains("/parcels")) {
                resourceName = "PARCEL";
            }
            
            byte[] responseArray = responseWrapper.getContentAsByteArray();
            
            if (resourceName != null) {
                String responseStr = new String(responseArray, responseWrapper.getCharacterEncoding());

                try {
                    JsonNode rootNode = objectMapper.readTree(responseStr);
                    
                    int userPermlevel = permlevelService.getUserMaxPermlevel(resourceName);
                    Map<String, Integer> fieldPermlevels = permlevelService.getFieldPermlevels(resourceName);
                    
                    redactNode(rootNode, fieldPermlevels, userPermlevel);
                    
                    String updatedResponseStr = objectMapper.writeValueAsString(rootNode);
                    byte[] updatedResponseArray = updatedResponseStr.getBytes(responseWrapper.getCharacterEncoding());
                    
                    responseWrapper.resetBuffer();
                    responseWrapper.getOutputStream().write(updatedResponseArray);
                    responseWrapper.setContentLength(updatedResponseArray.length);
                    responseWrapper.copyBodyToResponse();
                    return;

                } catch (Exception e) {
                    // Fall back to original response if parsing fails
                }
            }
        }
        
        responseWrapper.copyBodyToResponse();
    }
    
    private void redactNode(JsonNode node, Map<String, Integer> fieldPermlevels, int userPermlevel) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fieldName = field.getKey();
                
                Integer requiredPermlevel = fieldPermlevels.get(fieldName);
                if (requiredPermlevel != null && requiredPermlevel > userPermlevel) {
                    fields.remove(); // Remove field if user lacks clearance
                } else {
                    redactNode(field.getValue(), fieldPermlevels, userPermlevel);
                }
            }
        } else if (node.isArray()) {
            for (JsonNode arrayItem : node) {
                redactNode(arrayItem, fieldPermlevels, userPermlevel);
            }
        }
    }
}
