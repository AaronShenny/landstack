package in.landstack.interoperability.scheduler;

import in.landstack.domain.entity.AdapterEndpoint;
import in.landstack.domain.entity.AdapterFieldMapping;
import in.landstack.domain.entity.StateAdapter;
import in.landstack.domain.repository.AdapterEndpointRepository;
import in.landstack.domain.repository.AdapterFieldMappingRepository;
import in.landstack.domain.repository.ParcelRepository;
import in.landstack.domain.repository.StateAdapterRepository;
import in.landstack.interoperability.client.StateApiClient;
import in.landstack.interoperability.mapper.DynamicFieldMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
public class DynamicSyncScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DynamicSyncScheduler.class);

    private final TaskScheduler taskScheduler;
    private final StateAdapterRepository stateAdapterRepository;
    private final AdapterEndpointRepository endpointRepository;
    private final AdapterFieldMappingRepository mappingRepository;
    private final StateApiClient stateApiClient;
    private final DynamicFieldMapper fieldMapper;
    private final ParcelRepository parcelRepository;

    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public DynamicSyncScheduler(TaskScheduler taskScheduler,
                                StateAdapterRepository stateAdapterRepository,
                                AdapterEndpointRepository endpointRepository,
                                AdapterFieldMappingRepository mappingRepository,
                                StateApiClient stateApiClient,
                                DynamicFieldMapper fieldMapper,
                                ParcelRepository parcelRepository) {
        this.taskScheduler = taskScheduler;
        this.stateAdapterRepository = stateAdapterRepository;
        this.endpointRepository = endpointRepository;
        this.mappingRepository = mappingRepository;
        this.stateApiClient = stateApiClient;
        this.fieldMapper = fieldMapper;
        this.parcelRepository = parcelRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initSchedulers() {
        List<StateAdapter> activeAdapters = stateAdapterRepository.findByStatus("ACTIVE");
        for (StateAdapter adapter : activeAdapters) {
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

    private void executeSyncJob(StateAdapter adapter) {
        logger.info("Executing periodic boundary sync for state: {}", adapter.getStateCode());
        
        try {
            Optional<AdapterEndpoint> boundaryEndpoint = endpointRepository
                    .findByStateAdapter_StateCodeAndCapability(adapter.getStateCode(), "GEOMETRY");
            
            if (boundaryEndpoint.isEmpty()) {
                logger.warn("No GEOMETRY endpoint configured for state {}", adapter.getStateCode());
                return;
            }

            List<AdapterFieldMapping> mappings = mappingRepository
                    .findByStateAdapter_StateCodeAndCapability(adapter.getStateCode(), "GEOMETRY");

            // 1. Fetch raw data
            String rawJsonResponse = stateApiClient.executeRequest(adapter, boundaryEndpoint.get(), null);

            // 2. Map fields to canonical JSON
            var canonicalNodes = fieldMapper.mapToCanonical(rawJsonResponse, mappings);
            
            // 3. Process geometry, normalize CRS, and save to DB
            logger.info("Successfully fetched and mapped geometry data for {}. (Geometry parsing and saving implemented in next iteration)", adapter.getStateCode());
            
        } catch (Exception e) {
            logger.error("Error during sync job for state {}", adapter.getStateCode(), e);
        }
    }
}
