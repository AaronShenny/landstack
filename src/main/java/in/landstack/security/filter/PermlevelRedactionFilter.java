package in.landstack.security.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class PermlevelRedactionFilter extends OncePerRequestFilter {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(request, responseWrapper);

        // Process response payload for Permlevel Redaction
        String contentType = responseWrapper.getContentType();
        if (contentType != null && contentType.contains("application/json")) {
            byte[] responseArray = responseWrapper.getContentAsByteArray();
            String responseStr = new String(responseArray, responseWrapper.getCharacterEncoding());

            try {
                JsonNode rootNode = objectMapper.readTree(responseStr);
                
                // TODO: Fetch user's max permlevel from SecurityContext
                // TODO: Fetch CanonicalFields from DB
                // TODO: Traverse JsonNode and redact (remove) fields where field.permlevel > user.permlevel
                
                // For demonstration, we simply rewrite the exact same JSON.
                String updatedResponseStr = objectMapper.writeValueAsString(rootNode);
                byte[] updatedResponseArray = updatedResponseStr.getBytes(responseWrapper.getCharacterEncoding());
                
                responseWrapper.resetBuffer();
                responseWrapper.getOutputStream().write(updatedResponseArray);
                responseWrapper.setContentLength(updatedResponseArray.length);

            } catch (Exception e) {
                // If parsing fails, just write the original
                responseWrapper.copyBodyToResponse();
                return;
            }
        }
        
        responseWrapper.copyBodyToResponse();
    }
}
