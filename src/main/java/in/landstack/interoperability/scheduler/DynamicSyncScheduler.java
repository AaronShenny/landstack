package in.landstack.interoperability.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import in.landstack.domain.entity.*;
import in.landstack.domain.repository.*;
import in.landstack.interoperability.client.StateApiClient;
import in.landstack.interoperability.mapper.DynamicFieldMapper;
import in.landstack.domain.enums.AdapterCapability;
import in.landstack.domain.enums.AdapterStatus;
import in.landstack.domain.service.CapabilityCheckService;
import in.landstack.interoperability.gis.CrsNormalizationService;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.geotools.geojson.geom.GeometryJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class DynamicSyncScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DynamicSyncScheduler.class);

    private final TaskScheduler taskScheduler;
    private final StateAdapterRepository stateAdapterRepository;
    private final AdapterEndpointRepository endpointRepository;
    private final AdapterFieldMappingRepository mappingRepository;
    private final StateApiClient stateApiClient;
    private final DynamicFieldMapper fieldMapper;
    private final CapabilityCheckService capabilityCheckService;
    private final ParcelRepository parcelRepository;
    private final CrsNormalizationService crsNormalizationService;
    private final SyncJobRepository syncJobRepository;
    private final DistrictRepository districtRepository;
    private final VillageRepository villageRepository;

    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public DynamicSyncScheduler(TaskScheduler taskScheduler,
                                StateAdapterRepository stateAdapterRepository,
                                AdapterEndpointRepository endpointRepository,
                                AdapterFieldMappingRepository mappingRepository,
                                StateApiClient stateApiClient,
                                DynamicFieldMapper fieldMapper,
                                CapabilityCheckService capabilityCheckService,
                                ParcelRepository parcelRepository,
                                CrsNormalizationService crsNormalizationService,
                                SyncJobRepository syncJobRepository,
                                DistrictRepository districtRepository,
                                VillageRepository villageRepository) {
        this.taskScheduler = taskScheduler;
        this.stateAdapterRepository = stateAdapterRepository;
        this.endpointRepository = endpointRepository;
        this.mappingRepository = mappingRepository;
        this.stateApiClient = stateApiClient;
        this.fieldMapper = fieldMapper;
        this.capabilityCheckService = capabilityCheckService;
        this.parcelRepository = parcelRepository;
        this.crsNormalizationService = crsNormalizationService;
        this.syncJobRepository = syncJobRepository;
        this.districtRepository = districtRepository;
        this.villageRepository = villageRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initSchedulers() {
        List<StateAdapter> activeAdapters = stateAdapterRepository.findByStatus(AdapterStatus.ACTIVE);
        for (StateAdapter adapter : activeAdapters) {
            if (!capabilityCheckService.supportsCapability(adapter.getStateCode(), AdapterCapability.PARCEL_GEOMETRY)) {
                continue;
            }
            scheduleStateSync(adapter);
        }
    }

    public void scheduleStateSync(StateAdapter stateAdapter) {
        if (stateAdapter.getSyncCronExpression() == null || stateAdapter.getSyncCronExpression().isEmpty()) {
            return;
        }

        String stateCode = stateAdapter.getStateCode();

        if (scheduledTasks.containsKey(stateCode)) {
            scheduledTasks.get(stateCode).cancel(false);
        }

        logger.info("Scheduling sync job for state {} with cron {}", stateCode, stateAdapter.getSyncCronExpression());

        ScheduledFuture<?> future = taskScheduler.schedule(
                () -> executeSyncJob(stateAdapter),
                new CronTrigger(stateAdapter.getSyncCronExpression())
        );

        scheduledTasks.put(stateCode, future);
    }

    public SyncJob executeSyncJob(StateAdapter adapter) {
        logger.info("Executing periodic boundary sync for state: {}", adapter.getStateCode());
        
        SyncJob job = new SyncJob();
        job.setJobId(UUID.randomUUID());
        job.setStateAdapter(adapter);
        job.setStartedAt(LocalDateTime.now());
        job.setStatus("IN_PROGRESS");
        job.setRecordsUpdated(0);
        job = syncJobRepository.save(job);

        try {
            Optional<AdapterEndpoint> boundaryEndpoint = endpointRepository
                    .findByStateAdapter_StateCodeAndCapability(adapter.getStateCode(), AdapterCapability.PARCEL_GEOMETRY);
            
            if (boundaryEndpoint.isEmpty()) {
                job.setStatus("FAILED");
                job.setErrorLog("No GEOMETRY endpoint configured for state");
                job.setCompletedAt(LocalDateTime.now());
                return syncJobRepository.save(job);
            }

            List<AdapterFieldMapping> mappings = mappingRepository
                    .findByStateAdapter_StateCodeAndCapability(adapter.getStateCode(), AdapterCapability.PARCEL_GEOMETRY);

            // 1. Fetch raw data
            String rawJsonResponse = stateApiClient.executeRequest(adapter, boundaryEndpoint.get(), null);

            // 2. Map fields to canonical JSON
            JsonNode canonicalNodes = fieldMapper.mapToCanonical(rawJsonResponse, mappings);
            
            int successCount = 0;
            StringBuilder errorLog = new StringBuilder();

            // 3. Process geometry, normalize CRS, and save to DB
            if (canonicalNodes.isArray()) {
                for (JsonNode node : canonicalNodes) {
                    try {
                        processAndSaveParcel(adapter, node);
                        successCount++;
                    } catch (Exception e) {
                        logger.error("Error processing feature", e);
                        errorLog.append("Feature failed: ").append(e.getMessage()).append("\n");
                    }
                }
            } else {
                try {
                    processAndSaveParcel(adapter, canonicalNodes);
                    successCount++;
                } catch (Exception e) {
                    logger.error("Error processing single feature", e);
                    errorLog.append("Feature failed: ").append(e.getMessage()).append("\n");
                }
            }
            
            job.setRecordsUpdated(successCount);
            if (errorLog.length() > 0 && successCount > 0) {
                job.setStatus("PARTIAL_SUCCESS");
                job.setErrorLog(errorLog.toString());
            } else if (errorLog.length() > 0 && successCount == 0) {
                job.setStatus("FAILED");
                job.setErrorLog(errorLog.toString());
            } else {
                job.setStatus("SUCCESS");
            }
            
        } catch (Exception e) {
            logger.error("Error during sync job for state {}", adapter.getStateCode(), e);
            job.setStatus("FAILED");
            job.setErrorLog("Job exception: " + e.getMessage());
        }

        job.setCompletedAt(LocalDateTime.now());
        return syncJobRepository.save(job);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processAndSaveParcel(StateAdapter adapter, JsonNode canonicalNode) {
        String localParcelId = canonicalNode.has("localParcelId") ? canonicalNode.get("localParcelId").asText() : "UNKNOWN";
        String districtCode = canonicalNode.has("districtCode") ? canonicalNode.get("districtCode").asText() : "UNKNOWN";
        String villageCode = canonicalNode.has("villageCode") ? canonicalNode.get("villageCode").asText() : "UNKNOWN";
        
        // Generate ULPIN algorithm: STATE-DISTRICT-VILLAGE-LOCALID
        String ulpin = String.format("%s-%s-%s-%s", adapter.getStateCode(), districtCode, villageCode, localParcelId);
        
        Parcel parcel = parcelRepository.findById(ulpin).orElse(new Parcel());
        parcel.setUlpin(ulpin);
        parcel.setState(adapter.getState());
        parcel.setLocalParcelId(localParcelId);

        if (!"UNKNOWN".equals(districtCode)) {
            districtRepository.findById(districtCode).ifPresent(parcel::setDistrict);
        }
        if (!"UNKNOWN".equals(villageCode)) {
            villageRepository.findById(villageCode).ifPresent(parcel::setVillage);
        }

        if (canonicalNode.has("geometry")) {
            String geomStr = canonicalNode.get("geometry").toString();
            if (canonicalNode.get("geometry").isTextual()) {
                geomStr = canonicalNode.get("geometry").asText();
            }

            try {
                GeometryJSON reader = new GeometryJSON();
                Geometry sourceGeom = reader.read(new java.io.StringReader(geomStr));

                if (sourceGeom instanceof Polygon) {
                    parcel.setSourceGeom((Polygon) sourceGeom);
                    
                    String sourceCrs = "CRS_UNKNOWN";
                    if (canonicalNode.has("sourceCrs")) {
                        sourceCrs = canonicalNode.get("sourceCrs").asText();
                    } else if (canonicalNode.get("geometry").has("crs")) {
                        JsonNode crsNode = canonicalNode.get("geometry").get("crs");
                        if (crsNode.has("properties") && crsNode.get("properties").has("name")) {
                            sourceCrs = crsNode.get("properties").get("name").asText();
                        }
                    }
                    parcel.setSourceCrs(sourceCrs);

                    try {
                        Geometry wgs84Geom = crsNormalizationService.transformToWgs84(sourceGeom, sourceCrs);
                        parcel.setGeom((Polygon) wgs84Geom);
                    } catch (Exception e) {
                        logger.error("Failed to transform geometry for parcel {}", ulpin, e);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Geometry parsing failed: " + e.getMessage(), e);
            }
        }
        
        parcelRepository.save(parcel);
    }
}
