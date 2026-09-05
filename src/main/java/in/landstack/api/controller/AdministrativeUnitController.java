package in.landstack.api.controller;

import in.landstack.domain.entity.District;
import in.landstack.domain.entity.State;
import in.landstack.domain.entity.SubDistrict;
import in.landstack.domain.entity.Village;
import in.landstack.domain.repository.DistrictRepository;
import in.landstack.domain.repository.StateRepository;
import in.landstack.domain.repository.SubDistrictRepository;
import in.landstack.domain.repository.VillageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class AdministrativeUnitController {

    private final StateRepository stateRepository;
    private final DistrictRepository districtRepository;
    private final SubDistrictRepository subDistrictRepository;
    private final VillageRepository villageRepository;

    public AdministrativeUnitController(StateRepository stateRepository,
                                        DistrictRepository districtRepository,
                                        SubDistrictRepository subDistrictRepository,
                                        VillageRepository villageRepository) {
        this.stateRepository = stateRepository;
        this.districtRepository = districtRepository;
        this.subDistrictRepository = subDistrictRepository;
        this.villageRepository = villageRepository;
    }

    @GetMapping("/states")
    public ResponseEntity<List<State>> getAllStates() {
        return ResponseEntity.ok(stateRepository.findAll());
    }

    @GetMapping("/states/{code}/districts")
    public ResponseEntity<List<District>> getDistrictsByState(@PathVariable String code) {
        List<District> allDistricts = districtRepository.findAll();
        List<District> filtered = allDistricts.stream()
                .filter(d -> d.getState().getStateCode().equals(code))
                .collect(Collectors.toList());
        return ResponseEntity.ok(filtered);
    }

    @GetMapping("/districts/{code}/subdistricts")
    public ResponseEntity<List<SubDistrict>> getSubDistrictsByDistrict(@PathVariable String code) {
        List<SubDistrict> allSub = subDistrictRepository.findAll();
        List<SubDistrict> filtered = allSub.stream()
                .filter(sd -> sd.getDistrict().getDistrictCode().equals(code))
                .collect(Collectors.toList());
        return ResponseEntity.ok(filtered);
    }

    @GetMapping("/subdistricts/{code}/villages")
    public ResponseEntity<List<Village>> getVillagesBySubDistrict(@PathVariable String code) {
        List<Village> allV = villageRepository.findAll();
        List<Village> filtered = allV.stream()
                .filter(v -> v.getSubDistrict().getSubDistrictCode().equals(code))
                .collect(Collectors.toList());
        return ResponseEntity.ok(filtered);
    }

    @GetMapping("/administrative-units")
    public ResponseEntity<?> getAdministrativeUnits(@org.springframework.web.bind.annotation.RequestParam(required = false) String parent_code) {
        if (parent_code == null || parent_code.isEmpty()) {
            return ResponseEntity.ok(stateRepository.findAll());
        }
        
        // Infer type from code format (State: XX, District: XX-XXX, SubDistrict: XX-XXX-XXX)
        String[] parts = parent_code.split("-");
        if (parts.length == 1) {
            // Parent is state -> return districts
            List<District> allDistricts = districtRepository.findAll();
            return ResponseEntity.ok(allDistricts.stream().filter(d -> d.getState().getStateCode().equals(parent_code)).collect(Collectors.toList()));
        } else if (parts.length == 2) {
            // Parent is district -> return subdistricts
            List<SubDistrict> allSub = subDistrictRepository.findAll();
            return ResponseEntity.ok(allSub.stream().filter(sd -> sd.getDistrict().getDistrictCode().equals(parent_code)).collect(Collectors.toList()));
        } else if (parts.length == 3) {
            // Parent is subdistrict -> return villages
            List<Village> allV = villageRepository.findAll();
            return ResponseEntity.ok(allV.stream().filter(v -> v.getSubDistrict().getSubDistrictCode().equals(parent_code)).collect(Collectors.toList()));
        }
        
        return ResponseEntity.badRequest().body("Invalid parent_code format");
    }
}
