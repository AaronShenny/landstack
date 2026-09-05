package in.landstack.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
public class CapabilityNotSupportedException extends RuntimeException {
    public CapabilityNotSupportedException(String message) {
        super(message);
    }
}
