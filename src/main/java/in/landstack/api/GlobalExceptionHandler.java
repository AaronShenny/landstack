package in.landstack.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import in.landstack.api.exception.CapabilityNotSupportedException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", "BAD_REQUEST");
        body.put("message", ex.getMessage());
        body.put("timestamp", Instant.now().toString());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CapabilityNotSupportedException.class)
    public ResponseEntity<Object> handleCapabilityNotSupported(CapabilityNotSupportedException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", "NOT_IMPLEMENTED");
        body.put("message", ex.getMessage());
        body.put("timestamp", Instant.now().toString());
        return new ResponseEntity<>(body, HttpStatus.NOT_IMPLEMENTED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllUncaughtException(Exception ex, WebRequest request) {
        log.error("Unknown error occurred", ex);
        
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", "INTERNAL_SERVER_ERROR");
        body.put("message", "An unexpected error occurred");
        body.put("timestamp", Instant.now().toString());
        body.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}


