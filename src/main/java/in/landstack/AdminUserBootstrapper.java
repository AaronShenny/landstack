package in.landstack;

import in.landstack.api.dto.request.UserCreateDTO;
import in.landstack.domain.repository.UserRepository;
import in.landstack.domain.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminUserBootstrapper implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserService userService;

    public AdminUserBootstrapper(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("admin").isEmpty()) {
            UserCreateDTO adminDTO = new UserCreateDTO();
            adminDTO.setUsername("admin");
            adminDTO.setEmail("admin@landstack.in");
            adminDTO.setPassword("admin");
            adminDTO.setIsSuperadmin(true);
            
            userService.createUser(adminDTO);
            System.out.println("Default superadmin user created: admin / admin");
        }
    }
}
