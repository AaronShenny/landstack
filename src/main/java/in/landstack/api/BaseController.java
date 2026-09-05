package in.landstack.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class BaseController {

    @GetMapping("/status")
    public Map<String, String> status() {
        return Map.of("status", "API V1 Foundation is running");
    }
}
