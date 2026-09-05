package in.landstack.security;

import in.landstack.api.dto.request.UserCreateDTO;
import in.landstack.domain.entity.Parcel;
import in.landstack.domain.entity.State;
import in.landstack.domain.repository.ParcelRepository;
import in.landstack.domain.repository.StateRepository;
import in.landstack.domain.repository.UserRepository;
import in.landstack.domain.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class StateFilterIntegrationTest {

    @Autowired
    private ParcelRepository parcelRepository;

    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        State kl = stateRepository.findById("KL").orElseGet(() -> {
            State s = new State();
            s.setStateCode("KL");
            s.setName("Kerala");
            return stateRepository.save(s);
        });

        State mh = stateRepository.findById("MH").orElseGet(() -> {
            State s = new State();
            s.setStateCode("MH");
            s.setName("Maharashtra");
            return stateRepository.save(s);
        });

        GeometryFactory gf = new GeometryFactory();
        Coordinate[] coords = new Coordinate[]{
            new Coordinate(0, 0),
            new Coordinate(1, 0),
            new Coordinate(1, 1),
            new Coordinate(0, 1),
            new Coordinate(0, 0)
        };
        Polygon dummyGeom = gf.createPolygon(coords);
        dummyGeom.setSRID(4326);

        Parcel p1 = new Parcel();
        p1.setUlpin("KL-TEST-" + UUID.randomUUID().toString().substring(0, 5));
        p1.setState(kl);
        p1.setLocalParcelId("1");
        p1.setGeom(dummyGeom);
        p1.setCreatedAt(LocalDateTime.now());
        p1.setUpdatedAt(LocalDateTime.now());
        parcelRepository.save(p1);

        Parcel p2 = new Parcel();
        p2.setUlpin("MH-TEST-" + UUID.randomUUID().toString().substring(0, 5));
        p2.setState(mh);
        p2.setLocalParcelId("2");
        p2.setGeom(dummyGeom);
        p2.setCreatedAt(LocalDateTime.now());
        p2.setUpdatedAt(LocalDateTime.now());
        parcelRepository.save(p2);
        
        var existingUser = userRepository.findByUsername("kerala_user");
        if (existingUser.isEmpty()) {
            UserCreateDTO dto = new UserCreateDTO();
            dto.setUsername("kerala_user");
            dto.setEmail("kerala@landstack.in");
            dto.setPassword("password");
            dto.setIsSuperadmin(false);
            var user = userService.createUser(dto);
            userService.assignPermission(user.getUserId(), "state_code", "KL");
        }
    }
    
    @Test
    @WithMockUser(username = "kerala_user")
    void testStateFilter_AppliedToKeralaUser() {
        var parcels = parcelRepository.findAll();
        long klCount = parcels.stream().filter(p -> p.getUlpin().startsWith("KL-TEST")).count();
        long mhCount = parcels.stream().filter(p -> p.getUlpin().startsWith("MH-TEST")).count();
        
        assertEquals(1, klCount, "KL test parcel should be visible");
        assertEquals(0, mhCount, "MH test parcel should NOT be visible");
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_SUPERADMIN"})
    void testStateFilter_BypassedForSuperadmin() {
        var parcels = parcelRepository.findAll();
        long klCount = parcels.stream().filter(p -> p.getUlpin().startsWith("KL-TEST")).count();
        long mhCount = parcels.stream().filter(p -> p.getUlpin().startsWith("MH-TEST")).count();
        
        assertEquals(1, klCount, "KL test parcel should be visible to superadmin");
        assertEquals(1, mhCount, "MH test parcel should be visible to superadmin");
    }
}
