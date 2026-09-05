package in.landstack.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import in.landstack.domain.entity.Parcel;
import in.landstack.domain.service.ParcelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/parcels")
public class ParcelController {

    private final ParcelService parcelService;

    public ParcelController(ParcelService parcelService) {
        this.parcelService = parcelService;
    }

    @GetMapping
    public ResponseEntity<List<Parcel>> getParcelsByBbox(
            @RequestParam double minLon,
            @RequestParam double minLat,
            @RequestParam double maxLon,
            @RequestParam double maxLat) {
        
        List<Parcel> parcels = parcelService.getParcelsInBoundingBox(minLon, minLat, maxLon, maxLat);
        return ResponseEntity.ok(parcels);
    }

    @GetMapping("/{ulpin}/ror")
    public ResponseEntity<JsonNode> getRecordOfRights(@PathVariable String ulpin) {
        try {
            JsonNode rorData = parcelService.getFederatedRoRData(ulpin);
            // NOTE: The PermlevelRedactionFilter will automatically intercept this response
            // and redact any sensitive PII fields before it reaches the client.
            return ResponseEntity.ok(rorData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
