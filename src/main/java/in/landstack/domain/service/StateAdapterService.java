package in.landstack.domain.service;

import in.landstack.api.dto.request.StateAdapterCreateDTO;
import in.landstack.api.dto.request.StateAdapterStatusUpdateDTO;
import in.landstack.api.dto.request.StateAdapterUpdateDTO;
import in.landstack.domain.entity.State;
import in.landstack.domain.entity.StateAdapter;
import in.landstack.domain.enums.AdapterStatus;
import in.landstack.domain.repository.StateAdapterRepository;
import in.landstack.domain.repository.StateRepository;
import in.landstack.domain.repository.AdapterTestRunRepository;
import in.landstack.domain.entity.AdapterTestRun;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StateAdapterService {

    private final StateAdapterRepository stateAdapterRepository;
    private final StateRepository stateRepository;
    private final AdapterTestRunRepository testRunRepository;

    public StateAdapterService(StateAdapterRepository stateAdapterRepository, StateRepository stateRepository, AdapterTestRunRepository testRunRepository) {
        this.stateAdapterRepository = stateAdapterRepository;
        this.stateRepository = stateRepository;
        this.testRunRepository = testRunRepository;
    }

    public List<StateAdapter> getAllAdapters() {
        return stateAdapterRepository.findAll();
    }

    public StateAdapter getAdapter(String stateCode) {
        return stateAdapterRepository.findById(stateCode)
                .orElseThrow(() -> new IllegalArgumentException("State adapter not found for code: " + stateCode));
    }

    @Transactional
    public StateAdapter createAdapter(StateAdapterCreateDTO dto) {
        if (stateAdapterRepository.existsById(dto.getStateCode())) {
            throw new IllegalArgumentException("State adapter already exists for code: " + dto.getStateCode());
        }

        State state = stateRepository.findById(dto.getStateCode()).orElseGet(() -> {
            State s = new State();
            s.setStateCode(dto.getStateCode());
            s.setName(dto.getStateName());
            return stateRepository.save(s);
        });

        StateAdapter adapter = new StateAdapter();
        adapter.setStateCode(state.getStateCode());
        adapter.setState(state);
        adapter.setBaseUrl(dto.getBaseUrl());
        adapter.setStatus(AdapterStatus.DRAFT);
        // default 6-field cron to satisfy Spring Boot best practices rule
        adapter.setSyncCronExpression("0 0 0 * * *");
        
        return stateAdapterRepository.save(adapter);
    }

    @Transactional
    public StateAdapter updateAdapter(String stateCode, StateAdapterUpdateDTO dto) {
        StateAdapter adapter = getAdapter(stateCode);
        
        // Validate cron expression (Must be 6 fields for Spring Boot)
        if (dto.getSyncCronExpression() != null) {
            String[] parts = dto.getSyncCronExpression().trim().split("\\s+");
            if (parts.length != 6) {
                throw new IllegalArgumentException("Cron expression must have exactly 6 fields (Second, Minute, Hour, Day of Month, Month, Day of Week)");
            }
            if (!CronExpression.isValidExpression(dto.getSyncCronExpression())) {
                throw new IllegalArgumentException("Invalid cron expression format");
            }
        }

        adapter.setBaseUrl(dto.getBaseUrl());
        adapter.setAuthType(dto.getAuthType());
        adapter.setAuthCredentials(dto.getAuthCredentials());
        adapter.setSyncCronExpression(dto.getSyncCronExpression());

        return stateAdapterRepository.save(adapter);
    }

    @Transactional
    public StateAdapter updateStatus(String stateCode, StateAdapterStatusUpdateDTO dto) {
        StateAdapter adapter = getAdapter(stateCode);
        AdapterStatus current = adapter.getStatus();
        AdapterStatus target = dto.getStatus();

        if (current == target) return adapter;

        // Transitions: DRAFT -> CONFIGURING -> MAPPING -> TESTING -> VALIDATED -> ACTIVE <-> SUSPENDED
        boolean isValid = false;
        switch (current) {
            case DRAFT:
                if (target == AdapterStatus.CONFIGURING) isValid = true;
                break;
            case CONFIGURING:
                if (target == AdapterStatus.MAPPING || target == AdapterStatus.DRAFT) isValid = true;
                break;
            case MAPPING:
                if (target == AdapterStatus.TESTING || target == AdapterStatus.CONFIGURING) isValid = true;
                break;
            case TESTING:
                if (target == AdapterStatus.VALIDATED) {
                    List<AdapterTestRun> runs = testRunRepository.findByStateCodeOrderByTestTimestampDesc(stateCode);
                    if (runs.isEmpty() || !runs.get(0).isSuccessful()) {
                        throw new IllegalArgumentException("Cannot transition to VALIDATED. Adapter must have a passing test run.");
                    }
                    isValid = true;
                } else if (target == AdapterStatus.MAPPING) {
                    isValid = true;
                }
                break;
            case VALIDATED:
                if (target == AdapterStatus.ACTIVE || target == AdapterStatus.TESTING) isValid = true;
                break;
            case ACTIVE:
                if (target == AdapterStatus.SUSPENDED) isValid = true;
                break;
            case SUSPENDED:
                if (target == AdapterStatus.ACTIVE) isValid = true;
                break;
        }

        if (!isValid) {
            throw new IllegalArgumentException(String.format("Invalid status transition from %s to %s", current, target));
        }

        adapter.setStatus(target);
        return stateAdapterRepository.save(adapter);
    }

    @Transactional
    public void deleteAdapter(String stateCode) {
        StateAdapter adapter = getAdapter(stateCode);
        // Soft delete/deactivate logic as per requirements. We will transition to SUSPENDED.
        // If it's already SUSPENDED or DRAFT we could physically delete, but the requirement 
        // states "soft delete / deactivate" so transitioning to SUSPENDED makes sense for safety.
        adapter.setStatus(AdapterStatus.SUSPENDED);
        stateAdapterRepository.save(adapter);
    }
}

