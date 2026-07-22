package edu.carpool.carpool_rideshare.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import edu.carpool.carpool_rideshare.entity.User;
import edu.carpool.carpool_rideshare.repository.UserRepository;

import java.util.stream.Collectors;


@Service
public class CustomUserDetailsService implements UserDetailsService{
    
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with the email: " + email));
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("Role_" + role.name()))
                        .collect(Collectors.toList()))
                .build();
    }

}

