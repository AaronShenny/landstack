package in.landstack.security;

import in.landstack.domain.entity.User;
import in.landstack.domain.entity.UserRole;
import in.landstack.domain.repository.UserRepository;
import in.landstack.domain.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (Boolean.TRUE.equals(user.getIsSuperadmin())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_SUPERADMIN"));
        }
        
        List<UserRole> roles = userRoleRepository.findByUser_UserId(user.getUserId());
        for (UserRole ur : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + ur.getRole().getRoleId()));
        }
        
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                authorities
        );
    }
}
