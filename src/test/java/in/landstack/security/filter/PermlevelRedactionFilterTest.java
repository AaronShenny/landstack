package in.landstack.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.landstack.domain.service.PermlevelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class PermlevelRedactionFilterTest {

    @Mock
    private PermlevelService permlevelService;

    @InjectMocks
    private PermlevelRedactionFilter filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filter = new PermlevelRedactionFilter();
        ObjectMapper realMapper = new ObjectMapper();
        
        try {
            java.lang.reflect.Field mapperField = filter.getClass().getDeclaredField("objectMapper");
            mapperField.setAccessible(true);
            mapperField.set(filter, realMapper);
            
            java.lang.reflect.Field serviceField = filter.getClass().getDeclaredField("permlevelService");
            serviceField.setAccessible(true);
            serviceField.set(filter, permlevelService);
        } catch (Exception e) {}
    }

    @Test
    void testRedaction_Citizen_Permlevel0() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/parcels/123/ror");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) throws IOException {
                res.setContentType("application/json");
                res.getWriter().write("{\"ownerName\":\"John Doe\",\"area\":1000.0,\"encumbrances\":\"NONE\"}");
            }
        };

        when(permlevelService.getUserMaxPermlevel("RECORD_OF_RIGHTS")).thenReturn(0);
        when(permlevelService.getFieldPermlevels("RECORD_OF_RIGHTS")).thenReturn(Map.of(
            "ownerName", 1,
            "area", 0,
            "encumbrances", 0
        ));

        filter.doFilter(request, response, filterChain);

        String result = response.getContentAsString();
        assertTrue(result.contains("area"));
        assertTrue(result.contains("encumbrances"));
        assertFalse(result.contains("ownerName"), "PII field 'ownerName' should be redacted for permlevel 0");
    }

    @Test
    void testRedaction_Officer_Permlevel1() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/parcels/123/ror");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) throws IOException {
                res.setContentType("application/json");
                res.getWriter().write("{\"ownerName\":\"John Doe\",\"area\":1000.0,\"encumbrances\":\"NONE\"}");
            }
        };

        when(permlevelService.getUserMaxPermlevel("RECORD_OF_RIGHTS")).thenReturn(1);
        when(permlevelService.getFieldPermlevels("RECORD_OF_RIGHTS")).thenReturn(Map.of(
            "ownerName", 1,
            "area", 0,
            "encumbrances", 0
        ));

        filter.doFilter(request, response, filterChain);

        String result = response.getContentAsString();
        assertTrue(result.contains("area"));
        assertTrue(result.contains("encumbrances"));
        assertTrue(result.contains("ownerName"), "PII field 'ownerName' should NOT be redacted for permlevel 1");
    }
}
