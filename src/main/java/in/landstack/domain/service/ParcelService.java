package in.landstack.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import in.landstack.domain.entity.AdapterEndpoint;
import in.landstack.domain.entity.AdapterFieldMapping;
import in.landstack.domain.entity.Parcel;
import in.landstack.domain.entity.StateAdapter;
import in.landstack.domain.repository.AdapterEndpointRepository;
import in.landstack.domain.repository.AdapterFieldMappingRepository;
import in.landstack.domain.repository.ParcelRepository;
import in.landstack.domain.repository.StateAdapterRepository;
import in.landstack.interoperability.client.StateApiClient;
import in.landstack.interoperability.mapper.DynamicFieldMapper;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ParcelService {

    private final ParcelRepository parcelRepository;
    private final StateAdapterRepository stateAdapterRepository;
    private final AdapterEndpointRepository endpointRepository;
    private final AdapterFieldMappingRepository mappingRepository;
    private final StateApiClient stateApiClient;
    private final DynamicFieldMapper fieldMapper;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    public ParcelService(ParcelRepository parcelRepository,
                         StateAdapterRepository stateAdapterRepository,
                         AdapterEndpointRepository endpointRepository,
                         AdapterFieldMappingRepository mappingRepository,
                         StateApiClient stateApiClient,
                         DynamicFieldMapper fieldMapper) {
        this.parcelRepository = parcelRepository;
        this.stateAdapterRepository = stateAdapterRepository;
        this.endpointRepository = endpointRepository;
        this.mappingRepository = mappingRepository;
        this.stateApiClient = stateApiClient;
        this.fieldMapper = fieldMapper;
    }

    public List<Parcel> getParcelsInBoundingBox(double minLon, double minLat, double maxLon, double maxLat) {
        // Create a JTS Polygon representing the bounding box
        Coordinate[] coords = new Coordinate[] {
            new Coordinate(minLon, minLat),
            new Coordinate(minLon, maxLat),
            new Coordinate(maxLon, maxLat),
            new Coordinate(maxLon, minLat),
            new Coordinate(minLon, minLat)
        };
        Polygon bbox = geometryFactory.createPolygon(coords);
        bbox.setSRID(4326);
        
        return parcelRepository.findWithinBoundingBox(bbox);
    }

    public JsonNode getFederatedRoRData(String ulpin) throws Exception {
        Parcel parcel = parcelRepository.findById(ulpin)
                .orElseThrow(() -> new RuntimeException("Parcel not found for ULPIN: " + ulpin));

        String stateCode = parcel.getState().getStateCode();
        
        StateAdapter adapter = stateAdapterRepository.findById(stateCode)
                .orElseThrow(() -> new RuntimeException("State Adapter not configured for state: " + stateCode));
                
        AdapterEndpoint rorEndpoint = endpointRepository.findByStateAdapter_StateCodeAndCapability(stateCode, "ROR")
                .orElseThrow(() -> new RuntimeException("ROR endpoint not configured for state: " + stateCode));

        List<AdapterFieldMapping> mappings = mappingRepository.findByStateAdapter_StateCodeAndCapability(stateCode, "ROR");

        // Pass local parcel ID to state API as query param
        Map<String, String> params = Map.of("parcelId", parcel.getLocalParcelId());
        
        // 1. Fetch raw ROR data dynamically from State
        String rawJsonResponse = stateApiClient.executeRequest(adapter, rorEndpoint, params);
        
        // 2. Map to canonical JSON
        return fieldMapper.mapToCanonical(rawJsonResponse, mappings);
    }
}
